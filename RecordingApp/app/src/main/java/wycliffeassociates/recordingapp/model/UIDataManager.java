package wycliffeassociates.recordingapp.model;

import android.app.Activity;
import android.util.Pair;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.CanvasView;
import wycliffeassociates.recordingapp.MinimapView;
import wycliffeassociates.recordingapp.RecordingMessage;
import wycliffeassociates.recordingapp.RecordingQueues;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.WavPlayer;
import wycliffeassociates.recordingapp.WavVisualizer;
import wycliffeassociates.recordingapp.WaveformView;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class UIDataManager {

    private int largest;
    private final WaveformView mainWave;
    private final MinimapView minimap;
    private final Activity ctx;
    private boolean isRecording;
    public static Semaphore lock;
    private WavFileLoader wavLoader;
    private WavVisualizer wavVis;
    private MappedByteBuffer buffer;
    private MappedByteBuffer preprocessedBuffer;

    public UIDataManager(WaveformView mainWave, MinimapView minimap, Activity ctx){
        this.mainWave = mainWave;
        this.minimap = minimap;
        this.ctx = ctx;
        lock = new Semaphore(1);
    }

    public void setIsRecording(boolean isRecording){
        this.isRecording = isRecording;
    }

    public void loadWavFromFile(String path){
        try {
            wavLoader = new WavFileLoader(path, mainWave.getWidth());
            buffer = wavLoader.getMappedFile();
            preprocessedBuffer = wavLoader.getMappedCacheFile();
            largest = wavLoader.getLargest();
            System.out.println("Mapped files completed.");
        } catch (IOException e) {
            System.out.println("There was an error with mapping the files");
            e.printStackTrace();
        }
        wavVis = new WavVisualizer(buffer, preprocessedBuffer);
    }
    //NOTE: Only one instance of canvas view can call this; otherwise two threads will be pulling from the same queue!!
    public void listenForRecording(){
        mainWave.setDrawingFromBuffer(true);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped = false;
                boolean isPaused = false;
                while (!isStopped) {
                    try {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();

                        if (!isPaused && message.getData() != null) {
                            byte[] buffer = message.getData();
                            lock.acquire();
                            mainWave.setBuffer(buffer);
                            lock.release();
                            double max = getPeakVolume(buffer);
                            final double db = computeDB(max);
                            //System.out.println("db is "+db);
                            if(isRecording) {
                                mainWave.postInvalidate();
                            }
                            //changeVolumeBar(ctx, db);
                        }
                        if (isStopped) {
                            mainWave.setBuffer(null);
                            mainWave.postInvalidate();
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mainWave.setDrawingFromBuffer(false);
            }
        });
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

    public double computeDB(double value){
        return  Math.log10(value / (double) AudioInfo.AMPLITUDE_RANGE)* 20;
    }

    public void drawWaveformDuringPlayback(int location){
        mainWave.setDrawingFromBuffer(false);
        ArrayList<Pair<Double, Double>> samples = wavVis.getDataToDraw(location, mainWave.getWidth(), mainWave.getHeight(), largest);
        mainWave.setWaveformDataForPlayback(samples);
        mainWave.postInvalidate();
    }
}
