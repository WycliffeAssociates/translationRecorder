package wycliffeassociates.recordingapp;

import android.util.Pair;

import java.util.ArrayList;

public class WavVisualizer {

    private double largest;
    private ArrayList<Pair<Double, Double>> samples;

    public WavVisualizer(ArrayList<Pair<Double, Double>> samples, double largest) {
        this.samples = samples;
        this.largest = largest;
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

    public double getYScaleFactor(int canvasHeight){
        System.out.println(largest + " for calculating y scale");
        return (canvasHeight / (largest * 2.0));
    }

    public ArrayList<Pair<Double, Double>> getSamples(){
        return samples;
    }

}