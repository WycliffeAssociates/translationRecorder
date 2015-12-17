package wycliffeassociates.recordingapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import wycliffeassociates.recordingapp.AudioVisualization.WavVisualizer;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.Recording.WavFileWriter;
import wycliffeassociates.recordingapp.Reporting.Logger;

public class WavFileLoader {

    private volatile boolean threadFinished = false;
    private boolean LOADED_FILE = true;
    private boolean RECORDED_FILE = false;
    private byte[] wavFile;
    public short[] audioData;
    private int min;
    private int max;
    //number of datapoints per second that should actually be useful: 44100 samples per sec, div by 20 = 2205 * 2 channels = 4410
    //this is enough for a resolution of 2k pixel width to display one second of wav across
    private final int DATA_CHUNK = 2* AudioInfo.SAMPLERATE / 1000;
    private int largest = 10000;
    private MappedByteBuffer buffer;
    private MappedByteBuffer mappedAudioFile;
    private MappedByteBuffer preprocessedBuffer;
    private int startIndex;
    private String visTempFile = "visualization.tmp";
    private int screenWidth = 1;
    private File audioVisFile;
    final String loadedFilename;

    public MappedByteBuffer getMappedAudioFile(){
        return mappedAudioFile;
    }
    public MappedByteBuffer getMappedFile(){
        return buffer;
    }
    public MappedByteBuffer getMappedCacheFile(){
        return preprocessedBuffer;
    }

    /**
     * Maps the visualization file to memory if the thread is finished, and returns true
     * returns false if the thread is not finished
     * @return returns whether or not the thread generating the visualization file is finished
     */
    public boolean visFileLoaded(){
        if(threadFinished){
            try {
                RandomAccessFile rafCached = new RandomAccessFile(audioVisFile, "r");
                FileChannel fcCached = rafCached.getChannel();
                preprocessedBuffer = fcCached.map(FileChannel.MapMode.READ_ONLY, 0, rafCached.length());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return threadFinished;
    }

    /**
     * Constructs an object to load a wav file and set up memory mapped access to it as well as to
     * visualization files. Creates a map of the uncompressed file for drawing and a separate map
     * for audio playback. If a visualization file exists, it is loaded and mapped, otherwise
     * a thread is spawned to generate one in the background.
     * @param file name of the file to be loaded
     * @param screenWidth width of the screen for use in visualization
     * @param loadedFile whether or not the file was loaded or had just been recorded
     */
    public WavFileLoader(String file, int screenWidth, boolean loadedFile) {
        loadedFilename = file;
        this.screenWidth = screenWidth;
        threadFinished = false;
        RandomAccessFile raf = null;
        try {
            Logger.i(WavFileLoader.class.toString(), "Loading the file: " + file);
            raf = new RandomAccessFile(file, "r");
            FileChannel fc = raf.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE,
                    raf.length() - AudioInfo.HEADER_SIZE);
            mappedAudioFile = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE,
                    raf.length() - AudioInfo.HEADER_SIZE);

            //If the file was loaded, look for a .vis file with the same name
            if(loadedFile == LOADED_FILE) {
                audioVisFile = new File(AudioInfo.pathToVisFile + file.substring(file.lastIndexOf('/'),
                        file.lastIndexOf('.')) + ".vis");
            //Otherwise use visualization.vis
            } else{
                audioVisFile = new File(AudioInfo.pathToVisFile, "visualization.vis");
                Logger.i(WavFileLoader.class.toString(), "Using the default visualization file");
            }
            //If the visualization file exists, map it to memory
            if(audioVisFile.exists()) {
                RandomAccessFile rafCached = new RandomAccessFile(audioVisFile, "r");
                FileChannel fcCached = rafCached.getChannel();
                preprocessedBuffer = fcCached.map(FileChannel.MapMode.READ_ONLY, 0, rafCached.length());
                Logger.i(WavFileLoader.class.toString(), "Found a matching visualization file: "
                        + audioVisFile.getPath());
            //otherwise spawn a thread to generate the vis file file
            } else {
                preprocessedBuffer = null;
                Thread writeVisFile = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.w(WavFileLoader.class.toString(), "Could not find a matching vis file, creating...");
                        generateTempFile(loadedFilename);
                        Logger.w(WavFileLoader.class.toString(), "Finished creating a vis file");
                        threadFinished = true;
                    }
                });
                writeVisFile.start();
            }
            //TODO: find where largest should be updating, currently it seems to stay at 10k
            System.out.println("Largest value from file is " + largest);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void generateTempFile(String loadedFilename){
        try {
            audioVisFile  = new File(AudioInfo.pathToVisFile + loadedFilename.substring(loadedFilename.lastIndexOf('/'), loadedFilename.lastIndexOf('.')) + ".vis");
            FileOutputStream temp = new FileOutputStream(audioVisFile);
            int increment = (int)Math.floor((AudioInfo.SAMPLERATE * AudioInfo.COMPRESSED_SECONDS_ON_SCREEN)/screenWidth);
            increment = (increment % 2 == 0)? increment : increment+1;
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

    public float[] getMinimap(int canvasWidth, int canvasHeight) {
        System.out.println("minimap width is " + canvasWidth + " height is " + canvasHeight);
        //*4 because 2x values and 2y values for each pixel of width
        float[] minimap = new float[canvasWidth*4];
        int increment = (int)Math.floor((buffer.capacity()/2)/(double)canvasWidth);
        int idx = 0;
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
            if(idx < minimap.length) {
                minimap[idx] = idx/4;
                minimap[idx + 1] = (float)((max* WavVisualizer.getYScaleFactor(canvasHeight, largest)) + canvasHeight / 2);
                minimap[idx + 2] = idx/4;
                minimap[idx + 3] = (float)((min* WavVisualizer.getYScaleFactor(canvasHeight, largest)) + canvasHeight / 2);
            }
            idx+=4;
        }
        System.out.println("idx is " + idx);
        return minimap;
    }

    public WavFileLoader cut(int start, int end){
        WavPlayer.stop();
        try {
            File tempAudioFile = new File(AudioInfo.fileDir, loadedFilename+"c-");
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            System.out.println("Trying to cut from " +start+ " to " + end);
            for(int i = 0; i < AudioInfo.HEADER_SIZE; i++){
                fos.write(0);
            }
            for(int i = 0; i < start; i++){
                fos.write(mappedAudioFile.get(i));
            }
            for(int i = end; i < mappedAudioFile.capacity(); i++){
                fos.write(mappedAudioFile.get(i));
            }
            fos.close();
            System.out.println("new size is " + tempAudioFile.length() + " was originally " + (mappedAudioFile.capacity()+44));
            WavFileWriter.overwriteHeaderData(tempAudioFile.getAbsolutePath(), tempAudioFile.length());
            mappedAudioFile = null;
            buffer = null;
            preprocessedBuffer = null;
            File original = new File(AudioInfo.fileDir, loadedFilename);
            File originalVis = new File(AudioInfo.pathToVisFile, loadedFilename.substring(0, loadedFilename.lastIndexOf('.'))+".vis");
            if(originalVis.exists()){
                originalVis.delete();
            }
            File tempVis = new File(AudioInfo.pathToVisFile, "visualization.vis");
            if(tempVis.exists()) {
                tempVis.delete();
            }
            System.out.println(original.delete() + " to deleteing the file");
            System.out.println(tempAudioFile.renameTo(new File(AudioInfo.fileDir, loadedFilename)));
            System.out.println(tempAudioFile.getAbsolutePath());
            WavFileLoader cutFile = new WavFileLoader(AudioInfo.fileDir + loadedFilename, screenWidth, false);
            WavPlayer.loadFile(cutFile.getMappedAudioFile());
            return cutFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

/*
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
*/

}