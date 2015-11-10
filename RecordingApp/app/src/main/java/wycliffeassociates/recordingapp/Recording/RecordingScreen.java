package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
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
    private GestureDetectorCompat mDetector;
    private TextView filenameView;
    private WaveformView mainCanvas;
    private MinimapView minimap;
    private UIDataManager manager;
    private PreferencesManager pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean hasNotYetRecorded = true;
    private boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;
    private boolean isPausedPlayback = false;
    private RotateAnimation anim;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private int startPosition = 0;
        private int endPosition = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            if (isRecording || hasNotYetRecorded) {
                return true;
            }
            if (WavPlayer.exists() && e.getY() <= minimap.getHeight()) {
                minimap.setPlaySelectedSection(false);
                float xPos = e.getX() / minimap.getWidth();
                int timeToSeekTo = Math.round(xPos * WavPlayer.getDuration());
                WavPlayer.seekTo(timeToSeekTo);
                manager.updateUI();
                endPosition = (int) e.getX();
                WavPlayer.stopAt(WavPlayer.getDuration());
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if (isRecording || hasNotYetRecorded) {
                return true;
            }
            if (WavPlayer.exists() && event1.getY() <= minimap.getHeight()) {
                startPosition = (int) event1.getX();
                endPosition -= (int) distanceX;
                minimap.setPlaySelectedSection(true);
                minimap.setStartOfPlaybackSection(startPosition);
                minimap.setEndOfPlaybackSection(endPosition);
                int playbackSectionStart = (int) ((startPosition / (double) minimap.getWidth()) * WavPlayer.getDuration());
                int playbackSectionEnd = (int) ((endPosition / (double) minimap.getWidth()) * WavPlayer.getDuration());
                if (startPosition > endPosition) {
                    int temp = playbackSectionEnd;
                    playbackSectionEnd = playbackSectionStart;
                    playbackSectionStart = temp;
                }
                WavPlayer.seekTo(playbackSectionStart);
                WavPlayer.stopAt(playbackSectionEnd);
                //WavPlayer.selectionStart(playbackSectionStart);
                manager.updateUI();
            }
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);
        suggestedFilename = (String) pref.getPreferences("fileName") + "-" + pref.getPreferences("fileCounter").toString();

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        manager = new UIDataManager(mainCanvas, minimap, this);

        setButtonHandlers();
        enableButtons();

        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(false);

        filenameView = (TextView) findViewById(R.id.filenameView);
        filenameView.setText(suggestedFilename);
        hasNotYetRecorded = true;
        manager.useRecordingToolbar(true);
    }

    private void pauseRecording() {
        isPausedRecording = true;
        manager.pauseTimer();
        isRecording = false;
        manager.swapPauseAndRecord();
        stopService(new Intent(this, WavRecorder.class));
        RecordingQueues.pauseQueues();
    }

    private void pausePlayback() {
        isPausedPlayback = true;
        manager.swapPauseAndPlay();
        WavPlayer.pause();
    }

    private void skipForward() {
        WavPlayer.seekTo(WavPlayer.getDuration());
        manager.updateUI();
    }

    private void skipBack() {
        WavPlayer.seekToStart();
        manager.updateUI();
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
        isRecording = false;
        isPausedRecording = false;
        hasNotYetRecorded = false;

        //Switch the toolbar to display
        manager.useRecordingToolbar(false);

        //Stop recording, load the recorded file, and draw
        stopService(new Intent(this, WavRecorder.class));
        long start = System.currentTimeMillis();
        System.out.println("Stopping");
        RecordingQueues.stopQueues();
        manager.loadWavFromFile(recordedFilename);
        System.out.println("took " + (System.currentTimeMillis() - start) + " to finish writing");
        manager.updateUI();
    }

    private void playRecording() {
        manager.swapPauseAndPlay();
        isPlaying = true;
        isPausedPlayback = false;
        WavPlayer.play();
        manager.updateUI();
    }

    @Override
    public void onBackPressed() {
        if (!isSaved) {
            ExitDialog dialog = new ExitDialog(this, R.style.Theme_UserDialog);
            dialog.setFilename(recordedFilename);
            if (isRecording) {
                dialog.setIsRecording(true);
                isRecording = false;
            }
            if (isPlaying) {
                dialog.setIsPlaying(true);
                isPlaying = false;
            }
            if (isPausedRecording) {
                dialog.setIsPausedRecording(true);
            }
            dialog.show();
        } else {
            WavPlayer.release();
            super.onBackPressed();
        }
    }

    private boolean getSaveName(Context c) {
        final EditText toSave = new EditText(c);
        toSave.setInputType(InputType.TYPE_CLASS_TEXT);

        //pref.getPreferences("fileName");
        toSave.setText(suggestedFilename, TextView.BufferType.EDITABLE);

        //prepare the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Save as");
        builder.setView(toSave);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setName(toSave.getText().toString());
                //SAVE FILE HERE
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    private void setName(String newName) {
        suggestedFilename = newName;
        isSaved = true;
        recordedFilename = saveFile(suggestedFilename);
        filenameView.setText(suggestedFilename);
    }

    public String getName() {
        return suggestedFilename;
    }

    private void saveRecording() {
        try {
            getSaveName(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Names the currently recorded .wav file.
     *
     * @param name a string with the desired output filename. Should not include the .wav extension.
     * @return the absolute path of the file created
     */
    public String saveFile(String name) {
        File dir = new File(pref.getPreferences("fileDirectory").toString());
        // System.out.println(recordedFilename);
        File from = new File(recordedFilename);
        File to = new File(dir, name + AUDIO_RECORDER_FILE_EXT_WAV);
        Boolean out = from.renameTo(to);
        recordedFilename = to.getAbsolutePath();
        pref.setPreferences("fileCounter", ((int) pref.getPreferences("fileCounter") + 1));
        return to.getAbsolutePath();
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
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnPause).setOnClickListener(btnClick);
        findViewById(R.id.btnSkipBack).setOnClickListener(btnClick);
        findViewById(R.id.btnSkipForward).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnRecording, true);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
        enableButton(R.id.btnPauseRecording, true);
        enableButton(R.id.btnPause, true);
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
                case R.id.btnPlay: {
                    playRecording();
                    break;
                }
                case R.id.btnSave: {
                    saveRecording();
                    break;
                }
                case R.id.btnPause: {
                    pausePlayback();
                    break;
                }
                case R.id.btnPauseRecording: {
                    pauseRecording();
                    break;
                }
                case R.id.btnSkipForward: {
                    skipForward();
                    break;
                }
                case R.id.btnSkipBack: {
                    skipBack();
                    break;
                }
            }
        }
    };
}
