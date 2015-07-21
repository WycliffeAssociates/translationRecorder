package wycliffeassociates.recordingapp;

import android.util.Pair;

import java.util.ArrayList;

public class WavVisualizer {

    private double largest;
    private int numChannels;
    private short audio[][];
    private ArrayList<Pair<Double, Double>> samples;

    public WavVisualizer(short audio[][], int numChannels){
        this.audio = audio;
        this.numChannels = numChannels;
        this.largest = Double.MIN_VALUE;
    }

    public void sampleAudio(int increment){
        samples = new ArrayList<>();
        double recip = 1.0/increment;
        int index = 0;
        for(int i = 0; i < audio[0].length-increment-1; i += increment){
            double sum = 0;
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            //compute the average
            for(int j = 0; j < increment; j++){
                //sum += recip * audio[0][i+j];
                max = (max < (double)audio[0][i+j])? audio[0][i+j] : max;
                min = (min > (double)audio[0][i+j])? audio[0][i+j] : min;
            }
            largest = (Math.abs(min) > largest)? Math.abs(min) : largest;
            largest = (Math.abs(max) > largest)? max : largest;
            samples.add(index, new Pair<>(max, min));
            //samples[index] = -sum* yScale/4;
            index++;
        }
        System.out.println("Length should be: " + index);
        System.out.println("Sample size : " + samples.size());
        for(int i = samples.size()-1; i > 0; i--){
            if((samples.get(i).first < 0.00001) && (samples.get(i).first > -0.000001) && (samples.get(i).second < 0.00001) && samples.get(i).second > -0.00001)
                samples.remove(i);
            else
                break;
        }
        System.out.println("Sample size after trim : " + samples.size());
    }


    public double getXScaleFactor(int canvasWidth, int secondsOnScreen) {
        double secondsInCycles = 44100 * secondsOnScreen;

        if(secondsOnScreen > 0) {
            return canvasWidth / secondsInCycles;
        }
        else
            return (canvasWidth / ((double) audio[0].length));
    }

    public double getYScaleFactor(int canvasHeight){
        System.out.println(largest);
        return (canvasHeight / (largest * 2.0));
    }

    public short[] getAudio(int channel){
        return audio[channel];
    }

    public int getIncrement(double xScale) {
        int increment = (int) Math.ceil((1.0 / xScale));
        return increment;
    }
    public ArrayList<Pair<Double, Double>> getSamples(){
        return samples;
    }

}