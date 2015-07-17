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

    public static final int WAVE_HEADER_SIZE = 44;
    private int numChannels;
    private int sampleRate;



    public WavFileLoader(String file){

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
        System.out.println("size is " + wavFile.length);
        if(wavFile.length > WAVE_HEADER_SIZE) {
            numChannels = (int) wavFile[22] & 0xFF;
            int part1 = (((int) wavFile[27] & 0xFF) << 24) & 0xFF000000;
            int part2 = (((int) wavFile[26] & 0xFF) << 16) & 0x00FF0000;
            int part3 = (((int) wavFile[25] & 0xFF) << 8) & 0x0000FF00;
            int part4 = ((int) wavFile[24] & 0xFF) & 0x000000FF;
            sampleRate = part1 | part2 | part3 | part4;
            System.out.println("Sample rate is : " + sampleRate + " hz; Number of channels is " + numChannels);
        }
    }
    public int getSampleRate(){ return sampleRate;}
}
