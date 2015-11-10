package wycliffeassociates.recordingapp.AudioVisualization;

import java.nio.MappedByteBuffer;

import wycliffeassociates.recordingapp.AudioInfo;

public class WavVisualizer {

    private final MappedByteBuffer preprocessedBuffer;
    private final MappedByteBuffer buffer;
    private float userScale = 1f;
    //private final int AudioInfo.COMPRESSED_SECONDS_ON_SCREEN = 5;
    private final int defaultSecondsOnScreen = 10;
    private boolean useCompressedFile = false;
    private float[] samples;
    double yScale;
    int screenHeight;
    int screenWidth;

    public WavVisualizer(MappedByteBuffer buffer, MappedByteBuffer preprocessedBuffer, int screenWidth, int screenHeight) {
        this.buffer = buffer;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.preprocessedBuffer = preprocessedBuffer;
        samples = new float[screenWidth*8];
    }

    public float[] getDataToDraw(int location, int largest){


        //by default, the number of seconds on screen should be 10, but this should be multiplied by the zoom
        int numSecondsOnScreen = getNumSecondsOnScreen(userScale);
        //based on the user scale, determine which buffer waveData should be
        //useCompressedFile = shouldUseCompressedFile(numSecondsOnScreen);
        MappedByteBuffer waveData = selectBufferToUse(useCompressedFile);



        //get the number of array indices to skip over- the array will likely contain more data than one pixel can show
        int increment = getIncrement(numSecondsOnScreen);
        //compute the starting and ending index based on the current audio playback position, and ultimately how much time is represented in one pixel
        int startPosition = computeSampleStartPosition(location);
        int lastIndex = getLastIndex(location, numSecondsOnScreen);
        //jumping in increments will likely not line up with the end of the file, so store where it stops
        int leftOff = 0;



        //scale the waveform down based on the largest peak of the waveform
        yScale = getYScaleFactor(screenHeight, largest);



        //modify the starting position due to wanting the current position to start at the playback line
        //this means data prior to this location should be drawn, in which samples needs to be initialized to provide some empty space if
        //the current playback position is in the early parts of the file and there is no earlier data to read
        startPosition = computeOffsetForPlaybackLine(numSecondsOnScreen, startPosition);
        int index = initializeSamples(samples, startPosition, increment);
        //in the event that the actual start position ends up being negative (such as from shifting forward due to playback being at the start of the file)
        //it should be set to zero (and the buffer will already be initialized with some zeros, with index being the index of where to resume placing data
        startPosition = Math.max(0, startPosition);



        //beginning with the starting position, the width of each increment represents the data one pixel width is showing
        for(int i = startPosition; i < Math.min(waveData.capacity(), lastIndex); i += increment){
            index = addHighAndLowToDrawingArray(waveData, samples, i, i+increment, index);
            leftOff = i; //keeps track of where we got to in the buffer, in case the last pixel doesn't have a full increment worth of data
        }
        //if we ended the loop with data left, but jumping another increment would go over capacity or the last index....
        if(leftOff < Math.min(waveData.capacity(), lastIndex)){
            //Same as the previous loop, except it loops from where the last left off until the last index
            index = addHighAndLowToDrawingArray(waveData, samples, leftOff, lastIndex, index);
        }
        //zero out the rest of the array
        for (int i = index; i < samples.length; i++){
            samples[i] = 0;
        }

        return samples;
    }

    private int addHighAndLowToDrawingArray(MappedByteBuffer waveData, float[] samples, int beginIdx, int endIdx, int index){

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        //loop over the indicated chunk of data to extract out the high and low in that section, then store it in samples
        for(int i = beginIdx; i < Math.min(waveData.capacity(), endIdx); i+= AudioInfo.SIZE_OF_SHORT){
            if((i+1) < waveData.capacity()) {
                byte low = waveData.get(i);
                byte hi = waveData.get(i + 1);
                short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                max = (max < (double) value) ? value : max;
                min = (min > (double) value) ? value : min;
            }
        }
        if(samples.length > index+4){
            samples[index] = index/4;
            samples[index+1] = (float)((max* yScale) + screenHeight / 2);
            samples[index+2] =  index/4;
            samples[index+3] = (float)((min * yScale) + screenHeight / 2);
            index+=4;
        }

        //returns the end of relevant data in the buffer
        return index;
    }

    private int initializeSamples(float[] samples, int startPosition, int increment){
        if(startPosition <= 0) {
            int numberOfZeros = Math.abs(startPosition) / increment;
            //System.out.println("number of zeros is " + numberOfZeros);
            //System.out.println("Start position is " + startPosition + " increment is " + increment);
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

    public static boolean shouldUseCompressedFile(int numSecondsOnScreen){
        if(numSecondsOnScreen >= AudioInfo.COMPRESSED_SECONDS_ON_SCREEN){
            return true;
        }
        else return false;
    }

    private int millisecondsPerPixel(int numSecondsOnScreen){
        //not entirely sure why the 2 needs to be there for the second case, but it appears to be necessary
        //the math may be wrong for the first case, as using the uncompressed file works perfectly during playback
        int millisecondsPerPixel  = (useCompressedFile)? (int)Math.ceil(((AudioInfo.COMPRESSED_SECONDS_ON_SCREEN * 1000) / (double)screenWidth * (numSecondsOnScreen / (double)AudioInfo.COMPRESSED_SECONDS_ON_SCREEN))  * AudioInfo.SIZE_OF_SHORT):
                (int)((AudioInfo.SAMPLERATE*numSecondsOnScreen*2) / (double)screenWidth);
        return millisecondsPerPixel;
    }

    private int computeOffsetForPlaybackLine(int numSecondsOnScreen, int startPosition){
        int pixelsBeforeLine = (screenWidth/8);
        int mspp = millisecondsPerPixel(numSecondsOnScreen);
        //System.out.println("First start position is " + startPosition + " " + pixelsBeforeLine + " " + mspp);
        return startPosition - (mspp * pixelsBeforeLine);
    }

    private int computeSampleStartPosition(int startMillisecond){
        // multiplied by 2 because of a hi and low for each sample in the compressed file
        //System.out.println("start millisecond is " + startMillisecond + " numSecondsOnScreen is " + numSecondsOnScreen);
        int sampleStartPosition = (useCompressedFile)? (int)(startMillisecond * (screenWidth/(double)(1000*AudioInfo.COMPRESSED_SECONDS_ON_SCREEN))) * 2 *AudioInfo.SIZE_OF_SHORT : (int)((startMillisecond/1000.0) * AudioInfo.SAMPLERATE ) * AudioInfo.SIZE_OF_SHORT;
        return sampleStartPosition;
    }

    private int getIncrement(int numSecondsOnScreen){
        int increment = (useCompressedFile)?  (int)Math.floor((numSecondsOnScreen / AudioInfo.COMPRESSED_SECONDS_ON_SCREEN)) * 2 * AudioInfo.SIZE_OF_SHORT : (numSecondsOnScreen * AudioInfo.SAMPLERATE / screenWidth) * AudioInfo.SIZE_OF_SHORT;
        return increment;
    }

    private int getLastIndex(int startMillisecond, int numSecondsOnScreen) {
        int endMillisecond = startMillisecond + (numSecondsOnScreen)*1000;
        return computeSampleStartPosition(endMillisecond);
    }

    private int getNumSecondsOnScreen(float userScale){
        int numSecondsOnScreen = (int)(defaultSecondsOnScreen * userScale);
        return Math.max(numSecondsOnScreen, 1);
    }

    private MappedByteBuffer selectBufferToUse(boolean useCompressedFile){
        if (useCompressedFile){
            return preprocessedBuffer;
        }
        else
            return buffer;
    }

    public static double getYScaleFactor(int canvasHeight, int largest){
        //System.out.println(largest + " for calculating y scale");
        return ((canvasHeight*.8)/ (largest * 2.0));
    }

    private int computeSpaceToAllocateForSamples(int startPosition, int endPosition, int increment){
        //the 2 is to give a little extra room, and the 4 is to account for x1, y1, x2, y2 for each
        return Math.abs(((endPosition+2*increment*AudioInfo.SIZE_OF_SHORT)-startPosition*AudioInfo.SIZE_OF_SHORT)) * 4;
    }

    private int mapLocationToClosestSecond(int location){
        return (int)Math.round((double)location/(double)1000);
    }
}