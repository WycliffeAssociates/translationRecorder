package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;

public class RecordingScreen extends Activity {
    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";


    private final Context context = this;
    private TextView filenameView;
    private WaveformView mainCanvas;
    private MinimapView minimap;
    private UIDataManager manager;
    private PreferencesManager pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean isSaved = false;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);
        suggestedFilename = (String) pref.getPreferences("fileName") + "-" + pref.getPreferences("fileCounter").toString();

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        manager = new UIDataManager(mainCanvas, minimap, this, UIDataManager.RECORDING_MODE, true);

        setButtonHandlers();
        enableButtons();

        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(false);

        filenameView = (TextView) findViewById(R.id.filenameView);
        filenameView.setText(suggestedFilename);
    }

    private void pauseRecording() {
        isPausedRecording = true;
        manager.pauseTimer();
        isRecording = false;
        manager.swapPauseAndRecord();
        stopService(new Intent(this, WavRecorder.class));
        RecordingQueues.pauseQueues();
    }

    private void startRecording() {
        stopService(new Intent(this, WavRecorder.class));
        manager.swapPauseAndRecord();
        isRecording = true;
        manager.setIsRecording(true);

        if (!isPausedRecording) {
            manager.startTimer();
            isSaved = false;
            RecordingQueues.clearQueues();
            Intent intent = new Intent(this, WavFileWriter.class);
            intent.putExtra("audioFileName", getFilename());
            intent.putExtra("screenWidth", mainCanvas.getWidth());
            startService(new Intent(this, WavRecorder.class));
            startService(intent);
            manager.listenForRecording(true);
        } else {
            manager.resumeTimer();
            isPausedRecording = false;
            startService(new Intent(this, WavRecorder.class));
        }
    }

    private void stopRecording() {
        if(isPausedRecording || isRecording) {
            //Stop recording, load the recorded file, and draw
            stopService(new Intent(this, WavRecorder.class));
            long start = System.currentTimeMillis();
            System.out.println("Stopping");
            RecordingQueues.stopQueues();
            System.out.println("took " + (System.currentTimeMillis() - start) + " to finish writing");
            Intent intent = new Intent(this, PlaybackScreen.class);
            System.out.println("passing as extra " + recordedFilename);
            intent.putExtra("recordedFilename", recordedFilename);
            isRecording = false;
            isPausedRecording = false;
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isSaved) {
            FragmentManager fm = getFragmentManager();
            FragmentExitDialog d = new FragmentExitDialog();
            d.setFilename(recordedFilename);
            if (isRecording) {
                d.setIsRecording(true);
                isRecording = false;
            }
            if (isPausedRecording) {
                d.setIsPausedRecording(true);
            }
            d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            d.show(fm, "Exit Dialog");

//            ExitDialog dialog = new ExitDialog(this, R.style.Theme_UserDialog);
//            dialog.setFilename(recordedFilename);
//            if (isRecording) {
//                dialog.setIsRecording(true);
//                isRecording = false;
//            }
//            if (isPausedRecording) {
//                dialog.setIsPausedRecording(true);
//            }
//            dialog.show();

        } else {
            super.onBackPressed();
        }
    }

    /**
     * Retrieves the filename of the recorded audio file.
     * If the AudioRecorder folder does not exist, it is created.
     *
     * @return the absolute filepath to the recorded .wav file
     */
    public String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        if (recordedFilename != null)
            return (file.getAbsolutePath() + "/" + recordedFilename);
        else {
            recordedFilename = (file.getAbsolutePath() + "/" + UUID.randomUUID().toString() + AUDIO_RECORDER_FILE_EXT_WAV);
            System.out.println("filename is " + recordedFilename);
            return recordedFilename;
        }
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnRecording, true);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPauseRecording, true);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            System.out.println("Pressed something");
            switch (v.getId()) {
                case R.id.btnRecording: {
                    System.out.println("Pressed Record");
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    stopRecording();
                    break;
                }
                case R.id.btnPauseRecording: {
                    pauseRecording();
                    break;
                }
            }
        }
    };
}
