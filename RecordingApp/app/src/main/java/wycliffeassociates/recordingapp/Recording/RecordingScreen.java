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
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioVisualization.CanvasView;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.Playback.PlaybackLogic;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;
import wycliffeassociates.recordingapp.AudioVisualization.RecordingTimer;

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
    private boolean minimapClicked = false;
    private boolean isPausedRecording = false;
    private boolean isPausedPlayback = false;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private int startPosition = 0;
        private int endPosition = 0;
        @Override
        public boolean onDown(MotionEvent e) {
            if(isRecording || hasNotYetRecorded){
                return true;
            }
            if(WavPlayer.exists() && e.getY() <= minimap.getHeight() ) {
                minimap.setPlaySelectedSection(false);
                float xPos = e.getX() / minimap.getWidth();
                int timeToSeekTo = Math.round(xPos * WavPlayer.getDuration());
                WavPlayer.seekTo(timeToSeekTo);
                manager.updateUI(true);
            }
            endPosition = (int)e.getX();
            minimapClicked = true;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if(isRecording || hasNotYetRecorded){
                return true;
            }
            startPosition = (int)event1.getX();
            endPosition -= (int)distanceX;
            minimap.setPlaySelectedSection(true);
            minimap.setStartOfPlaybackSection(startPosition);
            minimap.setEndOfPlaybackSection(endPosition);
            int playbackSectionStart = (int)((startPosition / (double)minimap.getWidth()) * WavPlayer.getDuration());
            int playbackSectionEnd = (int)((endPosition / (double)minimap.getWidth()) * WavPlayer.getDuration());
            if(startPosition > endPosition) {
                int temp = playbackSectionEnd;
                playbackSectionEnd = playbackSectionStart;
                playbackSectionStart = temp;
            }
            WavPlayer.seekTo(playbackSectionStart);
            WavPlayer.stopAt(playbackSectionEnd);
            //WavPlayer.selectionStart(playbackSectionStart);
            manager.updateUI(true);
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
        suggestedFilename = (String)pref.getPreferences("fileName")+"-" +pref.getPreferences("fileCounter").toString();
//        recordedFilename = savedInstanceState.getString("outputFileName", null);
        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        mainCanvas = (CanvasView) findViewById(R.id.main_canvas) instanceof WaveformView ? ((WaveformView) findViewById(R.id.main_canvas)) : null;
        minimap = (CanvasView) findViewById(R.id.minimap) instanceof MinimapView ? ((MinimapView) findViewById(R.id.minimap)) : null;

        manager = new UIDataManager(mainCanvas, minimap, this);

        findViewById(R.id.volumeBar).setVisibility(View.VISIBLE);
        findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        setButtonHandlers();
        enableButtons(false);


        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(false);

        filenameView = (TextView)findViewById(R.id.filenameView);
        filenameView.setText(suggestedFilename);
        hasNotYetRecorded = true;
        manager.useRecordingToolbar(true);
    }

    private void saveRecording(){
        try {
            getSaveName(context);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSaveName(Context c){
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

    public void setName(String newName){
        suggestedFilename = newName;
        isSaved = true;
        recordedFilename = saveFile(suggestedFilename);
        filenameView.setText(suggestedFilename);
    }

    public String getName(){return suggestedFilename;}

    private void pauseRecording(){
        isPausedRecording = true;
        manager.pauseTimer();
        isRecording = false;
        manager.swapPauseAndPlayRecording(true);
        stopService(new Intent(this, WavRecorder.class));
        RecordingQueues.pauseQueues();
    }

    private void pausePlayback(){
        isPausedPlayback = true;
        manager.swapPauseAndPlayPlayback(true);
        WavPlayer.pause();
    }

    private void skipForward(){
        WavPlayer.seekTo(WavPlayer.getDuration());
        manager.updateUI(true);
    }

    private void skipBack(){
        WavPlayer.seekToStart();
        manager.updateUI(true);
    }

    private void startRecording() {
        stopService(new Intent(this, WavRecorder.class));
        manager.swapPauseAndPlayRecording(false);
        isRecording = true;
        manager.setIsRecording(true);

        if(!isPausedRecording) {
            manager.startTimer();
    	    isSaved = false;
    	    RecordingQueues.clearQueues();
            Intent intent = new Intent(this, WavFileWriter.class);
            intent.putExtra("audioFileName", getFilename());
            intent.putExtra("screenWidth", mainCanvas.getWidth());
            startService(new Intent(this, WavRecorder.class));
            startService(intent);
            manager.listenForRecording(true);
        }
        else {
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
        WavPlayer.loadFile(recordedFilename);
        manager.loadWavFromFile(recordedFilename);
        System.out.println("took " + (System.currentTimeMillis() - start) + " to finish writing");
        manager.updateUI(false);
    }

    private void playRecording(){
        manager.swapPauseAndPlayRecording(false);
        isPlaying = true;
        isPausedPlayback = false;
        WavPlayer.play();
        manager.updateUI(minimapClicked);
    }

    @Override
    public void onBackPressed() {
        if(!isSaved) {
            ExitDialog dialog = new ExitDialog(this, R.style.Theme_UserDialog);
            dialog.setFilename(recordedFilename);
            if(isRecording){
                dialog.setIsRecording(true);
                isRecording = false;
            }
            if(isPlaying){
                dialog.setIsPlaying(true);
                isPlaying = false;
            }
            if(isPausedRecording) {
                dialog.setIsPausedRecording(true);
            }
            dialog.show();
        }
        else
            super.onBackPressed();

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
        pref.setPreferences("fileCounter", ((int)pref.getPreferences("fileCounter")+1));
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
        if(recordedFilename != null)
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

    private void enableButton(int id,boolean isEnable){
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {

        enableButton(R.id.btnRecording, !isRecording);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
        enableButton(R.id.btnPauseRecording, isRecording);
        enableButton(R.id.btnPause, true);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            System.out.println("Pressed something");
            switch(v.getId()){
                case R.id.btnRecording:{
                    System.out.println("Pressed Record");
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
                    break;
                }
                case R.id.btnSave:{
                    saveRecording();
                    break;
                }
                case R.id.btnPause:{
                    enableButtons(true);
                    pausePlayback();
                    break;
                }
                case R.id.btnPauseRecording:{
                    enableButtons(false);
                    pauseRecording();
                    break;
                }
                case R.id.btnSkipForward:{
                    enableButtons(false);
                    skipForward();
                    break;
                }
                case R.id.btnSkipBack:{
                    enableButtons(false);
                    skipBack();
                    break;
                }
            }
        }
    };
}
