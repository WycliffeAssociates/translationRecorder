package wycliffeassociates.recordingapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class WavVisualizer {

    short audio[][];
    int max;
    int min;
    int largest;

    public WavVisualizer(short rawAudio[], int numChannels){
        audio = new short[numChannels][rawAudio.length/numChannels];
        int index = 0;
        for (int i = 0; i < rawAudio.length; i+=numChannels){
            for(int j = 0; j < numChannels; j++){
                audio[j][index] = rawAudio[i+j];
                max = (i==0 && j ==0)? rawAudio[i+j] : max;
                min = (i==0 && j ==0)? rawAudio[i+j] : min;
                max = (rawAudio[i+j] > max)? rawAudio[i+j] : max;
                min = (rawAudio[i+j] < min)?  rawAudio[i+j] : min;
            }
            index++;
        }
        largest = (Math.abs(min) < Math.abs(max))? max : min;
    }

    public double getXScaleFactor(int canvasWidth){
        return (canvasWidth / ((double) audio[0].length));
    }

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


}
