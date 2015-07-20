package wycliffeassociates.recordingapp;

import android.util.Pair;

import java.util.ArrayList;

public class WavVisualizer {

    private int largest;
    private int numChannels;
    private short audio[][];
    private ArrayList<Pair<Double, Double>> samples;

    public WavVisualizer(short audio[][], int numChannels, int largest){
        this.audio = audio;
        this.numChannels = numChannels;
        this.largest = largest;
    }

    public void sampleAudio(int increment, double yScale){
        samples = new ArrayList<>();
        double recip = 1.0/increment;
        int index = 0;
        for(int i = 0; i < audio[0].length-increment; i += increment){
            double sum = 0;
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            //compute the average
            for(int j = 0; j < increment; j++){
                //sum += recip * audio[0][i+j];
                max = (max < (double)audio[0][i+j])? audio[0][i+j] : max;
                min = (min > (double)audio[0][i+j])? audio[0][i+j] : min;
            }
            samples.add(index, new Pair<>(max*yScale/4, min*yScale/4));
            //samples[index] = -sum* yScale/4;
            index++;
        }
    }


    public double getXScaleFactor(int canvasWidth) { return (canvasWidth / ((double) audio[0].length)); }

    public double getYScaleFactor(int canvasHeight){
        return (canvasHeight / (largest * 2 * 1.2));
    }

    public short[] getAudio(int channel){
        return audio[channel];
    }

    public int getIncrement(double xScale) {
        int increment = (int) (audio[0].length / (audio[0].length * xScale));
        return increment;
    }
    public ArrayList<Pair<Double, Double>> getSamples(){
        return samples;
    }

}
