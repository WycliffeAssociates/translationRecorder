package wycliffeassociates.recordingapp.AudioVisualization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.MarkerView;
import wycliffeassociates.recordingapp.Playback.player.BufferPlayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingMessage;
import wycliffeassociates.recordingapp.Recording.RecordingQueues;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.wav.WavFile;
import wycliffeassociates.recordingapp.wav.WavMetadata;
import wycliffeassociates.recordingapp.wav.WavOutputStream;

/**
 * Created by sarabiaj on 9/10/2015.
 */

public class UIDataManager {

    static public final boolean PLAYBACK_MODE = true;
    static public final boolean RECORDING_MODE = false;
    private final WaveformView mainWave;
    private final VolumeBar mVolume;
    private final MinimapView minimap;
    private final Activity ctx;
    private final MarkerView mStartMarker;
    private final MarkerView mEndMarker;
    private boolean isRecording;
    private WavFileLoader mWavLoader;
    private WavVisualizer wavVis;
    private MappedByteBuffer buffer;
    private MappedByteBuffer mappedAudioFile;
    private MappedByteBuffer preprocessedBuffer;
    RecordingTimer timer;
    private final TextView timerView;
    private TextView durationView;
    private boolean playbackOrRecording;
    private CutOp mCutOp;
    private BufferPlayer mPlayer;

    public UIDataManager(WaveformView mainWave, MinimapView minimap, MarkerView start, MarkerView end, Activity ctx, boolean playbackOrRecording) {
        this(mainWave, minimap, null, start, end, ctx, playbackOrRecording);
    }

    public UIDataManager(WaveformView mainWave, MinimapView minimap, VolumeBar volume, MarkerView start, MarkerView end, Activity ctx, boolean playbackOrRecording){
        this.playbackOrRecording = playbackOrRecording;
        Logger.w(UIDataManager.class.toString(), "Playback mode: " + playbackOrRecording);
        mainWave.setUIDataManager(this);
        minimap.setUIDataManager(this);
        this.mainWave = mainWave;
        this.minimap = minimap;
        mVolume = volume;
        this.mStartMarker = start;
        this.mEndMarker = end;
        if(mEndMarker != null){
            Logger.w(this.toString(), "Connecting UImanager to marker views");
            start.setManager(this);
            end.setManager(this);
        }
        timerView = (TextView)ctx.findViewById(R.id.timer_view);
        this.ctx = ctx;

        Logger.w(this.toString(), "passing cut to BufferPlayer and Canvases");
        mCutOp = new CutOp();
        mainWave.setCut(mCutOp);
        minimap.setCut(mCutOp);

        timer = new RecordingTimer();
    }

    public int getMinimapWidth(){
        return minimap.getWidth();
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
        if(minimap == null || mainWave == null || mPlayer == null || mPlayer.getDuration() == 0){
            //System.out.println("Update UI is returning early because either minimap, mainView, or Wavplayer.getDuration() is null/0");
            return;
        }
        if(mWavLoader != null && mWavLoader.visFileLoaded()){
            wavVis.enableCompressedFileNextDraw(mWavLoader.getMappedCacheFile());
        }
        //Marker is set to the percentage of playback times the width of the minimap
        int location = mPlayer.getLocation();
        minimap.setMiniMarkerLoc((float) ((mCutOp.reverseTimeAdjusted(location) / ((double) mPlayer.getDuration() - mCutOp.getSizeCut())) * minimap.getWidth()));
        drawWaveformDuringPlayback(location);
        mainWave.setTimeToDraw(location);
        int adjLoc = mPlayer.getAdjustedLocation();
        final String time = String.format("%02d:%02d:%02d", adjLoc / 3600000, (adjLoc / 60000) % 60, (adjLoc / 1000) % 60);
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerView.setText(time);
                timerView.invalidate();
            }
        });
        if(mStartMarker != null ){
            int xStart = timeToScreenSpace(mPlayer.getLocation(),
                    SectionMarkers.getStartLocationMs(), wavVis.millisecondsPerPixel());
            mStartMarker.setX(xStart - mStartMarker.getWidth() + (AudioInfo.SCREEN_WIDTH/8.f));
            int xEnd = timeToScreenSpace(mPlayer.getLocation(),
                    SectionMarkers.getEndLocationMs(), wavVis.millisecondsPerPixel());
            mEndMarker.setX(xEnd + (AudioInfo.SCREEN_WIDTH/8.f));
        }
    }

    public void enablePlay(){
        if(playbackOrRecording == PLAYBACK_MODE) {
            ctx.findViewById(R.id.btn_pause).setVisibility(View.INVISIBLE);
            ctx.findViewById(R.id.btn_play).setVisibility(View.VISIBLE);
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

        int start = SectionMarkers.getStartLocationMs();
        int end = SectionMarkers.getEndLocationMs();
        if(start < 0){
            Logger.e(this.toString(), "Tried to cut from a negative start: " + start);
            start = 0;
        } else if(end > mPlayer.getDuration()){
            Logger.e(this.toString(), "Tried to cut from end: " + end + " which is greater than duration: " + mPlayer.getDuration());
            end = mPlayer.getDuration();
        }
        Logger.w(this.toString(), "Pushing cut to stack. Start is " + start + " End is " + end);
        mCutOp.cut(start, end);
        Logger.w(UIDataManager.class.toString(), "Cutting from " + start + " to " + end);
        SectionMarkers.clearMarkers(this);
        Logger.w(this.toString(), "Reinitializing minimap");
        minimap.init(wavVis.getMinimap(minimap.getHeight(), minimap.getWidth()));
        minimap.setAudioLength(mPlayer.getDuration() - mCutOp.getSizeCut());
        Logger.w(this.toString(), "Updating UI after cut");
        setDurationView();
        updateUI();
    }

    public void writeCut(File to, final WavFile wavFile, ProgressDialog pd) throws IOException {
        Logger.w(this.toString(), "Rewriting file to disk due to cuts");
        pd.setProgress(0);
        WavMetadata metadata = wavFile.getMetadata();
        int audioLength = wavFile.getTotalAudioLength();
        WavFile toWav = new WavFile(to, metadata);

        try (WavOutputStream wos = new WavOutputStream(toWav, WavOutputStream.BUFFERED)) {
            int percent = (int)Math.round((buffer.capacity()-mCutOp.getSizeCut()) /100.0);
            int count = percent;
            //may need to be -1?
            for(int i = 0; i < audioLength; i++){
                int skip = mCutOp.skipLoc(i, false);
                if(skip != -1){
                    i = skip;
                }
                //ensure no mis-alignment or out of bounds as a result of the skip
                if(i >= audioLength){
                    if(i%2 != 0 && i-1 < audioLength){
                        i--;
                    } else {
                        break;
                    }
                }
                wos.write(buffer.get(i));
                if(count <= 0) {
                    pd.incrementProgressBy(1);
                    count = percent;
                }
                count--;
            }
            mCutOp.clear();
        }
        return;
    }

    public boolean hasCut(){
        return mCutOp.hasCut();
    }

    public void loadWavFromFile(String path){
        Logger.w(this.toString(), "Loading wav from file: " + path);
        //mWavLoader = new WavFileLoader(path);
        configure();
    }

    public void loadWavFile(WavFile wavFile){
        mWavLoader = new WavFileLoader(wavFile);
        configure();
    }

    private void configure(){
        buffer = mWavLoader.getMappedFile();
        preprocessedBuffer = mWavLoader.getMappedCacheFile();
        mappedAudioFile = mWavLoader.getMappedAudioFile();
        if(buffer == null){
            Logger.e(UIDataManager.class.toString(), "Buffer is null.");
        }
        if(preprocessedBuffer == null){
            Logger.w(UIDataManager.class.toString(), "Visualization buffer is null.");
        }
        Logger.w(UIDataManager.class.toString(), "MainWave height: " + mainWave.getHeight() + " width: " + mainWave.getWidth());
        //mPlayer = new BufferPlayer(getMappedAudioFile(), mCutOp);
        Logger.w(UIDataManager.class.toString(), "Loaded file duration in ms is: " + mPlayer.getDuration());
        minimap.setAudioLength(mPlayer.getDuration());
        Logger.w(this.toString(), "Setting up visualizer");
        wavVis = new WavVisualizer(this, buffer, preprocessedBuffer, mainWave.getWidth(), mainWave.getHeight(), minimap.getWidth(), mCutOp);
        minimap.init(wavVis.getMinimap(minimap.getHeight(), minimap.getWidth()));
//        wavVis = new WavVisualizer(buffer, preprocessedBuffer, mainWave.getMeasuredWidth(), mainWave.getMeasuredHeight());
        durationView = (TextView)ctx.findViewById(R.id.duration_view);
        setDurationView();
    }

    private void setDurationView(){
        long t = mPlayer.getAdjustedDuration();
        final String time = String.format("/ %02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                durationView.setText(time);
                durationView.invalidate();
            }
        });
    }

    public int timeToScreenSpace(int markerTimeMs, int timeAtPlaybackLineMs, double mspp){
        return (int)Math.round((-mCutOp.reverseTimeAdjusted(markerTimeMs) + mCutOp.reverseTimeAdjusted(timeAtPlaybackLineMs)) / mspp);

    }

    //NOTE: software architecture will only allow one instance of this at a time, do not declare multiple
    //canvas views to listen for recording on the same activity
    public void listenForRecording(final boolean onlyVolumeTest){
        mainWave.setDrawingFromBuffer(true);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped = false;
                boolean isPaused = false;
                double maxDB = 0;
                long timeDelay = System.currentTimeMillis();
                try {
                    while (!isStopped) {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();

                        if (!isPaused && message.getData() != null) {
                            byte[] buffer = message.getData();
                            double max = getPeakVolume(buffer);
                            double db = Math.abs(max);
                            if (db > maxDB && ((System.currentTimeMillis() - timeDelay) < 33)) {
                                maxDB = db;
                                mVolume.setDb((int) maxDB);
                                mVolume.postInvalidate();
                                mainWave.resetFrameCount();
                                timeDelay = System.currentTimeMillis();
                            } else if (((System.currentTimeMillis() - timeDelay) > 33)) {
                                maxDB = db;
                                mVolume.setDb((int) maxDB);
                                mainWave.resetFrameCount();
                                mVolume.postInvalidate();
                                timeDelay = System.currentTimeMillis();
                            }
                            if (isRecording) {
                                mainWave.setBuffer(buffer);
                                mainWave.postInvalidate();
                                if (timer != null) {
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
                            } else if (onlyVolumeTest) {
                                mainWave.setBuffer(null);
                                RecordingQueues.writingQueue.clear();
                                RecordingQueues.compressionQueue.clear();
                            }
                        }
                        if (isStopped) {
                            mainWave.setBuffer(null);
                            mainWave.postInvalidate();
                            Logger.w(this.toString(), "UI thread received a stop message");
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    Logger.e(this.toString(), "Interruption exception in UI thread for recording", e);
                    e.printStackTrace();
                } finally {
                    try {
                        RecordingQueues.doneUI.put(new Boolean(true));
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "Interruption exception in finally of UI thread for recording", e);
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
        for(int i =0; i < buffer.length; i+=4) {
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
        minimap.init(wavVis.getMinimap(minimap.getHeight(), minimap.getWidth()));
        minimap.setAudioLength(mPlayer.getDuration() - mCutOp.getSizeCut());
        updateUI();
    }

    public int getAdjustedDuration(){
        return mPlayer.getAdjustedDuration();
    }

    public int getDuration(){
        return mPlayer.getDuration();
    }

    public int getLocation(){
        return mPlayer.getLocation();
    }

    public int getAdjustedLocation(){
        return mPlayer.getAdjustedLocation();
    }

//    public int getSelectionEnd(){
//        return mPlayer.getSelectionEnd();
//    }

    public void release(){
        mPlayer.release();
    }

    public void pause(boolean fromButtonPress){
        mPlayer.pause();
    }

    public void play(){
        mPlayer.play();
    }

    public void seekToEnd(){
        mPlayer.seekToEnd();
    }

    public void seekToStart(){
        mPlayer.seekToStart();
    }

    public void seekTo(int ms){
        mPlayer.seekTo(ms);
    }

    public boolean isPlaying(){
        if(mPlayer == null){
            return false;
        }
        return mPlayer.isPlaying();
    }

    public void startSectionAt(int ms){
        mPlayer.startSectionAt(ms);
    }

    public void stopSectionAt(int ms){
        mPlayer.stopSectionAt(ms);
    }

    public void setOnlyPlayingSection(boolean onlyPlayingSection){
        mPlayer.setOnlyPlayingSection(onlyPlayingSection);
    }

    public int timeAdjusted(int ms){
        return mCutOp.timeAdjusted(ms);
    }

    public int reverseTimeAdjusted(int ms){
        return mCutOp.reverseTimeAdjusted(ms);
    }

    public int skip(int ms){
        return mCutOp.skip(ms);
    }

    public int skipReverse(int ms){
        return mCutOp.skipReverse(ms);
    }

    public boolean checkIfShouldStop(){
        if(mPlayer == null){
            return true;
        }
        return mPlayer.checkIfShouldStop();
    }

    public double millisecondsPerPixel(){
        return wavVis.millisecondsPerPixel();
    }
}
