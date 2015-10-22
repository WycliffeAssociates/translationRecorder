package wycliffeassociates.recordingapp.Recording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.AudioInfo;




public class WavFileWriter extends Service{

    ArrayList<Byte> byteArrayList;
    byte[] dataFromQueue;
    private String filename = null;
    private String visTempFile = "/storage/emulated/0/AudioRecorder/visualization.tmp";
    private boolean stoppedRecording = false;
    public static int largest = 0;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        filename = intent.getStringExtra("audioFileName");
        final int screenWidth = intent.getIntExtra("screenWidth", 0);
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

        Thread compressionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int increment = (int)Math.floor((AudioInfo.SAMPLERATE * 5)/screenWidth)*AudioInfo.SIZE_OF_SHORT;
                System.out.println("Increment is " + increment);
                boolean stopped = false;
                byteArrayList = new ArrayList<>(10000);
                try {
                    FileOutputStream compressedFile = new FileOutputStream(visTempFile);
                    while(!stopped){
                        RecordingMessage message = RecordingQueues.compressionQueue.take();
                        if(message.isStopped()){
                            stopped = true;
                            stoppedRecording = true;
                            writeDataReceivedSoFar(compressedFile, byteArrayList, increment);
                            compressedFile.close();
                        }
                        else {
                            if (!message.isPaused()){
                                //compressedFile.write(message.getData());
                                dataFromQueue = message.getData();
                                for(byte x : dataFromQueue){
                                    byteArrayList.add(new Byte(x));
                                }

                                writeDataReceivedSoFar(compressedFile, byteArrayList, increment);
                            }
                            else
                                System.out.println("paused writing");
                        }
                    }
                    System.out.println("writing to file");
                    RecordingQueues.compressionQueue.clear();
                    RecordingQueues.doneWritingCompressed.put(new Boolean(true));
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
        compressionThread.start();
        writingThread.start();

        return START_STICKY;
    }


    private void writeDataReceivedSoFar(FileOutputStream compressedFile, ArrayList<Byte> list, int increment){
        byte[] data = new byte[increment];
        byte[] minAndMax = new byte[2*AudioInfo.SIZE_OF_SHORT];
        //while there is more data in the arraylist than one increment
        while(list.size() >= increment){
            //remove that data and put it in an array for min/max computation
            for(int i = 0; i < increment; i++){
                data[i] = list.remove(0);
            }
            //write the min/max to the minAndMax array
            getMinAndMaxFromArray(data, minAndMax);
            try {
                //write the minAndMax array to the compressed file
                compressedFile.write(minAndMax);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //if the recording was stopped and there is less data than a full increment, grab the remaining data
        if(stoppedRecording){
            byte[] remaining = new byte[list.size()];
            for(int i = 0; i < list.size(); i++){
                remaining[i] = list.remove(0);
            }
            getMinAndMaxFromArray(remaining, minAndMax);
            try {
                compressedFile.write(minAndMax);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getMinAndMaxFromArray(byte[] data, byte[] minAndMax){
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int minIdx = 0;
        int maxIdx = 0;
        for(int j = 0; j < data.length; j+=AudioInfo.SIZE_OF_SHORT){
            if((j+1) < data.length) {
                byte low = data[j];
                byte hi = data[j + 1];
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
        minAndMax[0] = data[minIdx];
        minAndMax[1] = data[minIdx+1];
        minAndMax[2] = data[maxIdx];
        minAndMax[3] = data[maxIdx+1];

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
