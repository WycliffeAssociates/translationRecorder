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
    float increment;
    int tid;

    public VisualizerRunnable(){

    }

    public VisualizerRunnable newState(int start, int end, BlockingQueue<Integer> response, AudioFileAccessor accessor, float[] samples, int index, int screenHeight, int startPosition, float increment, int tid){
        mStart = start;
        mEnd = end;
        mResponse = response;
        mAccessor = accessor;
        mSamples = samples;
        mIndex = start * 4;
        mScreenHeight = screenHeight;
        this.startPosition = startPosition;
        this.increment = increment;
        this.tid = tid;
        return this;
    }

    @Override
    public void run() {
        boolean wroteData = false;
        boolean resetIncrementNextIteration = false;
        float offset = 0;
        int iterations = 0;
        long startMs = 0;
        long endMs = 0;
        long sum = 0;

        for(int i = mStart; i < mEnd; i++){
            //startMs = System.nanoTime();
            if(startPosition > mAccessor.size(tid)){
                break;
            }
            mIndex = Math.max(WavVisualizer.addHighAndLowToDrawingArray(mAccessor, mSamples, startPosition, startPosition+(int)increment, mIndex, mScreenHeight, tid), mIndex);
            startPosition += Math.floor(increment);
            if(resetIncrementNextIteration){
                resetIncrementNextIteration = false;
                increment--;
                offset--;
            }
            if(offset > 1.0) {
                increment++;
                resetIncrementNextIteration = true;
            }
            offset += increment - Math.floor(increment);

//            count += leftover;
//            if(addedLeftover){
//                addedLeftover = false;
//                increment = (mUseCompressedFile)? increment - 0 : increment;
//            }
            wroteData = true;
            //endMs = System.nanoTime();
            sum += (endMs - startMs);
            iterations++;
        }
        //System.out.println("Average iteration took: " + (sum/(double)iterations) + " ns");
        try {
            mResponse.put((wroteData)? mIndex : 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }
}