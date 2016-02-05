package wycliffeassociates.recordingapp.AudioVisualization;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.Utils.U;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.MarkerView;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingMessage;
import wycliffeassociates.recordingapp.Recording.RecordingQueues;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.WavFileLoader;

/**
 * Created by sarabiaj on 9/10/2015.
 */

public class UIDataManager {

    static public final boolean PLAYBACK_MODE = true;
    static public final boolean RECORDING_MODE = false;
    private final WaveformView mainWave;
    private final MinimapView minimap;
    private final Activity ctx;
    private final MarkerView mStartMarker;
    private final MarkerView mEndMarker;
    private boolean isRecording;
    public static Semaphore lock;
    private WavFileLoader wavLoader;
    private WavVisualizer wavVis;
    private MappedByteBuffer buffer;
    private MappedByteBuffer mappedAudioFile;
    private MappedByteBuffer preprocessedBuffer;
    RecordingTimer timer;
    private final TextView timerView;
    private boolean playbackOrRecording;
    private boolean isALoadedFile = false;
    private CutOp mCutOp;


    public UIDataManager(WaveformView mainWave, MinimapView minimap, MarkerView start, MarkerView end, Activity ctx, boolean playbackOrRecording, boolean isALoadedFile){
        Logger.w(UIDataManager.class.toString(), "Is a loaded file: " + isALoadedFile);
        this.isALoadedFile = isALoadedFile;
        this.playbackOrRecording = playbackOrRecording;
        Logger.w(UIDataManager.class.toString(), "Playback mode: " + playbackOrRecording);
        mainWave.setUIDataManager(this);
        minimap.setUIDataManager(this);
        this.mainWave = mainWave;
        this.minimap = minimap;
        this.mStartMarker = start;
        this.mEndMarker = end;
        if(mEndMarker != null){
            Logger.w(this.toString(), "Connecting UImanager to marker views");
            start.setManager(this);
            end.setManager(this);
        }
        timerView = (TextView)ctx.findViewById(R.id.timerView);
        this.ctx = ctx;

        Logger.w(this.toString(), "passing cut to WavPlayer and Canvases");
        mCutOp = new CutOp();
        WavPlayer.setCutOp(mCutOp);
        mainWave.setCut(mCutOp);
        minimap.setCut(mCutOp);

        timer = new RecordingTimer();
    }

    public void startTimer(){
        timer.startTimer();
    }

    public void pauseTimer(){
        timer.pause();
    }

    public void resumeTimer(){
        timer.resume();
    }

    public MappedByteBuffer getMappedAudioFile(){
        return mappedAudioFile;
    }

    public void setIsRecording(boolean isRecording){
        this.isRecording = isRecording;
    }

    public void updateUI(){
        if(minimap == null || mainWave == null || WavPlayer.getDuration() == 0){
            //System.out.println("Update UI is returning early because either minimap, mainView, or Wavplayer.getDuration() is null/0");
            return;
        }
        if(wavLoader.visFileLoaded()){
            System.out.println("visFileLoaded() is true");
            wavVis.enableCompressedFileNextDraw(wavLoader.getMappedCacheFile());
        }
        //Marker is set to the percentage of playback times the width of the minimap
        int location = WavPlayer.getLocation();
        minimap.setMiniMarkerLoc((float) ((mCutOp.reverseTimeAdjusted(location) / ((double) WavPlayer.getDuration() - mCutOp.getSizeCut())) * minimap.getWidth()));
        drawWaveformDuringPlayback(location);
        mainWave.setTimeToDraw(location);
        int adjLoc = WavPlayer.getAdjustedLocation();
        final String time = String.format("%02d:%02d:%02d", adjLoc / 3600000, (adjLoc / 60000) % 60, (adjLoc / 1000) % 60);
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerView.setText(time);
                timerView.invalidate();
            }
        });
        if(mStartMarker != null ){
            int xStart = timeToScreenSpace(WavPlayer.getLocation(),
                    SectionMarkers.getStartLocationMs(), wavVis.millisecondsPerPixel());
            mStartMarker.setX(xStart - mStartMarker.getWidth() + (AudioInfo.SCREEN_WIDTH/8.f));
            int xEnd = timeToScreenSpace(WavPlayer.getLocation(),
                    SectionMarkers.getEndLocationMs(), wavVis.millisecondsPerPixel());
            mEndMarker.setX(xEnd + (AudioInfo.SCREEN_WIDTH/8.f));
//            Logger.w(this.toString(), "location is " + WavPlayer.getLocation());
//            Logger.w(this.toString(), "mspp is " + wavVis.millisecondsPerPixel());
//            Logger.w(this.toString(), "Start marker at: " + xStart);
//            Logger.w(this.toString(), "End marker at: " + xEnd);
        }
    }

    public void enablePlay(){
        if(playbackOrRecording == PLAYBACK_MODE) {
            ctx.findViewById(R.id.btnPause).setVisibility(View.INVISIBLE);
            ctx.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
        }
    }

    public void swapViews(int[] toShow, int[] toHide) {
        for (int v : toShow) {
            View view = ctx.findViewById(v);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
        for (int v : toHide) {
            View view = ctx.findViewById(v);
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void cutAndUpdate(){
        //FIXME: currently restricting cuts to one per file
        if(mCutOp.hasCut()){
            SectionMarkers.clearMarkers();
            updateUI();
            Toast.makeText(ctx, "Cut is limited to 1 operation at this time",Toast.LENGTH_SHORT).show();
            return;
        }
        int start = SectionMarkers.getStartLocationMs();
        int end = SectionMarkers.getEndLocationMs();
        if(start < 0){
            Logger.e(this.toString(), "Tried to cut from a negative start: " + start);
            start = 0;
        } else if(end > WavPlayer.getDuration()){
            Logger.e(this.toString(), "Tried to cut from end: " + end + " which is greater than duration: " + WavPlayer.getDuration());
            end = WavPlayer.getDuration();
        }
        Logger.w(this.toString(), "Pushing cut to stack. Start is " + start + " End is " + end);
        mCutOp.cut(start, end);
        Logger.w(UIDataManager.class.toString(), "Cutting from " + start + " to " + end);
        SectionMarkers.clearMarkers();
        Logger.w(this.toString(), "Reinitializing minimap");
        minimap.init(wavVis.getMinimap(minimap.getHeight()));
        minimap.setAudioLength(WavPlayer.getDuration() - mCutOp.getSizeCut());
        Logger.w(this.toString(), "Updating UI after cut");
        updateUI();
    }

    public void writeCut(File to, ProgressDialog pd) throws IOException {
        Logger.w(this.toString(), "Rewriting file to disk due to cuts");
        pd.setProgress(0);

        FileOutputStream fis = new FileOutputStream(to);
        for(int i = 0; i < AudioInfo.HEADER_SIZE; i++){
            fis.write(mappedAudioFile.get(i));
        }
        for(int i = 0; i < buffer.capacity(); i++){
            int skip = mCutOp.skipLoc(i, false);
            if(skip != -1){
                i = skip;
            }
            fis.write(buffer.get(i));
            pd.setProgress((int)Math.round(i/(double)(buffer.capacity()) * 100));
        }
        mCutOp.clear();

        return;
    }

    public boolean hasCut(){
        return mCutOp.hasCut();
    }

    public void loadWavFromFile(String path){
        Logger.w(this.toString(), "Loading wav from file: " + path);
        wavLoader = new WavFileLoader(path, mainWave.getWidth(), isALoadedFile);
        buffer = wavLoader.getMappedFile();
        preprocessedBuffer = wavLoader.getMappedCacheFile();
        mappedAudioFile = wavLoader.getMappedAudioFile();
        if(buffer == null){
            Logger.e(UIDataManager.class.toString(), "Buffer is null.");
        }
        if(preprocessedBuffer == null){
            Logger.w(UIDataManager.class.toString(), "Visualization buffer is null.");
        }
        Logger.w(UIDataManager.class.toString(), "MainWave height: " + mainWave.getHeight() + " width: " + mainWave.getWidth());
        WavPlayer.loadFile(getMappedAudioFile());
        Logger.w(UIDataManager.class.toString(), "Loaded file duration in ms is: " + WavPlayer.getDuration());
        minimap.setAudioLength(WavPlayer.getDuration());
        Logger.w(this.toString(), "Setting up visualizer");
        wavVis = new WavVisualizer(buffer, preprocessedBuffer, mainWave.getWidth(), mainWave.getHeight(), mCutOp);
        minimap.init(wavVis.getMinimap(minimap.getHeight()));
//        wavVis = new WavVisualizer(buffer, preprocessedBuffer, mainWave.getMeasuredWidth(), mainWave.getMeasuredHeight());
    }

    public int timeToScreenSpace(int markerTimeMs, int timeAtPlaybackLineMs, double mspp){
        //Logger.w(this.toString(), "Time differential is " + (markerTimeMs - timeAtPlaybackLineMs));
        //Logger.w(this.toString(), "mspp is " + mspp);
        return (int)Math.round((-markerTimeMs + timeAtPlaybackLineMs) / mspp);

    }

    //NOTE: software architecture will only allow one instance of this at a time, do not declare multiple
    //canvas views to listen for recording on the same activity
    public void listenForRecording(final boolean onlyVolumeTest){
        mainWave.setDrawingFromBuffer(true);
        ctx.findViewById(R.id.volumeBar).setVisibility(View.VISIBLE);
        ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped = false;
                boolean isPaused = false;
                double maxDB = 0;
                long timeDelay = System.currentTimeMillis();
                while (!isStopped) {
                    try {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();

                        if (!isPaused && message.getData() != null) {
                            byte[] buffer = message.getData();
                            double max = getPeakVolume(buffer);
                            double db = U.computeDb(max);
                            if(db > maxDB && ((System.currentTimeMillis() - timeDelay) < 1500)){
                                VolumeMeter.changeVolumeBar(ctx, db);
                                maxDB = db;
                            }
                            else if(((System.currentTimeMillis() - timeDelay) > 1500)){
                                VolumeMeter.changeVolumeBar(ctx, db);
                                maxDB = db;
                                timeDelay = System.currentTimeMillis();
                            }

                            if(isRecording) {
                                mainWave.setBuffer(buffer);
                                mainWave.postInvalidate();
                                if(timer != null) {
                                    long t = timer.getTimeElapsed();
                                    final String time = String.format("%02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
                                    ctx.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            timerView.setText(time);
                                            timerView.invalidate();
                                        }
                                    });
                                }
                            //if only running the volume meter, the queues need to be emptied
                            } else if(onlyVolumeTest) {
                                RecordingQueues.writingQueue.clear();
                                RecordingQueues.compressionQueue.clear();
                            }
                        }
                        if (isStopped) {
                            mainWave.setBuffer(null);
                            mainWave.postInvalidate();
                            RecordingQueues.doneUI.put(new Boolean(true));
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mainWave.setDrawingFromBuffer(false);
            }
        });
        uiThread.setName("UIThread");
        uiThread.start();
    }

    public double getPeakVolume(byte[] buffer){
        double max = 0;
        for(int i =0; i < buffer.length; i+=2) {
            byte low = buffer[i];
            byte hi = buffer[i + 1];
            short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
            max = (Math.abs(value) > max)? Math.abs(value) : max;
        }
        return max;
    }

    public void drawWaveformDuringPlayback(int location){
        mainWave.setDrawingFromBuffer(false);
        float[] samples = null;
        mainWave.setMarkerToDrawStart(SectionMarkers.getStartLocationMs());
        mainWave.setMarkerToDrawEnd(SectionMarkers.getEndLocationMs());
        //Scaling should be based on db levels anyway?
        samples = wavVis.getDataToDraw(location);
        mainWave.setIsDoneDrawing(false);
        mainWave.setWaveformDataForPlayback(samples);
        mainWave.postInvalidate();
    }

    public void undoCut(){
        Logger.w(this.toString(), "Popping off last cut");
        mCutOp.undo();
        Logger.w(this.toString(), "Recomputing minimap");
        minimap.init(wavVis.getMinimap(minimap.getHeight()));
        minimap.setAudioLength(WavPlayer.getDuration() - mCutOp.getSizeCut());
        updateUI();
    }
}
