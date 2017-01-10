package org.wycliffeassociates.translationrecorder.AudioVisualization;

import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 12/20/2016.
 */

public class VisualizerRunnable implements Runnable {

    int mStart;
    int mEnd;
    int mUseCompressedFile;
    int startPosition;
    int index;
    float[] mSamples;
    AudioFileAccessor mAccessor;
    BlockingQueue<Integer> mResponse;
    int mIndex;
    int mScreenHeight;
    int increment;

    public VisualizerRunnable(){

    }

    public VisualizerRunnable newState(int start, int end, BlockingQueue<Integer> response, AudioFileAccessor accessor, float[] samples, int index, int screenHeight, int startPosition, int increment){
        mStart = start;
        mEnd = end;
        mResponse = response;
        mAccessor = accessor;
        mSamples = samples;
        mIndex = index;
        mScreenHeight = screenHeight;
        this.startPosition = startPosition;
        this.increment = increment;
        return this;
    }

    @Override
    public void run() {
        for(int i = mStart; i < mEnd; i++){
//            if(count > 1){
//                increment = (mUseCompressedFile)? increment + 0 : increment;
//                count--;
//                addedLeftover = true;
//            }
            if(startPosition+increment > mAccessor.size()){
                break;
            }
            mIndex = WavVisualizer.addHighAndLowToDrawingArray(mAccessor, mSamples, startPosition, startPosition+(int)increment, mIndex, mScreenHeight);
            startPosition += increment;
//            count += leftover;
//            if(addedLeftover){
//                addedLeftover = false;
//                increment = (mUseCompressedFile)? increment - 0 : increment;
//            }
        }
        try {
            mResponse.put(mIndex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }
}