package wycliffeassociates.recordingapp;

import android.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class WavFileLoader {

    private byte[] wavFile;
    public short[] audioData;
    private int min;
    private int max;
    private final int DATA_CHUNK = 2*AudioInfo.SAMPLERATE / 1000; //number of datapoints per second that should actually be useful: 44100 samples per sec, div by 20 = 2205 * 2 channels = 4410
                                         //this is enough for a resolution of 2k pixel width to display one second of wav across
    private int largest = 0;
    private MappedByteBuffer buffer;
    private MappedByteBuffer preprocessedBuffer;
    private int startIndex;
    private String visTempFile = "/storage/emulated/0/AudioRecorder/visualization.tmp";
    private int screenWidth = 2560;

    public MappedByteBuffer getMappedFile(){
        return buffer;
    }
    public MappedByteBuffer getMappedCacheFile(){
        return preprocessedBuffer;
    }

    public WavFileLoader(String file, int screenWidth) throws IOException {
        this.screenWidth = screenWidth;
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fc = raf.getChannel();
        buffer = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE, raf.length() - AudioInfo.HEADER_SIZE);
        generateTempFile();
        RandomAccessFile rafCached = new RandomAccessFile(visTempFile, "r");
        FileChannel fcCached = rafCached.getChannel();
        preprocessedBuffer = fcCached.map(FileChannel.MapMode.READ_ONLY, 0, rafCached.length());
        System.out.println("Largest value from file is " + largest);
    }

    private void generateTempFile(){
        try {
            FileOutputStream temp = new FileOutputStream(new File(visTempFile));
            //generate a file that can show 5 seconds on the screen without compromising resolution
            int increment = (int)Math.floor((AudioInfo.SAMPLERATE * 5)/screenWidth);
            System.out.println(increment + "increment ");

            for(int i = 0; i < buffer.capacity(); i+=AudioInfo.SIZE_OF_SHORT*increment){
                int max = Integer.MIN_VALUE;
                int min = Integer.MAX_VALUE;
                int minIdx = 0;
                int maxIdx = 0;

                for(int j = 0; j < increment*AudioInfo.SIZE_OF_SHORT; j+=AudioInfo.SIZE_OF_SHORT){
                    if((i+j+1) < buffer.capacity()) {
                        byte low = buffer.get(i + j);
                        byte hi = buffer.get(i + j + 1);
                        short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                        if(max < value){
                            max = value;
                            maxIdx = j;
                            if(value > largest){
                                largest = value;
                            }
                        }
                        if(min > value) {
                            min = value;
                            minIdx = j;
                        }
                    }
                }
                //order matters, rather than get the values themselves, find the index on this range and use that to write the values
                try {
                    if(minIdx < maxIdx){
                        temp.write((byte) buffer.get(i + minIdx));
                        temp.write((byte) buffer.get(i+minIdx+1));
                        temp.write((byte) buffer.get(i+maxIdx));
                        temp.write((byte) buffer.get(i+maxIdx+1));
                    }
                    else{
                        temp.write(buffer.get(i+maxIdx));
                        temp.write(buffer.get(i+maxIdx+1));
                        temp.write(buffer.get(i+minIdx));
                        temp.write(buffer.get(i+minIdx+1));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            temp.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }



    public ArrayList<Pair<Double,Double>> getMinimap(int canvasWidth){
        ArrayList<Pair<Double,Double>> minimap = new ArrayList<>();
        int increment = (int)Math.floor((buffer.capacity()/2)/(double)canvasWidth);
        for(int i = 0; i < buffer.capacity(); i+= increment*AudioInfo.SIZE_OF_SHORT){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            //compute the average
            for(int j = 0; j < increment*AudioInfo.SIZE_OF_SHORT; j+=AudioInfo.SIZE_OF_SHORT){
                if((i+j+1) < buffer.capacity()) {
                    //System.out.println("Capacity is: " + buffer.capacity() + ", i is : " + i + ", j is : " + j + ", i+j+1="+(i+j+1));
                    byte low = buffer.get(i + j);
                    byte hi = buffer.get(i + j + 1);
                    short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                    max = (max < (double) value) ? value : max;
                    min = (min > (double) value) ? value : min;
                }
            }
            minimap.add(new Pair<>(max, min));
        }
        return minimap;
    }


    private void setSampleStartIndex(int startSecond, int increment){
        int incInSec = (int)Math.floor(AudioInfo.SAMPLERATE / increment);
        startIndex = incInSec * startSecond;
    }
    public int getSampleStartIndex(){
        return startIndex;
    }

    public static int positionToWindowStart(int position){
        int second = (int)Math.floor(position/1000.0);
        return Math.max(second-2, 0);
    }

    private void sampleAudioChunk(byte[] chunk){
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < chunk.length; i+= AudioInfo.BLOCKSIZE){
            byte low = chunk[i];
            byte hi = chunk[i+1];
            //wav file written in little endian
            short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
            max = (i==0)? value : max;
            min = (i==0)? value : min;
            max = (value > max)? value : max;
            min = (value < min)?  value : min;
        }
        //samples.add(new Pair<>(max, min));
        //largest = (Math.abs(min) > largest)? Math.abs(min) : largest;
        //largest = (Math.abs(max) > largest)? max : largest;
    }

    private void processHeader(){
        if(wavFile.length > AudioInfo.HEADER_SIZE) {
            int blockSize = (int) wavFile[32] & 0xFF;
            int part1 = (((int) wavFile[27] & 0xFF) << 24) & 0xFF000000;
            int part2 = (((int) wavFile[26] & 0xFF) << 16) & 0x00FF0000;
            int part3 = (((int) wavFile[25] & 0xFF) << 8) & 0x0000FF00;
            int part4 = ((int) wavFile[24] & 0xFF) & 0x000000FF;
            int sampleRate = part1 | part2 | part3 | part4;
            System.out.println("Sample rate is : " + sampleRate + " hz; Number of channels is ");
        }
    }
    private void parseAudio(){
        audioData = new short[(wavFile.length-AudioInfo.HEADER_SIZE)/ AudioInfo.BLOCKSIZE];
        int index = 0;
        for(int i = AudioInfo.HEADER_SIZE; i < wavFile.length; i+=AudioInfo.BLOCKSIZE){
            for(int j = 0; j < AudioInfo.NUM_CHANNELS; j++){
                byte low = wavFile[i+j];
                byte hi = wavFile[i+j+1];

                //wav file written in little endian
                short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                audioData[index] = value;
                max = (i==0 && j ==0)? value : max;
                min = (i==0 && j ==0)? value : min;
                max = (value > max)? value : max;
                min = (value < min)?  value : min;
            }
            index++;
        }
    }
    public int getSampleRate(){ return AudioInfo.SAMPLERATE; }
    public short[] getAudioData(){ return audioData; }

    public int getNumChannels() {
        return AudioInfo.NUM_CHANNELS;
    }

    public int getLargest() {
        return largest;
    }


}
