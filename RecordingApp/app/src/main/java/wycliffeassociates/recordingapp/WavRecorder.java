package wycliffeassociates.recordingapp;


import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;



/**
 * Contains the ability to record audio and output to a .wav file.
 * The file is written to a temporary .raw file, then upon a stop call
 * the file is copied into a .wav file with a UUID name.
 *
 * Recorded files can be renamed with a call to toSave()
 */
public class WavRecorder extends Service {
    //Constants for WAV format
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private String tempFileName = null; //does not contain path
    private String recordedFilename = null; //does contain path
    private byte data[];


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        isRecording = true;
        System.out.println("Starting recording service");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Starting recording service");
        Toast.makeText(getApplicationContext(), "Starting Recording", Toast.LENGTH_LONG).show();
        record();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(getApplicationContext(), "Stopping Recording Service", Toast.LENGTH_LONG).show();
        isRecording = false;
        recorder.release();
        super.onDestroy();
    }

    private void record(){
        bufferSize = AudioRecord.getMinBufferSize(44100, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                feedToQueues();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void feedToQueues(){
        data = new byte[bufferSize];
        int read = 0;
        while (isRecording){
            read = recorder.read(data, 0, bufferSize);

            if(AudioRecord.ERROR_INVALID_OPERATION != read){
                RecordingMessage temp = new RecordingMessage(data, false, false);
                try {
                    RecordingQueues.UIQueue.put(temp);
                    RecordingQueues.writingQueue.put(temp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}