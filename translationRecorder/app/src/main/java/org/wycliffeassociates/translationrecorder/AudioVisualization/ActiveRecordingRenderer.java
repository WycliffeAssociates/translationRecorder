package org.wycliffeassociates.translationrecorder.AudioVisualization;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.Recording.RecordingMessage;
import org.wycliffeassociates.translationrecorder.Recording.RecordingQueues;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;

/**
 * Created by sarabiaj on 9/10/2015.
 */

public class ActiveRecordingRenderer {

    private final WaveformView mainWave;
    private final VolumeBar mVolume;
    private final TextView mTimerView;
    private Handler mHandler;
    private boolean isRecording;
    RecordingTimer timer;

    public ActiveRecordingRenderer(WaveformView mainWave, VolumeBar volume, TextView timerView){
        mainWave.setUIDataManager(this);
        this.mainWave = mainWave;
        mVolume = volume;
        mTimerView = timerView;
        mHandler = new Handler(Looper.getMainLooper());
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

    public void setIsRecording(boolean isRecording){
        this.isRecording = isRecording;
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
                                timeDelay = System.currentTimeMillis();
                            } else if (((System.currentTimeMillis() - timeDelay) > 33)) {
                                maxDB = db;
                                mVolume.setDb((int) maxDB);
                                mVolume.postInvalidate();
                                timeDelay = System.currentTimeMillis();
                            }
                            if (isRecording) {
                                mainWave.setBuffer(buffer);
                                mainWave.postInvalidate();
                                if (timer != null) {
                                    long t = timer.getTimeElapsed();
                                    final String time = String.format("%02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTimerView.setText(time);
                                            mTimerView.invalidate();
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
}
