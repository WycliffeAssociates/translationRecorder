package wycliffeassociates.recordingapp.AudioVisualization;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.util.Vector;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.WavPlayer;

public class WavVisualizer {

    private MappedByteBuffer preprocessedBuffer;
    private MappedByteBuffer buffer;
    private float userScale = 1f;
    private final int defaultSecondsOnScreen = 10;
    public static int numSecondsOnScreen;
    private boolean useCompressedFile = false;
    private boolean canSwitch = false;
    private float[] samples;
    double yScale;
    int screenHeight;
    int screenWidth;
    AudioFileAccessor mAccessor;

    public WavVisualizer(MappedByteBuffer buffer, MappedByteBuffer preprocessedBuffer, int screenWidth, int screenHeight, CutOp cut) {
        this.buffer = buffer;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.preprocessedBuffer = preprocessedBuffer;
        numSecondsOnScreen = defaultSecondsOnScreen;
        canSwitch = (preprocessedBuffer == null)? false : true;
        samples = new float[screenWidth*8];
        mAccessor = new AudioFileAccessor(preprocessedBuffer, buffer, cut);
    }

    public void enableCompressedFileNextDraw(MappedByteBuffer preprocessedBuffer){
        System.out.println("Swapping buffers now");
        this.preprocessedBuffer = preprocessedBuffer;
        this.canSwitch = true;
    }

    public float[] getMinimap(CutOp cut, int minimapHeight){
        //selects the proper buffer to use
        boolean useCompressed = canSwitch && numSecondsOnScreen > AudioInfo.COMPRESSED_SECONDS_ON_SCREEN;
        mAccessor.switchBuffers(useCompressed);

        float[] minimap = new float[AudioInfo.SCREEN_WIDTH * 4];
        int pos = 0;
        int index = 0;
        int increment = mAccessor.getIncrement(WavPlayer.getAdjustedDuration()/(double)1000, useCompressed);
        for(int i = 0; i < AudioInfo.SCREEN_WIDTH; i++){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for(int j = 0; j < increment; j+=2){
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
            minimap[index] = index/4;
            minimap[index+1] = (float)((max* 168/15000) + minimapHeight / 2);
            minimap[index+2] =  index/4;
            minimap[index+3] = (float)((min * 168/15000) + minimapHeight / 2);
            System.out.println(max +" " + min + " ");
            System.out.println(minimap[index + 1] + " " + minimap[index + 3] + " ");
            index+=4;
        }
        System.out.print("height is " + minimapHeight);

        return minimap;
    }



    public float[] getDataToDraw(int location, int largest, CutOp cut){

        numSecondsOnScreen = getNumSecondsOnScreen(userScale);
        //based on the user scale, determine which buffer waveData should be
        useCompressedFile = shouldUseCompressedFile(numSecondsOnScreen);
        mAccessor.switchBuffers(useCompressedFile);

        //get the number of array indices to skip over- the array will likely contain more data than one pixel can show
        int increment = getIncrement(numSecondsOnScreen);
        int timeToSubtract = msBeforePlaybackLine(numSecondsOnScreen);
        int startPosition = mAccessor.indexAfterSubtractingTime(timeToSubtract, location, numSecondsOnScreen);

        //scale the waveform down based on the largest peak of the waveform
        yScale = getYScaleFactor(screenHeight, largest);

        int index = initializeSamples(samples, startPosition, increment);
        //in the event that the actual start position ends up being negative (such as from shifting forward due to playback being at the start of the file)
        //it should be set to zero (and the buffer will already be initialized with some zeros, with index being the index of where to resume placing data
        startPosition = Math.max(0, startPosition);
        int end = samples.length/4;

        Log.i(this.toString(), "loc is " + location + " duration is " + WavPlayer.getDuration() + " adjusted loc is " + cut.timeAdjusted(location) + " duration without the cut " + (WavPlayer.getDuration() - cut.getSizeCut()));
        Log.i(this.toString(), "start is " + startPosition);

        //beginning with the starting position, the width of each increment represents the data one pixel width is showing
        for(int i = index/4; i < end; i++){
            if(startPosition+increment > mAccessor.size()){
                break;
            }
            index = addHighAndLowToDrawingArray(mAccessor, samples, startPosition, startPosition+increment, index);
            startPosition += increment;
        }
        //zero out the rest of the array
        for (int i = index; i < samples.length; i++){
            samples[i] = 0;
        }

        return samples;
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

    private int mapLocationToTime(int idx, CutOp cut){
        double idxP = (useCompressedFile)? idx/(double)preprocessedBuffer.capacity()
                : idx/(double)buffer.capacity();
        int ms = (int)Math.round(idxP * WavPlayer.getDuration());
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
            int numberOfZeros = (int)Math.round(Math.abs(startPosition) / (double)increment);
            System.out.println("number of zeros is " + numberOfZeros);
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

    public boolean shouldUseCompressedFile(int numSecondsOnScreen){
        if(numSecondsOnScreen >= AudioInfo.COMPRESSED_SECONDS_ON_SCREEN && canSwitch){
            return true;
        }
        else return false;
    }

    private int msBeforePlaybackLine(int numSecondsOnScreen){
        int pixelsBeforeLine = (screenWidth/8);
        double mspp = (numSecondsOnScreen * 1000) / (double)screenWidth;
        return (int)Math.round(mspp * pixelsBeforeLine);
    }

    private int computeSampleStartPosition(int startMillisecond, int numSecondsOnScreen){
        // multiplied by 2 because of a hi and low for each sample in the compressed file
        //System.out.println("Duration of file is " + WavPlayer.getDuration());
/*
        int sampleStartPosition = (useCompressedFile)? AudioInfo.SIZE_OF_SHORT * 2 * (int)(Math.round((

                                                                                                        (startMillisecond/(double)WavPlayer.getDuration())
                                                                                                                *(preprocessedBuffer.capacity()/4)
                                                                                                        )
                                                                                                    ))
                //(int)Math.round(startMillisecond * (screenWidth/((double)(AudioInfo.COMPRESSED_SECONDS_ON_SCREEN * 1000)))) * 4
        //int sampleStartPosition = (useCompressedFile)? (int)Math.floor((startMillisecond/(double)WavPlayer.getDuration())*(preprocessedBuffer.capacity()/4.0)*4)
                : (int)(Math.round((startMillisecond/1000.d) * AudioInfo.SAMPLERATE )) * AudioInfo.SIZE_OF_SHORT;
*/
        int sampleStartPosition = (int)Math.round((startMillisecond/1000.d) * AudioInfo.SAMPLERATE ) * AudioInfo.SIZE_OF_SHORT;
        if(useCompressedFile){
            int increment = (int)Math.round((AudioInfo.SAMPLERATE * AudioInfo.COMPRESSED_SECONDS_ON_SCREEN) / (double)screenWidth)  * AudioInfo.SIZE_OF_SHORT;
            sampleStartPosition = (int)Math.round(sampleStartPosition / (double)increment) * AudioInfo.SIZE_OF_SHORT * 2;
        }
        return sampleStartPosition;
    }

    private int getIncrement(int numSecondsOnScreen){
        int increment = (useCompressedFile)?  (int)Math.round((numSecondsOnScreen / AudioInfo.COMPRESSED_SECONDS_ON_SCREEN)) * 2 * AudioInfo.SIZE_OF_SHORT
                : (numSecondsOnScreen * AudioInfo.SAMPLERATE / screenWidth) * AudioInfo.SIZE_OF_SHORT;
        increment = (increment % 2 == 0)? increment : increment+1;
        return increment;
    }

    private int getLastIndex(int startMillisecond, int numSecondsOnScreen) {
        int endMillisecond = startMillisecond + (numSecondsOnScreen) * 1000;
        return computeSampleStartPosition(endMillisecond, numSecondsOnScreen);
    }

    private int getNumSecondsOnScreen(float userScale){
        int numSecondsOnScreen = (int)Math.round(defaultSecondsOnScreen * userScale);
        return Math.max(numSecondsOnScreen, 1);
    }

    public double millisecondsPerPixel(){
        return numSecondsOnScreen * 1000/(double)screenWidth;
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

    private int mapTimeToClosestSecond(int location){
        return (int)Math.round((double)location/(double)1000);
    }
}