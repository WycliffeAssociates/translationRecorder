package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
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


public class MainActivity extends Activity {
    Button play,stop,record;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;
    private String recordedFilename = null;
    private String loadedAudioFilename = null;
    private WavRecorder recorder = null;
    private WavPlayer player = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        player = new WavPlayer();

        setButtonHandlers();
        enableButtons(false);
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

    private void startRecording(){
        recorder = new WavRecorder();
        recorder.record();
    }
    private void stopRecording(){
        recorder.stop();
        recordedFilename= recorder.saveFile("test");
        Toast.makeText(getApplicationContext(), "result is " + recordedFilename, Toast.LENGTH_LONG).show();

        recorder = null;

    }
    private void playRecording(){
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
        player.play(recordedFilename);
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

