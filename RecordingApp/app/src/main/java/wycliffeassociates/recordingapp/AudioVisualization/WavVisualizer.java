package wycliffeassociates.recordingapp.AudioVisualization;

import java.nio.MappedByteBuffer;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.Utils.U;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

public class WavVisualizer {

    private MappedByteBuffer mCompressed;
    private MappedByteBuffer buffer;
    private float mUserScale = 1f;
    private final int mDefaultSecondsOnScreen = 10;
    public static int mNumSecondsOnScreen;
    private boolean mUseCompressedFile = false;
    private boolean mCanSwitch = false;
    private float[] mSamples;
    private float[] mMinimap;
    int mScreenHeight;
    int mScreenWidth;
    AudioFileAccessor mAccessor;
    UIDataManager mManager;

    public WavVisualizer(UIDataManager manager, MappedByteBuffer buffer, MappedByteBuffer compressed, int screenWidth, int screenHeight, CutOp cut) {
        this.buffer = buffer;
        mScreenHeight = screenHeight;
        mScreenWidth = screenWidth;
        mCompressed = compressed;
        mNumSecondsOnScreen = mDefaultSecondsOnScreen;
        mCanSwitch = (compressed == null)? false : true;
        mSamples = new float[screenWidth*8];
        mManager = manager;
        mAccessor = new AudioFileAccessor(compressed, buffer, cut, manager);
        mMinimap = new float[AudioInfo.SCREEN_WIDTH * 4];
    }

    public void enableCompressedFileNextDraw(MappedByteBuffer compressed){
        //System.out.println("Swapping buffers now");
        mCompressed = compressed;
        mAccessor.setCompressed(compressed);
        mCanSwitch = true;
    }

    public float[] getMinimap(int minimapHeight){
        //selects the proper buffer to use
        boolean useCompressed = mCanSwitch && mNumSecondsOnScreen > AudioInfo.COMPRESSED_SECONDS_ON_SCREEN;
        mAccessor.switchBuffers(useCompressed);

        int pos = 0;
        int index = 0;
        double incrementTemp = mAccessor.getIncrement(mManager.getAdjustedDuration()/(double)1000, useCompressed, mManager.getAdjustedDuration());
        double leftover = incrementTemp - (int)Math.floor(incrementTemp);
        double count = 0;
        int increment = (int)Math.floor(incrementTemp);
        boolean leapedInc = false;
        for(int i = 0; i < AudioInfo.SCREEN_WIDTH; i++){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            if(count > 1){
                count-=1;
                increment++;
                leapedInc = true;
            }
            for(int j = 0; j < 2*increment; j+=2){
                if(pos+1 >= mAccessor.size()){
                    break;
                }
                byte low = mAccessor.get(pos);
                byte hi = mAccessor.get(pos + 1);
                short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                max = (max < (double) value) ? value : max;
                min = (min > (double) value) ? value : min;
                pos+=2;
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



    public float[] getDataToDraw(int location){

        mNumSecondsOnScreen = getNumSecondsOnScreen(mUserScale);
        //based on the user scale, determine which buffer waveData should be
        mUseCompressedFile = shouldUseCompressedFile(mNumSecondsOnScreen);
        mAccessor.switchBuffers(mUseCompressedFile);

        //get the number of array indices to skip over- the array will likely contain more data than one pixel can show
        double increment = getIncrement(mNumSecondsOnScreen);
        int timeToSubtract = msBeforePlaybackLine(mNumSecondsOnScreen);
        int startPosition = mAccessor.indexAfterSubtractingTime(timeToSubtract, location, mNumSecondsOnScreen);

        int index = initializeSamples(mSamples, startPosition, increment);
        //in the event that the actual start position ends up being negative (such as from shifting forward due to playback being at the start of the file)
        //it should be set to zero (and the buffer will already be initialized with some zeros, with index being the index of where to resume placing data
        startPosition = Math.max(0, startPosition);
        int end = mSamples.length/4;

//        Log.i(this.toString(), "loc is " + location + " duration is " + WavPlayer.getDuration() + " adjusted loc is " + cut.timeAdjusted(location) + " duration without the cut " + (WavPlayer.getDuration() - cut.getSizeCut()));
//        Log.i(this.toString(), "start is " + startPosition);

        //beginning with the starting position, the width of each increment represents the data one pixel width is showing
        for(int i = index/4; i < end; i++){
            if(startPosition+increment > mAccessor.size()){
                break;
            }
            index = addHighAndLowToDrawingArray(mAccessor, mSamples, startPosition, startPosition+(int)increment, index);
            startPosition += increment;
        }
        //zero out the rest of the array
        for (int i = index; i < mSamples.length; i++){
            mSamples[i] = 0;
        }

        return mSamples;
    }

    private int backUpStartPos(int start, int delta, CutOp cut){
        int loc = start;
        for(int i = 0; i < delta; i++){
            loc--;
            int skip = cut.skipReverse(loc);
            if(skip != Integer.MAX_VALUE){
                loc = skip;
            }
        }
        return loc;
    }

    private int mapLocationToTime(int idx){
        double idxP = (mUseCompressedFile)? idx/(double)mCompressed.capacity()
                : idx/(double)buffer.capacity();
        int ms = (int)Math.round(idxP * mManager.getDuration());
        return ms;
    }


    private int addHighAndLowToDrawingArray(AudioFileAccessor accessor, float[] samples, int beginIdx, int endIdx, int index){

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        //loop over the indicated chunk of data to extract out the high and low in that section, then store it in samples
        for(int i = beginIdx; i < Math.min(mAccessor.size(), endIdx); i+= AudioInfo.SIZE_OF_SHORT){
            if((i+1) < accessor.size()) {
                byte low = accessor.get(i);
                byte hi = accessor.get(i + 1);
                short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                max = (max < (double) value) ? value : max;
                min = (min > (double) value) ? value : min;
            }
        }
        if(samples.length > index+4){
            samples[index] = index/4;
            samples[index+1] = U.getValueForScreen(max, mScreenHeight);
            samples[index+2] =  index/4;
            samples[index+3] = U.getValueForScreen(min, mScreenHeight);
            index+=4;
        }

        //returns the end of relevant data in the buffer
        return index;
    }

    private int initializeSamples(float[] samples, int startPosition, double increment){
        if(startPosition <= 0) {
            int numberOfZeros = (int)Math.round(Math.abs(startPosition) / ((double)increment));
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

    public boolean shouldUseCompressedFile(int numSecondsOnScreen){
        if(numSecondsOnScreen >= AudioInfo.COMPRESSED_SECONDS_ON_SCREEN && mCanSwitch){
            return true;
        }
        else return false;
    }

    private int msBeforePlaybackLine(int numSecondsOnScreen){
        int pixelsBeforeLine = (mScreenWidth/8);
        double mspp = (numSecondsOnScreen * 1000) / (double)mScreenWidth;
        return (int)Math.round(mspp * pixelsBeforeLine);
    }

    private int computeSampleStartPosition(int startMillisecond){
        int seconds = startMillisecond/1000;
        int ms = (startMillisecond-(seconds*1000));
        int tens = ms/10;

        int sampleStartPosition = (AudioInfo.SAMPLERATE* 2 * seconds) + (ms * 88) + (tens*AudioInfo.SIZE_OF_SHORT);
        if(mUseCompressedFile){
            sampleStartPosition /= 50;
            if(sampleStartPosition%2 != 0) {
                sampleStartPosition++;
            }
        }
        return sampleStartPosition;
    }

    private double getIncrement(int numSecondsOnScreen){
        double increment = (int)(numSecondsOnScreen * AudioInfo.SAMPLERATE / (float)mScreenWidth) * AudioInfo.SIZE_OF_SHORT;
        if(mUseCompressedFile) {
            increment /= 50.d;
            increment *= 2;
        }
        //increment = (increment % 2 == 0)? increment : increment+1;
        System.out.println("increment is " + increment);
        return increment;
    }

    private int getLastIndex(int startMillisecond, int numSecondsOnScreen) {
        int endMillisecond = startMillisecond + (numSecondsOnScreen) * 1000;
        return computeSampleStartPosition(endMillisecond);
    }

    private int getNumSecondsOnScreen(float userScale){
        int numSecondsOnScreen = (int)Math.round(mDefaultSecondsOnScreen * userScale);
        return Math.max(numSecondsOnScreen, 1);
    }

    public double millisecondsPerPixel(){
        return mNumSecondsOnScreen * 1000/(double)mScreenWidth;
    }


    private MappedByteBuffer selectBufferToUse(boolean useCompressedFile){
        if (useCompressedFile){
            return mCompressed;
        }
        else
            return buffer;
    }

    private int computeSpaceToAllocateForSamples(int startPosition, int endPosition, int increment){
        //the 2 is to give a little extra room, and the 4 is to account for x1, y1, x2, y2 for each
        return Math.abs(((endPosition+2*increment*AudioInfo.SIZE_OF_SHORT)-startPosition*AudioInfo.SIZE_OF_SHORT)) * 4;
    }

    private int mapTimeToClosestSecond(int location){
        return (int)Math.round((double)location/(double)1000);
    }
}