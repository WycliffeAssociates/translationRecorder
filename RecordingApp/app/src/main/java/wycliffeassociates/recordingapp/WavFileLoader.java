package wycliffeassociates.recordingapp;


import android.content.Context;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class WavFileLoader {

    private byte[] wavFile;
    public short[][] audioData;
    public static final int WAVE_HEADER_SIZE = 44;
    private int numChannels;
    private int sampleRate;
    private int blockSize;
    private int min;
    private int max;
    private int largestValue;



    public WavFileLoader(String file){
        System.out.println("Opening file " + file);
        try {
            File f = new File(file);
            wavFile = new byte[(int)f.length()];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(wavFile, 0, wavFile.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        processHeader();
        parseAudio();
        System.out.println("Parsed Audio");

    }
    private void processHeader(){
        if(wavFile.length > WAVE_HEADER_SIZE) {
            numChannels = (int) wavFile[22] & 0xFF;
            numChannels = 2;
            blockSize = (int) wavFile[32] & 0xFF;
            int part1 = (((int) wavFile[27] & 0xFF) << 24) & 0xFF000000;
            int part2 = (((int) wavFile[26] & 0xFF) << 16) & 0x00FF0000;
            int part3 = (((int) wavFile[25] & 0xFF) << 8) & 0x0000FF00;
            int part4 = ((int) wavFile[24] & 0xFF) & 0x000000FF;
            sampleRate = part1 | part2 | part3 | part4;
            System.out.println("Sample rate is : " + sampleRate + " hz; Number of channels is " + numChannels);
        }
    }
    private void parseAudio(){
        audioData = new short[numChannels][(wavFile.length-WAVE_HEADER_SIZE)/blockSize];
        int index = 0;
        for(int i = WAVE_HEADER_SIZE; i < wavFile.length; i+=blockSize){
            for(int j = 0; j < numChannels; j++){
                byte low = wavFile[i+j];
                byte hi = wavFile[i+j+1];

                //wav file written in little endian
                short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                audioData[j][index] = value;
                max = (i==0 && j ==0)? value : max;
                min = (i==0 && j ==0)? value : min;
                max = (value > max)? value : max;
                min = (value < min)?  value : min;
            }
            index++;
        }
        if(audioData[0].length != (wavFile.length-WAVE_HEADER_SIZE)){
            System.out.println("Audio data length: " + audioData[0].length);
            System.out.println("File length: " + wavFile.length);
            System.out.println("What what what");
        }
        largestValue = (Math.abs(min) < Math.abs(max))? max : min;
    }
    public int getSampleRate(){ return sampleRate; }
    public short[][] getAudioData(){ return audioData; }

    public int getNumChannels() {
        return numChannels;
    }

    public int getLargestValue() {
        return largestValue;
    }
}
