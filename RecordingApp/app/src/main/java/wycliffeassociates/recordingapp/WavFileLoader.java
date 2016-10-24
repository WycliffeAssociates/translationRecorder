package wycliffeassociates.recordingapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.wav.WavFile;

public class WavFileLoader {

    private volatile boolean threadFinished = false;
    private MappedByteBuffer buffer;
    private MappedByteBuffer mappedAudioFile;
    private MappedByteBuffer preprocessedBuffer;
    private File audioVisFile;

    public MappedByteBuffer getMappedAudioFile() {
        return mappedAudioFile;
    }

    public MappedByteBuffer getMappedFile() {
        return buffer;
    }

    public MappedByteBuffer getMappedCacheFile() {
        return preprocessedBuffer;
    }

    /**
     * Maps the visualization file to memory if the thread is finished, and returns true
     * returns false if the thread is not finished
     *
     * @return returns whether or not the thread generating the visualization file is finished
     */
    public boolean visFileLoaded() {
        if (threadFinished) {
            try {
                RandomAccessFile rafCached = new RandomAccessFile(audioVisFile, "r");
                FileChannel fcCached = rafCached.getChannel();
                preprocessedBuffer = fcCached.map(FileChannel.MapMode.READ_ONLY, 0, rafCached.length());
            } catch (IOException e) {
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
     *
     * @param wavFile file to be mapped
     */
    public WavFileLoader(WavFile wavFile) {
        threadFinished = false;
        RandomAccessFile raf;
        try {
            String filename = wavFile.getFile().getName();
            Logger.i(WavFileLoader.class.toString(), "Loading the file: " + wavFile.getFile());
            raf = new RandomAccessFile(wavFile.getFile(), "r");
            FileChannel fc = raf.getChannel();

            //only map the PCM data; the file could contain metadata that should not be mapped
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE,
                    AudioInfo.HEADER_SIZE + wavFile.getTotalAudioLength());
            mappedAudioFile = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE,
                    AudioInfo.HEADER_SIZE + wavFile.getTotalAudioLength());

            //If the file was loaded, look for a .vis file with the same name
            audioVisFile = new File(Utils.VISUALIZATION_DIR, filename.substring(0,
                    filename.lastIndexOf('.')) + ".vis");

            //If the visualization file exists, map it to memory
            if (audioVisFile.exists()) {
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
                        generateTempFile();
                        Logger.w(WavFileLoader.class.toString(), "Finished creating a vis file");
                        threadFinished = true;
                    }
                });
                writeVisFile.start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTempFile() {
        try {
            //audioVisFile  = new File(AudioInfo.pathToVisFile + loadedFilename.substring(loadedFilename.lastIndexOf('/'), loadedFilename.lastIndexOf('.')) + ".vis");
            FileOutputStream temp = new FileOutputStream(audioVisFile);
            int increment = AudioInfo.COMPRESSION_RATE;
            System.out.println(increment + "increment ");

            for (int i = 0; i < buffer.capacity(); i += increment) {
                int max = Integer.MIN_VALUE;
                int min = Integer.MAX_VALUE;
                int minIdx = 0;
                int maxIdx = 0;

                for (int j = 0; j < increment; j += AudioInfo.SIZE_OF_SHORT) {
                    if ((i + j + 1) < buffer.capacity()) {
                        byte low = buffer.get(i + j);
                        byte hi = buffer.get(i + j + 1);
                        short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                        if (max < value) {
                            max = value;
                            maxIdx = j;
                        }
                        if (min > value) {
                            min = value;
                            minIdx = j;
                        }
                    }
                }
                //order matters, rather than get the values themselves, find the index on this range and use that to write the values
                try {
                    if (minIdx < maxIdx) {
                        temp.write(buffer.get(i + minIdx));
                        temp.write(buffer.get(i + minIdx + 1));
                        temp.write(buffer.get(i + maxIdx));
                        temp.write(buffer.get(i + maxIdx + 1));
                    } else {
                        temp.write(buffer.get(i + maxIdx));
                        temp.write(buffer.get(i + maxIdx + 1));
                        temp.write(buffer.get(i + minIdx));
                        temp.write(buffer.get(i + minIdx + 1));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            temp.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}