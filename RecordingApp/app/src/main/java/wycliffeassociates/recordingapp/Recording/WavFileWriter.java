package wycliffeassociates.recordingapp.Recording;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Reporting.Logger;


public class WavFileWriter extends Service {

    public static final String KEY_WAV_FILE = "wavfile";
    private String nameWithoutExtension = null;
    public static int largest = 0;

    public static Intent getIntent(Context ctx, WavFile wavFile) {
        Intent intent = new Intent(ctx, WavFileWriter.class);
        intent.putExtra(KEY_WAV_FILE, wavFile);
        return intent;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final WavFile audioFile = intent.getParcelableExtra(KEY_WAV_FILE);
        String name = audioFile.getFile().getName();
        nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
        Thread writingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopped = false;
                FileOutputStream rawAudio = null;
                try {
                    //WARNING DO NOT USE BUFFERED OUTPUT HERE, WILL CAUSE END LINE TO BE OFF IN PLAYBACK
                    rawAudio = new FileOutputStream(audioFile.getFile(), true);
                    while (!stopped) {
                        RecordingMessage message = RecordingQueues.writingQueue.take();
                        if (message.isStopped()) {
                            Logger.w(this.toString(), "raw audio thread received a stop message");
                            stopped = true;
                        } else {
                            if (!message.isPaused()) {
                                rawAudio.write(message.getData());
                            } else {
                                Logger.w(this.toString(), "raw audio thread received a pause message");
                            }
                        }
                    }
                    Logger.w(this.toString(), "raw audio thread exited loop");
                    audioFile.overwriteHeaderData();
                    audioFile.writeMetadata();
                    rawAudio.close();
                    Logger.w(this.toString(), "raw audio thread closed file");
                    RecordingQueues.writingQueue.clear();
                    Logger.e(this.toString(), "raw audio queue finishing, sending done message");
                } catch (FileNotFoundException e) {
                    Logger.e(this.toString(), "File not found exception in writing thread", e);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Logger.e(this.toString(), "Interrupted Exception in writing queue", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Logger.e(this.toString(), "IO Exception in writing queue", e);
                    e.printStackTrace();
                } catch (JSONException e) {
                    Logger.e(this.toString(), "JSON Exception in writing queue", e);
                    e.printStackTrace();
                } finally {
                    try {
                        RecordingQueues.doneWriting.put(new Boolean(true));
                        if (rawAudio != null) {
                            rawAudio.close();
                        }
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "InterruptedException in finally of writing queue", e);
                        e.printStackTrace();
                    } catch (IOException e) {
                        Logger.e(this.toString(), "IOException while closing the file", e);
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread compressionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.w(this.toString(), "starting compression thread");
                int increment = AudioInfo.COMPRESSION_RATE;
                boolean stopped = false;
                boolean stoppedRecording = false;
                ArrayList<Byte> byteArrayList = new ArrayList<>();
                try {
                    File dir = new File(AudioInfo.fileDir + "/Visualization");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(AudioInfo.pathToVisFile + nameWithoutExtension + ".vis");
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                        Logger.w(this.toString(), "created a new vis file");
                    }
                    //WARNING DO NOT USE BUFFERED OUTPUT HERE, WILL CAUSE END LINE TO BE OFF IN PLAYBACK
                    FileOutputStream compressedFile = new FileOutputStream(file);
                    while (!stopped) {
                        RecordingMessage message = RecordingQueues.compressionQueue.take();
                        if (message.isStopped()) {
                            Logger.w(this.toString(), "Compression thread received a stop message");
                            stopped = true;
                            stoppedRecording = true;
                            Logger.w(this.toString(), "Compression thread writing remaining data");
                            writeDataReceivedSoFar(compressedFile, byteArrayList, increment, stoppedRecording);
                            compressedFile.close();
                            Logger.w(this.toString(), "Compression thread closed file");
                        } else {
                            if (!message.isPaused()) {
                                byte[] dataFromQueue = message.getData();
                                for (byte x : dataFromQueue) {
                                    byteArrayList.add(new Byte(x));
                                }
                                if (byteArrayList.size() >= increment) {
                                    writeDataReceivedSoFar(compressedFile, byteArrayList, increment, stoppedRecording);
                                }
                            } else {
                                Logger.w(this.toString(), "Compression thread received a pause message");
                            }
                        }
                    }
                    Logger.w(this.toString(), "exited compression thread loop");
                    RecordingQueues.compressionQueue.clear();
                    Logger.w(this.toString(), "Compression queue finishing, sending done message");
                } catch (FileNotFoundException e) {
                    Logger.e(this.toString(), "File not found exception in compression thread", e);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Logger.e(this.toString(), "Interrupted Exception in compression queue", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Logger.e(this.toString(), "IO Exception in compression queue", e);
                    e.printStackTrace();
                } finally {
                    try {
                        RecordingQueues.doneWritingCompressed.put(new Boolean(true));
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "InterruptedException in finally of Compression queue", e);
                        e.printStackTrace();
                    }
                }
            }
        });
        compressionThread.start();
        writingThread.start();

        return START_STICKY;
    }

    private void writeDataReceivedSoFar(FileOutputStream compressedFile, ArrayList<Byte> list, int increment, boolean stoppedRecording) throws IOException {
        byte[] data = new byte[increment];
        byte[] minAndMax = new byte[2 * AudioInfo.SIZE_OF_SHORT];
        //while there is more data in the arraylist than one increment
        while (list.size() >= increment) {
            //remove that data and put it in an array for min/max computation
            for (int i = 0; i < increment; i++) {
                data[i] = list.remove(0);
            }
            //write the min/max to the minAndMax array
            getMinAndMaxFromArray(data, minAndMax);
            //write the minAndMax array to the compressed file
            compressedFile.write(minAndMax);

        }
        //if the recording was stopped and there is less data than a full increment, grab the remaining data
        if (stoppedRecording) {
            System.out.println("Stopped recording, writing some remaining data");
            byte[] remaining = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                remaining[i] = list.remove(0);
            }
            getMinAndMaxFromArray(remaining, minAndMax);
            compressedFile.write(minAndMax);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getMinAndMaxFromArray(byte[] data, byte[] minAndMax) {
        if (data.length < 4) {
            return;
        }
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int minIdx = 0;
        int maxIdx = 0;
        for (int j = 0; j < data.length; j += AudioInfo.SIZE_OF_SHORT) {
            if ((j + 1) < data.length) {
                byte low = data[j];
                byte hi = data[j + 1];
                short value = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                if (max < value) {
                    max = value;
                    maxIdx = j;
                    if (value > largest) {
                        largest = value;
                    }
                }
                if (min > value) {
                    min = value;
                    minIdx = j;
                }
            }
        }
        minAndMax[0] = data[minIdx];
        minAndMax[1] = data[minIdx + 1];
        minAndMax[2] = data[maxIdx];
        minAndMax[3] = data[maxIdx + 1];

    }

    public static void overwriteHeaderData(String filepath, long totalAudioLength, int metadataLength) {
        overwriteHeaderData(new File(filepath), totalAudioLength, metadataLength);
    }

    public static void overwriteHeaderData(File filepath, long totalAudioLen, int metadataLength) {
        //long totalAudioLen = totalDataLen - AudioInfo.HEADER_SIZE; //While the header is 44 bytes, 8 consist of the data subchunk header
        long totalDataLen = totalAudioLen + metadataLength - 8; //this subtracts out the data subchunk header
        try {
            RandomAccessFile fileAccessor = new RandomAccessFile(filepath, "rw");
            //seek to header[4] to overwrite data length
            long longSampleRate = AudioInfo.SAMPLERATE;
            long byteRate = (AudioInfo.BPP * AudioInfo.SAMPLERATE * AudioInfo.NUM_CHANNELS) / 8;
            byte[] header = new byte[44];

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // fmt  chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of fmt chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) AudioInfo.NUM_CHANNELS; // number of channels
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) ((AudioInfo.NUM_CHANNELS * AudioInfo.BPP) / 8); // block align
            header[33] = 0;
            header[34] = AudioInfo.BPP; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            fileAccessor.write(header);
            fileAccessor.close();
        } catch (FileNotFoundException e) {
            Logger.e("WavFileWriter", "File Not Found Exception in writing header", e);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.e("WavFileWriter", "IO Exception in writing header", e);
            e.printStackTrace();
        }
    }
}
