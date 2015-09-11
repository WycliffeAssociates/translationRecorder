package wycliffeassociates.recordingapp;

import android.util.Pair;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class WavVisualizer {

    private final MappedByteBuffer preprocessedBuffer;
    private final MappedByteBuffer buffer;
    private double largest;
    private ArrayList<Pair<Double, Double>> samples;


    public WavVisualizer(MappedByteBuffer buffer, MappedByteBuffer preprocessedBuffer) {
        this.buffer = buffer;
        this.preprocessedBuffer = preprocessedBuffer;
    }
/*
    public ArrayList<Pair<Double, Double>> getDataToDraw(int location){
        ArrayList<Pair<Double,Double>> samples = new ArrayList<>();
        //by default, the number of seconds on screen should be 10, but this should be multiplied by the zoom
        int numSeconds = getNumSecondsOnScreen(userScale);
        //based on the user scale, determine which buffer waveData should be
        MappedByteBuffer waveData = selectBufferToUse(userScale);
        int startSecond = mapLocationToClosestSecond(location);
        int lastIndex = getLastIndex(startSecond);
        int increment = getIncrement(userScale, width);
        int leftOff = 0;
        computeSampleStartPosition(startSecond, increment);

        int extraSecondsBuffer = (int)Math.ceil(numSeconds/8.0);
        for(int i = startSecond*AudioInfo.SAMPLERATE*AudioInfo.SIZE_OF_SHORT; i < Math.min(buffer.capacity(), lastIndex); i += increment*AudioInfo.SIZE_OF_SHORT){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            //compute the average
            for(int j = 0; j < increment*AudioInfo.SIZE_OF_SHORT; j+=AudioInfo.SIZE_OF_SHORT){
                if((i+j+1) < buffer.capacity()) {
                    byte low = buffer.get(i + j);
                    byte hi = buffer.get(i + j + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            samples.add(new Pair<>(max, min));
            leftOff = i;
        }

        //Finally loop through the last section if it doesn't line up perfectly
        if(leftOff +(increment*AudioInfo.SIZE_OF_SHORT) < Math.min(buffer.capacity(), lastIndex)){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for(int i = leftOff; i < Math.min(buffer.capacity(), lastIndex); i+=AudioInfo.SIZE_OF_SHORT){
                if((i+1) < buffer.capacity()) {
                    byte low = buffer.get(i);
                    byte hi = buffer.get(i + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            samples.add(new Pair<>(max, min));
        }
        return samples;
    }*/

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

    public double getYScaleFactor(int canvasHeight){
        System.out.println(largest + " for calculating y scale");
        return (canvasHeight / (largest * 2.0));
    }

    public ArrayList<Pair<Double, Double>> getSamples(){
        return samples;
    }

}