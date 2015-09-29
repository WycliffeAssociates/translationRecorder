 package wycliffeassociates.recordingapp;

import android.provider.MediaStore;
import android.util.Pair;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class WavVisualizer {

    private final MappedByteBuffer preprocessedBuffer;
    private final MappedByteBuffer buffer;
    private float userScale = 1f;
    private final int compressedSecondsOnScreen = 5;
    private final int defaultSecondsOnScreen = 10;
    private boolean useCompressedFile = false;
    private int sampleSize = 0;
    private float[] samples;

    public WavVisualizer(MappedByteBuffer buffer, MappedByteBuffer preprocessedBuffer, int screenWidth) {
        this.buffer = buffer;
        this.preprocessedBuffer = preprocessedBuffer;
        samples = new float[screenWidth*8];
    }

    public float[] getDataToDraw(int location, int screenWidth, int screenHeight, int largest){
        //by default, the number of seconds on screen should be 10, but this should be multiplied by the zoom
        int numSecondsOnScreen = getNumSecondsOnScreen(userScale);
        //System.out.println("numSeconds on screen is " + numSecondsOnScreen);
        //based on the user scale, determine which buffer waveData should be

        useCompressedFile = shouldUseCompressedFile(numSecondsOnScreen);

        MappedByteBuffer waveData = selectBufferToUse(useCompressedFile);
        int increment = getIncrement(numSecondsOnScreen, screenWidth);
        int leftOff = 0;
        int startPosition = computeSampleStartPosition(location, numSecondsOnScreen, screenWidth);
        int lastIndex = getLastIndex(location, numSecondsOnScreen, screenWidth);
        double yScale = getYScaleFactor(screenHeight, largest);

        startPosition = computeOffsetForPlaybackLine(screenWidth, numSecondsOnScreen, startPosition);
        int index = initializeSamples(samples, startPosition, increment);
        //startPosition = Math.max(0, startPosition);
        //initializeSamples(samples, screenWidth, numSecondsOnScreen, location);
        startPosition = Math.max(0, startPosition);
        //System.out.println("Size of padding for drawing buffer = "+samples.size());
        //System.out.println("Start position is " +  startPosition+ " lastIndex is " + lastIndex + " capacity is " +  waveData.capacity() + " increment is " + increment);

        sampleSize = index;
        for(int i = startPosition; i < Math.min(waveData.capacity(), lastIndex); i += increment){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for(int j = 0; j < increment; j+= AudioInfo.SIZE_OF_SHORT){

                if((i+j+1) < waveData.capacity()) {
                    byte low = waveData.get(i + j);
                    byte hi = waveData.get(i + j + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            //samples.add(new Pair<>(max * yScale, min * yScale));
            if(samples.length < index+4){
                break;
            }
            samples[index] = index/4;
            samples[index+1] = (float)((max* yScale) + screenHeight / 2);
            samples[index+2] =  index/4;
            samples[index+3] = (float)((min * yScale) + screenHeight / 2);
            index+=4;
            sampleSize++;
            leftOff = i;
        }

        //Finally loop through the last section if it doesn't line up perfectly
        //System.out.println("Size of buffer is "+buffer.capacity());
        if(leftOff *(increment) < Math.min(waveData.capacity(), lastIndex)){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for(int i = leftOff; i < Math.min(waveData.capacity(), lastIndex); i+=AudioInfo.SIZE_OF_SHORT){
                if((i+1) < waveData.capacity()) {
                    byte low = waveData.get(i);
                    byte hi = waveData.get(i + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            if(samples.length > index+4){


                //samples.add(new Pair<>(max * yScale, min * yScale));
                samples[index] = index/4;
                samples[index+1] = (float)((max* yScale) + screenHeight / 2);
                samples[index+2] =  index/4;
                samples[index+3] = (float)((min * yScale) + screenHeight / 2);
                sampleSize++;
                index+=4;
            }
        }
        //System.out.println("Size of samples is "+ samples.size());

        //zero out the rest of the array
        for (int i = index; i < samples.length; i++){
            samples[i] = 0;
        }
        return samples;
    }

    private int computeSpaceToAllocateForSamples(int startPosition, int endPosition, int increment){
        //the 2 is to give a little extra room, and the 4 is to account for x1, y1, x2, y2 for each
        return Math.abs(((endPosition+2*increment*AudioInfo.SIZE_OF_SHORT)-startPosition*AudioInfo.SIZE_OF_SHORT)) * 4;
    }

    private boolean shouldUseCompressedFile(int numSecondsOnScreen){
        if(numSecondsOnScreen >= compressedSecondsOnScreen){
            return true;
        }
        else return false;
    }

    private int millisecondsPerPixel(int screenWidth, int numSecondsOnScreen){
        //not entirely sure why the 2 needs to be there for the second case, but it appears to be necessary
        //the math may be wrong for the first case, as using the uncompressed file works perfectly during playback
        int millisecondsPerPixel  = (useCompressedFile)? (int)Math.ceil(((compressedSecondsOnScreen * 1000) / (double)screenWidth * (numSecondsOnScreen / (double)compressedSecondsOnScreen))  * AudioInfo.SIZE_OF_SHORT):
                (int)((AudioInfo.SAMPLERATE*numSecondsOnScreen*2) / (double)screenWidth);
        return millisecondsPerPixel;
    }

    private int initializeSamples(float[] samples, int startPosition, int increment){
        if(startPosition <= 0) {
            int numberOfZeros = Math.abs(startPosition) / increment;
            System.out.println("number of zeros is " + numberOfZeros);
            System.out.println("Start position is " + startPosition + " increment is " + increment);
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

    private int computeOffsetForPlaybackLine(int screenWidth, int numSecondsOnScreen, int startPosition){
        int pixelsBeforeLine = (screenWidth/8);
        int mspp = millisecondsPerPixel(screenWidth, numSecondsOnScreen);
        System.out.println("First start position is " + startPosition + " " + pixelsBeforeLine + " " + mspp);
        return startPosition - (mspp * pixelsBeforeLine);
    }

    private int computeSampleStartPosition(int startMillisecond, int numSecondsOnScreen, int screenWidth){
        // multiplied by 2 because of a hi and low for each sample in the compressed file
        //System.out.println("start millisecond is " + startMillisecond + " numSecondsOnScreen is " + numSecondsOnScreen);
        int sampleStartPosition = (useCompressedFile)? (int)(startMillisecond * (screenWidth/(double)(1000*compressedSecondsOnScreen))) * 2 *AudioInfo.SIZE_OF_SHORT : (int)((startMillisecond/1000.0) * AudioInfo.SAMPLERATE ) * AudioInfo.SIZE_OF_SHORT;
        return sampleStartPosition;
    }

    private int getIncrement(int numSecondsOnScreen, int screenWidth){
        int increment = (useCompressedFile)?  (int)Math.floor((numSecondsOnScreen / compressedSecondsOnScreen)) * 2 * AudioInfo.SIZE_OF_SHORT : (numSecondsOnScreen * AudioInfo.SAMPLERATE / screenWidth) * AudioInfo.SIZE_OF_SHORT;
        return increment;
    }

    private int getLastIndex(int startMillisecond, int numSecondsOnScreen, int screenWidth){
        int endMillisecond = startMillisecond + (numSecondsOnScreen)*1000;
        //System.out.println("end millisecond is  " + endMillisecond + " start millisecond is " + startMillisecond );
        return computeSampleStartPosition(endMillisecond, numSecondsOnScreen, screenWidth);
    }

    private int getNumSecondsOnScreen(float userScale){
        int numSecondsOnScreen = (int)(defaultSecondsOnScreen * userScale);
        return Math.max(numSecondsOnScreen, 1);
    }

    private int mapLocationToClosestSecond(int location){
        //convert location from miliseconds to seconds, then round
        return (int)Math.round((double)location/(double)1000);
    }

    private MappedByteBuffer selectBufferToUse(boolean useCompressedFile){
        if (useCompressedFile){
            return preprocessedBuffer;
        }
        else
            return buffer;
    }

    public int getIncrement(double xScale) {
        int increment = (int) Math.ceil((1.0 / xScale));
        return increment;
    }

    public double getXScaleFactor(int canvasWidth, int secondsOnScreen) {
        double secondsInCycles = 44100.0 * secondsOnScreen;

        if(secondsOnScreen > 0) {
            return canvasWidth / secondsInCycles;
        }
        else
            return (canvasWidth / ((double) sampleSize));
    }

    public static double getYScaleFactor(int canvasHeight, int largest){
        //System.out.println(largest + " for calculating y scale");
        return ((canvasHeight*.8)/ (largest * 2.0));
    }


}