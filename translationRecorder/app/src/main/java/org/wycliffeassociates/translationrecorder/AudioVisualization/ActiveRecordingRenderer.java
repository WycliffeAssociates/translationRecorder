package org.wycliffeassociates.translationrecorder.AudioVisualization;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.AudioVisualization.Utils.U;
import org.wycliffeassociates.translationrecorder.Recording.RecordingMessage;
import org.wycliffeassociates.translationrecorder.Recording.RecordingQueues;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingControls;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingWaveform;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentVolumeBar;
import org.wycliffeassociates.translationrecorder.utilities.RingBuffer;

/**
 * Created by sarabiaj on 9/10/2015.
 */

public class ActiveRecordingRenderer {

    private FragmentRecordingControls mFragmentRecordingControls;
    private FragmentVolumeBar mFragmentVolumeBar;
    private FragmentRecordingWaveform mFragmentRecordingWaveform;
    private int mCanvasHeight;

    public static int NUM_SECONDS_ON_SCREEN = 10;

    public ActiveRecordingRenderer(FragmentRecordingControls timer, FragmentVolumeBar volume, FragmentRecordingWaveform waveform) {
        mFragmentRecordingControls = timer;
        mFragmentVolumeBar = volume;
        mFragmentRecordingWaveform = waveform;
    }

    //NOTE: software architecture will only allow one instance of this at a time, do not declare multiple
    //canvas views to listen for recording on the same activity
    public void listenForRecording(final boolean onlyVolumeTest){
        if(!onlyVolumeTest) {
            mFragmentRecordingWaveform.setDrawingFromBuffer(true);
            //initializeRingBuffer();
            mCanvasHeight = mFragmentRecordingWaveform.getHeight();
        }
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RingBuffer ringBuffer = null;
                if(!onlyVolumeTest) {
                    ringBuffer = new RingBuffer(mFragmentRecordingWaveform.getWidth());
                }
                VisualizerCompressor visualizerCompressor = new VisualizerCompressor(NUM_SECONDS_ON_SCREEN, ringBuffer);
                boolean isStopped = false;
                boolean isPaused = false;
                double maxDB = 0;
                long volumeBarDelay = System.currentTimeMillis();
                long waveformDelay = System.currentTimeMillis();
                try {
                    while (!isStopped) {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();

                        if (!isPaused && message.getData() != null) {
                            byte[] buffer = message.getData();
                            double max = getPeakVolume(buffer);
                            double db = Math.abs(max);
                            if (db > maxDB && ((System.currentTimeMillis() - volumeBarDelay) < 60)) {
                                maxDB = db;
                                if(mFragmentVolumeBar != null) {
                                    mFragmentVolumeBar.updateDb((int) maxDB);
                                }
                            } else if (((System.currentTimeMillis() - volumeBarDelay) > 60)) {
                                volumeBarDelay = System.currentTimeMillis();
                                maxDB = db;
                                if(mFragmentVolumeBar != null) {
                                    mFragmentVolumeBar.updateDb((int) maxDB);
                                }
                            }
                            if (!onlyVolumeTest) {
                                for(int i = 0; i < buffer.length; i+=2) {
                                    byte low = buffer[i];
                                    byte hi = buffer[i+1];
                                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                                    visualizerCompressor.add(value);
                                }
                                if((System.currentTimeMillis() - waveformDelay) > 12){
                                    waveformDelay = System.currentTimeMillis();
                                    mFragmentRecordingWaveform.updateWaveform(ringBuffer.getArray());
                                    if(mFragmentRecordingControls != null) {
                                        mFragmentRecordingControls.updateTime();
                                    }
                                }
                                //if only running the volume meter, the queues need to be emptied
                            } else {
                                mFragmentRecordingWaveform.updateWaveform(null);
                                RecordingQueues.writingQueue.clear();
                                RecordingQueues.compressionWriterQueue.clear();
                                RecordingQueues.compressionQueue.clear();
                            }
                        }
                        if (isStopped) {
                            mFragmentRecordingWaveform.updateWaveform(null);
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

                mFragmentRecordingWaveform.setDrawingFromBuffer(false);
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

    private class VisualizerCompressor {

        RingBuffer mRingBuffer;
        float[] accumulator;
        int index = 0;

        public VisualizerCompressor(int numSecondsOnScreen, RingBuffer ringBuffer) {
            accumulator = new float[numSecondsOnScreen * 2];
            mRingBuffer = ringBuffer;
        }

        public void add(float[] data) {
            for(float sample : data) {
                if(index >= accumulator.length){
                    sendDataToRingBuffer();
                    index = 0;
                }
                accumulator[index] = sample;
                index++;
            }
        }

        public void add(float data) {
            if(index >= accumulator.length){
                sendDataToRingBuffer();
                index = 0;
            }
            accumulator[index] = data;
            index++;
        }

        private void sendDataToRingBuffer(){
            if(mRingBuffer == null) {
                return;
            }
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for(float sample : accumulator) {
                max = (max < sample)? sample : max;
                min = (min > sample)? sample : min;
            }
            mRingBuffer.add(U.getValueForScreen(min, mCanvasHeight));
            mRingBuffer.add(U.getValueForScreen(max, mCanvasHeight));
        }
    }
}
