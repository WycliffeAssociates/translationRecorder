package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.os.Environment;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {
    Button play,stop,record;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;
    private String recordedFilename = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonHandlers();
        enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(44100,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT);
    }

    private void setButtonHandlers() {
        ((Button)findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnPlay)).setOnClickListener(btnClick);


    }

    private void enableButton(int id,boolean isEnable){
        ((Button)findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnRecord, !isRecording);
        enableButton(R.id.btnStop, isRecording);
        enableButton(R.id.btnPlay, !isRecording);
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }
    private void playRecording(){
        MediaPlayer m = new MediaPlayer();

        try {
            m.setDataSource(recordedFilename);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m.prepare();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        m.start();
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
    }

    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(),getFilename());
        deleteTempFile();
    }
    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        recordedFilename = outFilename;

        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
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
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ' ;
        header[16] = 16; // 4 bytes: size of ‘fmt ‘ chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnRecord:{
                    //AppLog.logString("Start Recording");

                    enableButtons(true);
                    startRecording();

                    break;
                }
                case R.id.btnStop:{
                    //AppLog.logString("Start Recording");

                    enableButtons(false);
                    stopRecording();

                    break;
                }
                case R.id.btnPlay:{
                    enableButtons(false);
                    playRecording();
                }
            }
        }
    };
}

/*
      play=(Button)findViewById(R.id.button3);
      stop=(Button)findViewById(R.id.button2);
      record=(Button)findViewById(R.id.button);

      stop.setEnabled(false);
      play.setEnabled(false);
      outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

      myAudioRecorder=new MediaRecorder();
      myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
      myAudioRecorder.setOutputFile(outputFile);

      record.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            try {
               myAudioRecorder.prepare();
               myAudioRecorder.start();
            }

            catch (IllegalStateException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            record.setEnabled(false);
            stop.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
         }
      });

      stop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder  = null;

            stop.setEnabled(false);
            play.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Audio recorded successfully",Toast.LENGTH_LONG).show();
         }
      });

      play.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) throws IllegalArgumentException,SecurityException,IllegalStateException {
            MediaPlayer m = new MediaPlayer();

            try {
               m.setDataSource(outputFile);
            }

            catch (IOException e) {
               e.printStackTrace();
            }

            try {
               m.prepare();
            }

            catch (IOException e) {
               e.printStackTrace();
            }

            m.start();
            Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
         }
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.

      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

}
*/