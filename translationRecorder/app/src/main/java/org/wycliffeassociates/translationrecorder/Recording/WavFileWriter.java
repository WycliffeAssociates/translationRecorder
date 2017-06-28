package org.wycliffeassociates.translationrecorder.Recording;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.wycliffeassociates.translationrecorder.AudioInfo;
import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.wav.WavOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;


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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final WavFile audioFile = intent.getParcelableExtra(KEY_WAV_FILE);
        String name = audioFile.getFile().getName();
        nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
        Thread writingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopped = false;

                try (WavOutputStream rawAudio = new WavOutputStream(audioFile, true)) {
                    //WARNING DO NOT USE BUFFERED OUTPUT HERE, WILL CAUSE END LINE TO BE OFF IN PLAYBACK
                    while (!stopped) {
                        RecordingMessage message = RecordingQueues.writingQueue.take();
                        if (message.isStopped()) {
                            Logger.w(this.toString(), "raw audio thread received a stop message");
                            stopped = true;
                        } else {
                            if (!message.isPaused()) {
                                rawAudio.write(message.getData());
                            } else {
                                Logger.w(this.toString(), "raw audio thread received a onPauseRecording message");
                            }
                        }
                    }
                    Logger.w(this.toString(), "raw audio thread exited loop");
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
                } finally {
                    try {
                        RecordingQueues.doneWriting.put(new Boolean(true));
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "InterruptedException in finally of writing queue", e);
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
                LinkedList<Byte> byteArrayList = new LinkedList<>();
                try {
                    while (!stopped) {
                        RecordingMessage message = RecordingQueues.compressionQueue.take();
                        if (message.isStopped()) {
                            Logger.w(this.toString(), "Compression thread received a stop message");
                            stopped = true;
                            stoppedRecording = true;
                            Logger.w(this.toString(), "Compression thread writing remaining data");
                            writeDataReceivedSoFar(byteArrayList, increment, stoppedRecording);
                        } else {
                            if (!message.isPaused()) {
                                byte[] dataFromQueue = message.getData();
                                for (byte x : dataFromQueue) {
                                    byteArrayList.add(new Byte(x));
                                }
                                if (byteArrayList.size() >= increment) {
                                    writeDataReceivedSoFar(byteArrayList, increment, stoppedRecording);
                                }
                            } else {
                                Logger.w(this.toString(), "Compression thread received a onPauseRecording message");
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
                        RecordingQueues.doneCompressing.put(new Boolean(true));
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "InterruptedException in finally of Compression queue", e);
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread compressionWriterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopped = false;

                File dir = new File(getExternalCacheDir(), "Visualization");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, nameWithoutExtension + ".vis");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Logger.w(this.toString(), "created a new vis file");
                }
                try (FileOutputStream fos = new FileOutputStream(file);
                     BufferedOutputStream compressedFile = new BufferedOutputStream(fos);
                ) {
                    compressedFile.write(new byte[4]);
                    while (!stopped) {
                        RecordingMessage message = RecordingQueues.compressionWriterQueue.take();
                        if (message.isStopped()) {
                            Logger.w(this.toString(), "raw audio thread received a stop message");
                            stopped = true;
                        } else {
                            if (!message.isPaused()) {
                                compressedFile.write(message.getData());
                            } else {
                                Logger.w(this.toString(), "raw audio thread received a onPauseRecording message");
                            }
                        }
                    }
                    compressedFile.flush();
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                        raf.seek(0);
                        raf.write('D');
                        raf.write('O');
                        raf.write('N');
                        raf.write('E');
                        raf.close();
                    }
                    Logger.w(this.toString(), "raw audio thread exited loop");
                    RecordingQueues.compressionWriterQueue.clear();
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
                } finally {
                    try {
                        RecordingQueues.doneWritingCompressed.put(new Boolean(true));
                    } catch (InterruptedException e) {
                        Logger.e(this.toString(), "InterruptedException in finally of writing queue", e);
                        e.printStackTrace();
                    }
                }
            }
        });
        compressionThread.start();
        compressionWriterThread.start();
        writingThread.start();

        return START_STICKY;
    }

    private void writeDataReceivedSoFar(List<Byte> list, int increment, boolean stoppedRecording) throws IOException {
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
        }
        //if the recording was stopped and there is less data than a full increment, grab the remaining data
        if (stoppedRecording) {
            System.out.println("Stopped recording, writing some remaining data");
            byte[] remaining = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                remaining[i] = list.remove(0);
            }
            getMinAndMaxFromArray(remaining, minAndMax);
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
        try {
            RecordingQueues.UIQueue.put(new RecordingMessage(minAndMax, false, false));
            RecordingQueues.compressionWriterQueue.put(new RecordingMessage(minAndMax, false, false));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
