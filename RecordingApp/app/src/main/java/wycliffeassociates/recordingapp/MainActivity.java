package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {
    private String recordedFilename = null;
    private WavRecorder recorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonHandlers();
        enableButtons(false);
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnRecord).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnRecord, !isRecording);
        enableButton(R.id.btnStop, isRecording);
        enableButton(R.id.btnPlay, !isRecording);
    }

    private void startRecording(){
        Toast.makeText(getApplicationContext(), "Starting Recording", Toast.LENGTH_LONG).show();
        recorder = new WavRecorder();
        recorder.record();
    }
    private void stopRecording(){
        recorder.stop();
        recordedFilename= recorder.saveFile("test");
        Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_LONG).show();
        recorder = null;

    }
    private void playRecording(){
        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
        WavPlayer.play(recordedFilename);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnRecord:{
                    enableButtons(true);
                    startRecording();

                    break;
                }
                case R.id.btnStop:{
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

