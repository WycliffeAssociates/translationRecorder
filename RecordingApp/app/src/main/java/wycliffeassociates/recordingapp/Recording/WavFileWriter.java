package wycliffeassociates.recordingapp.Recording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Reporting.Logger;


public class WavFileWriter extends Service{

    private String filename = null;
    private String nameWithoutExtension = null;
    private String visTempFile = "visualization.vis";
    private Project mProject;
    public static int largest = 0;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        final WavFile audioFile = intent.getParcelableExtra("wavfile");
        filename = intent.getStringExtra("audioFileName");
        mProject = intent.getParcelableExtra(Project.PROJECT_EXTRA);
        nameWithoutExtension = filename.substring(filename.lastIndexOf("/")+1, filename.lastIndexOf("."));
        Logger.w(this.toString(),"Passed in string name " + filename);
        Thread writingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopped = false;
                try {
                    FileOutputStream rawAudio = new FileOutputStream(filename, true);
                    while(!stopped){
                        RecordingMessage message = RecordingQueues.writingQueue.take();
                        if(message.isStopped()){
                            stopped = true;
                        }
                        else {
                            if(!message.isPaused())
                                rawAudio.write(message.getData());
                            else
                                System.out.println("paused writing");
                        }
                    }
                    audioFile.overwriteHeaderData();
                    audioFile.writeMetadata();
                    System.out.println("writing to file");
                    rawAudio.close();
                    RecordingQueues.writingQueue.clear();
                    Logger.e(this.toString(), "Writing queue finishing, sending done message");
                    RecordingQueues.doneWriting.put(new Boolean(true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e ) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread compressionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Logger.w(this.toString(), "starting compression thread");
                int increment = AudioInfo.COMPRESSION_RATE;
                //System.out.println("Increment is " + increment);
                boolean stopped = false;
                //int numRemoved = 0;
                //int count = 0;
                boolean stoppedRecording = false;
                ArrayList<Byte> byteArrayList = new ArrayList<>();
                try {
                    File dir = new File(AudioInfo.fileDir+"/Visualization");
                    if(!dir.exists()){
                        dir.mkdir();
                    }
                    File file = new File(AudioInfo.pathToVisFile + nameWithoutExtension +".vis");
                    if(!file.exists()){
                        file.createNewFile();
                        //Logger.w(this.toString(), "created a new vis file");
                    }
                    FileOutputStream compressedFile = new FileOutputStream(file);
                    while(!stopped){
                        //Logger.w(this.toString(), "continue loop");
                        RecordingMessage message = RecordingQueues.compressionQueue.take();
                        //Logger.w(this.toString(), "took a message from the queue");
                        if(message.isStopped()){
                            //Logger.w(this.toString(), "message contained stop");
                            stopped = true;
                            stoppedRecording = true;
                            //Logger.w(this.toString(), "writing remaining data");
                            writeDataReceivedSoFar(compressedFile, byteArrayList, increment, stoppedRecording);
                            compressedFile.close();
                            //Logger.w(this.toString(), "closing file");
                        }
                        else {
                            if (!message.isPaused()){
                                //Logger.w(this.toString(), "message contained data");
                                byte[] dataFromQueue = message.getData();
                                for(byte x : dataFromQueue){
                                    byteArrayList.add(new Byte(x));
                                    //count++;
                                }
                                if(byteArrayList.size() >= increment) {
                                    //Logger.w(this.toString(), "writing data to file");
                                    writeDataReceivedSoFar(compressedFile, byteArrayList, increment, stoppedRecording);
                                }
                            }
                            else{
                                //System.out.println("paused writing");
                            }
                        }
                    }
                    //Logger.w(this.toString(), "exited loop");
                    //System.out.println("total count was " + count + " num removed is " + numRemoved);
                    RecordingQueues.compressionQueue.clear();
                    //Logger.e(this.toString(), "Compression queue finishing, sending done message");
                    RecordingQueues.doneWritingCompressed.put(new Boolean(true));
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

    private void writeDataReceivedSoFar(FileOutputStream compressedFile, ArrayList<Byte> list, int increment, boolean stoppedRecording){
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
            System.out.println("Stopped recording, writing some remaining data");
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
        if(data.length < 4){ return; }
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

    public static void overwriteHeaderData(String filepath, long totalAudioLength, int metadataLength){
        overwriteHeaderData(new File(filepath), totalAudioLength, metadataLength);
    }

    public static void overwriteHeaderData(File filepath, long totalAudioLen, int metadataLength){
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
