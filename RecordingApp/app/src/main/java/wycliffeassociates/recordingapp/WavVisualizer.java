package wycliffeassociates.recordingapp;

import android.provider.MediaStore;
import android.util.Pair;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class WavVisualizer {

    private final MappedByteBuffer preprocessedBuffer;
    private final MappedByteBuffer buffer;
    private ArrayList<Pair<Double, Double>> samples;
    private float userScale = 1.f;
    private final int compressedSecondsOnScreen = 5;
    private final int defaultSecondsOnScreen = 10;


    public WavVisualizer(MappedByteBuffer buffer, MappedByteBuffer preprocessedBuffer) {
        this.buffer = buffer;
        this.preprocessedBuffer = preprocessedBuffer;

    }

    public ArrayList<Pair<Double, Double>> getDataToDraw(int location, int screenWidth, int screenHeight, int largest){
        ArrayList<Pair<Double,Double>> samples = new ArrayList<>();
        //by default, the number of seconds on screen should be 10, but this should be multiplied by the zoom
        int numSeconds = getNumSecondsOnScreen(userScale);
        //based on the user scale, determine which buffer waveData should be
        MappedByteBuffer waveData = selectBufferToUse(userScale);
        int startSecond = mapLocationToClosestSecond(location);
        int lastIndex = getLastIndex(startSecond, numSeconds, screenWidth);
        int increment = getIncrement(numSeconds, screenWidth);
        int leftOff = 0;
        int startPosition = computeSampleStartPosition(location, increment, screenWidth);
        double yScale = getYScaleFactor(screenHeight, largest);
        if(numSeconds >= compressedSecondsOnScreen) {
            //compressed buffer is storing a hi and low value for the segment of the buffer that it compresses
            //therefore even with no increment here, you have double the values
            increment*=2;
        }
        startPosition -= computeOffsetForPlaybackLine(screenWidth, numSeconds);
        startPosition = Math.max(0, startPosition);
        initializeSamples(samples, screenWidth, numSeconds, location);
        //startPosition = Math.max(0, startPosition);
        for(int i = startPosition*AudioInfo.SIZE_OF_SHORT; i < Math.min(waveData.capacity(), lastIndex); i += increment*AudioInfo.SIZE_OF_SHORT){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            //compute the average
            for(int j = 0; j < increment*AudioInfo.SIZE_OF_SHORT; j+=AudioInfo.SIZE_OF_SHORT){
                if((i+j+1) < waveData.capacity()) {
                    byte low = waveData.get(i + j);
                    byte hi = waveData.get(i + j + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            samples.add(new Pair<>(max * yScale, min * yScale));
            leftOff = i;
        }

        //Finally loop through the last section if it doesn't line up perfectly
        System.out.println(buffer.capacity());
        if(leftOff *(increment*AudioInfo.SIZE_OF_SHORT) < Math.min(waveData.capacity(), lastIndex)){
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
            samples.add(new Pair<>(max * yScale, min * yScale));
        }
        return samples;
    }
    private int millisecondsPerPixel(int screenWidth, int numSeconds){
        int millisecondsPerPixel  = (numSeconds > compressedSecondsOnScreen)? (AudioInfo.SAMPLERATE*numSeconds) / screenWidth : (compressedSecondsOnScreen * 1000) / screenWidth * (numSeconds / compressedSecondsOnScreen);
        return millisecondsPerPixel;
    }

    private void initializeSamples(ArrayList<Pair<Double, Double>> samples, int screenWidth, int numSeconds, int location){
        int positionOfPlaybackLine = screenWidth / 8;
        int millisecondsPerPixel = millisecondsPerPixel(screenWidth, numSeconds);
        int positionOfPlaybackLocation = (location / millisecondsPerPixel);
        if (positionOfPlaybackLine > positionOfPlaybackLocation){
            for (int i = 0; i < (positionOfPlaybackLine - positionOfPlaybackLocation); i++){
                samples.add(new Pair<>(0.0,0.0));
            }
        }
        System.out.println("samples is now of length " + samples.size());
    }

    private int computeOffsetForPlaybackLine(int screenWidth, int numSeconds){
        int lineInPixels = (screenWidth/8);
        int lineInMilliseconds = millisecondsPerPixel(screenWidth, numSeconds);
        int positionInBuffer = (numSeconds > compressedSecondsOnScreen)? lineInMilliseconds * 2 : lineInMilliseconds * 4;
        return positionInBuffer;
    }

    private int computeSampleStartPosition(int startMillisecond, int numSeconds, int screenWidth){
        // multiplied by 2 because of a hi and low for each sample in the compressed file
        int sampleStartPosition = (numSeconds < defaultSecondsOnScreen)? startMillisecond * (AudioInfo.SAMPLERATE / 1000) : startMillisecond * (screenWidth/5000) * 2;
        return sampleStartPosition;
    }

    private int getIncrement(int numSecondsOnScreen, int screenWidth){
        int increment = (numSecondsOnScreen < compressedSecondsOnScreen)? numSecondsOnScreen * AudioInfo.SAMPLERATE / screenWidth : (int)Math.floor((numSecondsOnScreen * (screenWidth / compressedSecondsOnScreen)) / screenWidth);
        return increment;
    }

    private int getLastIndex(int startSecond, int numSeconds, int screenWidth){
        //get a bit extra to draw off screen
        int extraSecondsBuffer = (int)Math.ceil(numSeconds/4.0);
        //check to determine which file we're looping over
        int samplesPerSecond = (numSeconds < defaultSecondsOnScreen)? AudioInfo.SAMPLERATE*numSeconds*AudioInfo.SIZE_OF_SHORT : (int)Math.ceil((screenWidth/defaultSecondsOnScreen)*numSeconds*AudioInfo.SIZE_OF_SHORT);
        int lastIndex = (startSecond+numSeconds+extraSecondsBuffer)*AudioInfo.SIZE_OF_SHORT*samplesPerSecond;
        return lastIndex;
    }

    private int getNumSecondsOnScreen(float userScale){
        int numSecondsOnScreen = (int)(defaultSecondsOnScreen * userScale);
        return Math.max(numSecondsOnScreen, 1);
    }

    private int mapLocationToClosestSecond(int location){
        //convert location from miliseconds to seconds, then round
        return (int)Math.round((double)location/1000);
    }

    private MappedByteBuffer selectBufferToUse(float userScale){
        //.5 is the userscale needed to show 5 seconds on the screen
        if (userScale <  compressedSecondsOnScreen/defaultSecondsOnScreen){
            return buffer;
        }
        else
            return preprocessedBuffer;
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
            return (canvasWidth / ((double) samples.size()));
    }

    public double getYScaleFactor(int canvasHeight, int largest){
        System.out.println(largest + " for calculating y scale");
        return ((canvasHeight*.8)/ (largest * 2.0));
    }

    public ArrayList<Pair<Double, Double>> getSamples(){
        return samples;
    }

}