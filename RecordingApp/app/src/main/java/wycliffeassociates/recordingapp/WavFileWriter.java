package wycliffeassociates.recordingapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


public class WavFileWriter extends Service{

    private String filename = null;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        filename = intent.getStringExtra("audioFileName");
        System.out.println("Passed in string name " + filename);
        Thread writingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean stopped = false;
                try {
                    FileOutputStream wavFile = new FileOutputStream(filename);
                    writeWaveFileHeaderPlaceholder(wavFile);
                    while(!stopped){
                        RecordingMessage message = RecordingQueues.writingQueue.take();
                        if(message.isStopped()){
                            stopped = true;
                        }
                        else {
                            if(!message.isPaused())
                                wavFile.write(message.getData());
                            else
                                System.out.println("paused writing");
                        }
                    }
                    System.out.println("writing to file");
                    wavFile.close();
                    File file = new File(filename);
                    long totalAudioLength = file.length();
                    overwriteHeaderData(filename, totalAudioLength);
                    RecordingQueues.writingQueue.clear();
                    RecordingQueues.doneWriting.put(new Boolean(true));
                    stopSelf();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e ) {
                    e.printStackTrace();
                }

            }
        });
        writingThread.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void overwriteHeaderData(String filepath, long totalDataLen){
        long totalAudioLen = totalDataLen - 36; //While the header is 44 bytes, 8 consist of the data subchunk header
        totalDataLen -= 8; //this subtracts out the data subchunk header
        try {
            RandomAccessFile fileAccessor = new RandomAccessFile(filepath, "rw");
            System.out.println("Passed in string name " + filename);
            //seek to header[4] to overwrite data length
            long longSampleRate = AudioInfo.SAMPLERATE;
            long byteRate = AudioInfo.BPP * AudioInfo.SAMPLERATE * AudioInfo.NUM_CHANNELS;
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
            header[15] = ' ' ;
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
            header[32] = (byte) (AudioInfo.NUM_CHANNELS * AudioInfo.BPP); // block align
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
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Writes the Wave header to a file
     *
     * @param out filename of the .wav file being created
     * @throws IOException
     */
    private void writeWaveFileHeaderPlaceholder(FileOutputStream out) throws IOException {
        byte[] header = new byte[44];
        out.write(header);
    }

}
