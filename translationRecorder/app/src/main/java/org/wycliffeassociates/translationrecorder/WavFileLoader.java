package org.wycliffeassociates.translationrecorder;

import android.content.Context;

import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public class WavFileLoader {

    public ShortBuffer mapAndGetAudioBuffer() throws IOException {
        FileChannel fc = new FileInputStream(mAudioFile.getFile()).getChannel();
        ShortBuffer buff;
        MappedByteBuffer map = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE, mAudioFile.getTotalAudioLength());
        buff = map.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        return buff;
    }

    public ShortBuffer mapAndGetVisualizationBuffer() throws IOException {
        if (audioVisFile != null && audioVisFile.exists() && visualizationReady) {
            FileChannel fc = new FileInputStream(audioVisFile).getChannel();
            ShortBuffer buff;
            //visualization starts with 4 bytes that say "DONE"
            MappedByteBuffer map = fc.map(FileChannel.MapMode.READ_ONLY, 4, audioVisFile.length() - 4);
            buff = map.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            return buff;
        } else {
            return null;
        }
    }

    public interface OnVisualizationFileCreatedListener {
        void onVisualizationCreated(ShortBuffer mappedVisualizationFile);
    }

    private volatile boolean visualizationReady = false;
    private File audioVisFile;
    private WavFile mAudioFile;
    private OnVisualizationFileCreatedListener onVisualizationFileCreatedListener;

    public void setOnVisualizationFileCreatedListener(OnVisualizationFileCreatedListener listener) {
        onVisualizationFileCreatedListener = listener;
    }

    /**
     * Maps the visualization file to memory if the thread is finished, and returns true
     * returns false if the thread is not finished
     *
     * @return returns whether or not the thread generating the visualization file is finished
     */
    public void mapNewVisFile() {
        if (visualizationReady) {
            try {
                if (onVisualizationFileCreatedListener != null) {
                    onVisualizationFileCreatedListener.onVisualizationCreated(mapAndGetVisualizationBuffer());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs an object to load a wav file and set up memory mapped access to it as well as to
     * visualization files. Creates a map of the uncompressed file for drawing and a separate map
     * for audio playback. If a visualization file exists, it is loaded and mapped, otherwise
     * a thread is spawned to generate one in the background.
     *
     * @param wavFile file to be mapped
     */
    public WavFileLoader(WavFile wavFile, Context ctx) {
        visualizationReady = false;
        mAudioFile = wavFile;
        Logger.i(WavFileLoader.class.toString(), "Loading the file: " + wavFile.getFile());
        String filename = wavFile.getFile().getName();
        //If the file was loaded, look for a .vis file with the same name
        File visDir = new File(ctx.getExternalCacheDir(), "Visualization");
        audioVisFile = new File(visDir, filename.substring(0,
                filename.lastIndexOf('.')) + ".vis");

        //If the visualization file doesn't exist or didn't finish, create a new one
        if (!audioVisFile.exists() || !verifyVisualizationFile(audioVisFile)) {
            Thread writeVisFile = new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.w(WavFileLoader.class.toString(), "Could not find a matching vis file, creating...");
                    generateTempFile();
                    Logger.w(WavFileLoader.class.toString(), "Finished creating a vis file");
                    visualizationReady = true;
                }
            });
            writeVisFile.start();
        } else {
            visualizationReady = true;
            Logger.i(WavFileLoader.class.toString(), "Found a matching visualization file: "
                    + audioVisFile.getPath());
        }
    }

    private boolean verifyVisualizationFile(File file) {
        boolean verified = false;
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.read() == 'D' && fis.read() == 'O' && fis.read() == 'N' && fis.read() == 'E') {
                return true;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return verified;
    }

    private void generateTempFile() {
        try (
                RandomAccessFile raf = new RandomAccessFile(mAudioFile.getFile(), "r");
                FileOutputStream fos = new FileOutputStream(audioVisFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            FileChannel fc = raf.getChannel();
            //only map the PCM data; the file could contain metadata that should not be mapped
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, AudioInfo.HEADER_SIZE,
                    mAudioFile.getTotalAudioLength());

            bos.write(new byte[4]);
            int increment = AudioInfo.COMPRESSION_RATE;
            System.out.println(increment + "increment ");

            for (int i = 0; i < buffer.capacity() + increment; i += increment) {
                if (i >= buffer.capacity()) {
                    break;
                }
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
                //try {
                if (minIdx < maxIdx) {
                    bos.write(buffer.get(i + minIdx));
                    bos.write(buffer.get(i + minIdx + 1));
                    bos.write(buffer.get(i + maxIdx));
                    bos.write(buffer.get(i + maxIdx + 1));
                } else {
                    bos.write(buffer.get(i + maxIdx));
                    bos.write(buffer.get(i + maxIdx + 1));
                    bos.write(buffer.get(i + minIdx));
                    bos.write(buffer.get(i + minIdx + 1));
                }
                //}
            }
            bos.flush();
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (RandomAccessFile raf = new RandomAccessFile(audioVisFile, "rw")) {
            raf.seek(0);
            raf.write('D');
            raf.write('O');
            raf.write('N');
            raf.write('E');
            raf.close();
            visualizationReady = true;
            mapNewVisFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}