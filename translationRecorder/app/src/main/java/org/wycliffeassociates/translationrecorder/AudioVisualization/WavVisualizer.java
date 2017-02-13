package org.wycliffeassociates.translationrecorder.AudioVisualization;

import org.wycliffeassociates.translationrecorder.AudioInfo;
import org.wycliffeassociates.translationrecorder.AudioVisualization.Utils.U;
import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;

import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WavVisualizer {

    private ShortBuffer mCompressed;
    private ShortBuffer buffer;
    private float mUserScale = 1f;
    private final int mDefaultFramesOnScreen = 441000;
    public static int mNumFramesOnScreen;
    private boolean mUseCompressedFile = false;
    private boolean mCanSwitch = false;
    private float[] mSamples;
    private float[] mMinimap;
    int mScreenHeight;
    int mScreenWidth;
    AudioFileAccessor mAccessor;

    ThreadPoolExecutor mThreads;
    ArrayBlockingQueue<Integer>[] mThreadResponse;
    VisualizerRunnable[] mRunnable;

    int mNumThreads = 4;


    public WavVisualizer(ShortBuffer buffer, ShortBuffer compressed, int numThreads, int screenWidth, int screenHeight, int minimapWidth, CutOp cut) {
        this.buffer = buffer;
        mScreenHeight = screenHeight;
        mScreenWidth = screenWidth;
        mCompressed = compressed;
        mNumFramesOnScreen = mDefaultFramesOnScreen;
        mCanSwitch = (compressed == null)? false : true;
        mSamples = new float[screenWidth*4];
        mAccessor = new AudioFileAccessor(compressed, buffer, cut);
        mMinimap = new float[minimapWidth * 4];
        mNumThreads = numThreads;
        mThreads = new ThreadPoolExecutor(numThreads, mNumThreads, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(mNumThreads));
        mThreads.allowCoreThreadTimeOut(true);
        mThreadResponse = new ArrayBlockingQueue[mNumThreads];
        mRunnable = new VisualizerRunnable[mNumThreads];
        for(int i = 0; i < mNumThreads; i++){
            mThreadResponse[i] = new ArrayBlockingQueue<Integer>(1);
            mRunnable[i] = new VisualizerRunnable();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mThreads.shutdown();
        mThreads.purge();
    }

    public void enableCompressedFileNextDraw(ShortBuffer compressed){
        //System.out.println("Swapping buffers now");
        mCompressed = compressed;
        mAccessor.setCompressed(compressed);
        mCanSwitch = true;
    }

    public float[] getMinimap(int minimapHeight, int minimapWidth, int durationMs){
        //selects the proper buffer to use
        boolean useCompressed = mCanSwitch && mNumFramesOnScreen > AudioInfo.COMPRESSED_FRAMES_ON_SCREEN;
        mAccessor.switchBuffers(useCompressed);

        int pos = 0;
        int index = 0;
        double seconds = durationMs / (double) 1000;

        double incrementTemp = mAccessor.getIncrement(seconds, useCompressed, durationMs, minimapWidth);
        double leftover = incrementTemp - (int)Math.floor(incrementTemp);
        double count = 0;
        int increment = (int)Math.floor(incrementTemp);
//        if(useCompressed){
//            increment*=2;
//        }
        boolean leapedInc = false;
        for(int i = 0; i < minimapWidth; i++){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            if(count > 1){
                count-=1;
                increment++;
                leapedInc = true;
            }
            for(int j = 0; j < increment; j++){
                if(pos >= mAccessor.size()){
                    break;
                }
                short value = mAccessor.get(pos);
                max = (max < (double) value) ? value : max;
                min = (min > (double) value) ? value : min;
                pos++;
            }
            if(leapedInc){
                increment--;
                leapedInc = false;
            }
            count += leftover;
            mMinimap[index] = index/4;
            mMinimap[index+1] = U.getValueForScreen(max, minimapHeight);
            mMinimap[index+2] =  index/4;
            mMinimap[index+3] = U.getValueForScreen(min, minimapHeight);
            index+=4;
        }
        //System.out.print("height is " + minimapHeight);

        return mMinimap;
    }

    public float[] getDataToDraw(int frame){

       // long start = System.currentTimeMillis();
        mNumFramesOnScreen = computeNumFramesOnScreen(mUserScale);
        //based on the user scale, determine which buffer waveData should be
        mUseCompressedFile = shouldUseCompressedFile(mNumFramesOnScreen);
        mAccessor.switchBuffers(mUseCompressedFile);

        //get the number of array indices to skip over- the array will likely contain more data than one pixel can show
        float increment = getIncrement(mNumFramesOnScreen);
        int framesToSubtract = framesBeforePlaybackLine(mNumFramesOnScreen);
        int locAndTime[] = mAccessor.indexAfterSubtractingFrame(framesToSubtract, frame);
        int startPosition = locAndTime[0];
        int newTime = locAndTime[1];
        int index = initializeSamples(mSamples, startPosition, newTime);
        //in the event that the actual start position ends up being negative (such as from shifting forward due to playback being at the start of the file)
        //it should be set to zero (and the buffer will already be initialized with some zeros, with index being the index of where to resume placing data
        startPosition = Math.max(0, startPosition);
        int end = mSamples.length/4;

        //beginning with the starting position, the width of each increment represents the data one pixel width is showing
//        double leftover = getIncrementLeftover(mNumSecondsOnScreen);
//        double count = 0;
//        boolean addedLeftover = false;

        int iterations = end - index/4;
        int rangePerThread = iterations / mNumThreads;

//        //zero out the rest of the array
//        for (int i = index; i < mSamples.length; i++){
//            mSamples[i] = 0;
//        }

        for(int i = 0; i < mThreadResponse.length; i++){
            mThreads.submit(mRunnable[i].newState(
                    index/4 + (rangePerThread * i),
                    index/4 + (rangePerThread * (i+1)),
                    mThreadResponse[i],
                    mAccessor,
                    mSamples,
                    index + ((rangePerThread * mNumThreads) * i),
                    mScreenHeight,
                    startPosition + (int)((increment) * (rangePerThread * i)),
                    increment,
                    i
            ));
        }

        for(int i = 0; i < mThreadResponse.length; i++){
            try {
                int returnIdx = mThreadResponse[i].take();
                index = Math.max(returnIdx, index);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //zero out the rest of the array
        for (int i = index; i < mSamples.length; i++){
            mSamples[i] = 0;
        }

        //long stop = System.currentTimeMillis();

       //System.out.println("Took " + (stop-start) + "ms to generate the array in parallel");

        return mSamples;
    }

//    private int backUpStartPos(int start, int delta, CutOp cut){
//        int loc = start;
//        for(int i = 0; i < delta; i++){
//            loc--;
//            int skip = cut.skipReverse(loc);
//            if(skip != Integer.MAX_VALUE){
//                loc = skip;
//            }
//        }
//        return loc;
//    }
//
//    private int mapLocationToTime(int idx){
//        double idxP = (mUseCompressedFile)? idx/(double)mCompressed.capacity()
//                : idx/(double)buffer.capacity();
//        int ms = (int)Math.round(idxP * mManager.getRelativeDurationMs());
//        return ms;
//    }


    public static int addHighAndLowToDrawingArray(AudioFileAccessor accessor, float[] samples, int beginIdx, int endIdx, int index, int screenHeight){

        boolean addedVal = false;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        //loop over the indicated chunk of data to extract out the high and low in that section, then store it in samples
        for(int i = beginIdx; i < Math.min(accessor.size(), endIdx); i++){
            short value = accessor.get(i);
            max = (max < (double) value) ? value : max;
            min = (min > (double) value) ? value : min;
        }
        if(samples.length > index+4){
            samples[index] = index/4;
            samples[index+1] = U.getValueForScreen(max, screenHeight);
            samples[index+2] =  index/4;
            samples[index+3] = U.getValueForScreen(min, screenHeight);
            index+=4;
            addedVal = true;
        }

        //returns the end of relevant data in the buffer
        return (addedVal)? index : 0;
    }

    private int initializeSamples(float[] samples, int startPosition, int framesUntilZero){
        if(startPosition <= 0) {
            int numberOfZeros = 0;
            if(framesUntilZero < 0){
                framesUntilZero *= -1;
                double fpp = (mNumFramesOnScreen) / (double)mScreenWidth;
                numberOfZeros = (int)Math.round(framesUntilZero/fpp);
            }
            int index = 0;
            for (int i = 0; i < numberOfZeros; i++) {
                samples[index] = index/4;
                samples[index+1] = 0;
                samples[index+2] =  index/4;
                samples[index+3] = 0;
                index+=4;
            }
            return index;
        }
        return 0;
    }

    public boolean shouldUseCompressedFile(int numFramesOnScreen){
        if(numFramesOnScreen >= AudioInfo.COMPRESSED_FRAMES_ON_SCREEN && mCanSwitch){
            return true;
        }
        else return false;
    }

    private int framesBeforePlaybackLine(int numFramesOnScreen){
        int pixelsBeforeLine = (mScreenWidth/8);
        double fpp = (numFramesOnScreen) / (double)mScreenWidth;
        return (int)Math.round(fpp * pixelsBeforeLine);
    }

    private int computeSampleStartPosition(int startFrame){
//        int seconds = startMillisecond/1000;
//        int ms = (startMillisecond-(seconds*1000));
//        int tens = (int)Math.round(ms/10.0);
//
//        int sampleStartPosition = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        if(mUseCompressedFile){
            startFrame /= 25;
        }
        return startFrame;
    }

    private float getIncrement(int numFramesOnScreen){
        float increment = (int)( numFramesOnScreen / (float)mScreenWidth);
        if(mUseCompressedFile) {
            increment /= 25.0;
        }
//        if(mUseCompressedFile){
//            increment *= 1;
//        } else {
//            increment *= 1;
//        }
        //System.out.println("increment is " + increment);
        return increment;
    }

//    private double getIncrementLeftover(int numSecondsOnScreen){
//        double increment = (int)(numSecondsOnScreen * AudioInfo.SAMPLERATE / (float)mScreenWidth);
//        if(mUseCompressedFile) {
//            increment /= 25.d;
//        }
//        double diff = increment-Math.floor(increment);
//        return diff;
//    }
//
//    private int getLastIndex(int startMillisecond, int numSecondsOnScreen) {
//        int endMillisecond = startMillisecond + (numSecondsOnScreen) * 1000;
//        return computeSampleStartPosition(endMillisecond);
//    }

//    private int getNumSecondsOnScreen(float userScale){
//        int numSecondsOnScreen = (int)Math.round(mDefaultSecondsOnScreen * userScale);
//        return Math.max(numSecondsOnScreen, 1);
//    }

    private int computeNumFramesOnScreen(float userScale) {
        int numSecondsOnScreen = Math.round(mNumFramesOnScreen * userScale);
        return Math.max(numSecondsOnScreen, AudioInfo.COMPRESSED_SECONDS_ON_SCREEN);
    }

//    public double millisecondsPerPixel(){
//        return mNumSecondsOnScreen * 1000/(double)mScreenWidth;
//    }


//    private ShortBuffer selectBufferToUse(boolean useCompressedFile){
//        if (useCompressedFile){
//            return mCompressed;
//        }
//        else
//            return buffer;
//    }
//
//    private int computeSpaceToAllocateForSamples(int startPosition, int endPosition, int increment){
//        //the 2 is to give a little extra room, and the 4 is to account for x1, y1, x2, y2 for each
//        return Math.abs(((endPosition+2*increment)-startPosition)) * 4;
//    }
//
//    private int mapTimeToClosestSecond(int location){
//        return (int)Math.round((double)location/(double)1000);
//    }
}