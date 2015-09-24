package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

import wycliffeassociates.recordingapp.model.RecordingTimer;

public class RecordingScreen extends Activity {
    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";


    private String recordedFilename = null;
    private WavRecorder recorder = null;
    final Context context = this;
    private String outputName = null;
    private WaveformView mainCanvas;
    private MinimapView minimap;
    private UIDataManager manager;
    private float userScale;
    private float xTranslation;
    private ScaleGestureDetector SGD;
    private GestureDetector gestureDetector;
    private GestureDetector clickMinimap;
    private boolean paused = false;
    private boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isRecording = false;
    private boolean minimapClicked = false;
    private PreferencesManager pref;
    RotateAnimation anim;
    private boolean isPausedRecording = false;
    private RecordingTimer timer;
    private TextView timerView;
    private TextView filenameView;
    private long timePaused = 0;
    private long totalTimePaused = 0;
    private boolean resetPlaybackThread = true;
    Thread playback;
    private long startTime = System.currentTimeMillis();



    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getPointerCount() > 1.0){
            SGD.onTouchEvent(ev);
        }
        else {
            //gestureDetector.onTouchEvent(ev);
            clickMinimap.onTouchEvent(ev);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);

        outputName = (String)pref.getPreferences("fileName")+"-" +pref.getPreferences("fileCounter").toString();

        //recordedFilename = savedInstanceState.getString("outputFileName", null);

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);
        userScale = 1.f;

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                xTranslation += distanceX;
                mainCanvas.invalidate();
                return true;
            }

        };

        GestureDetector.SimpleOnGestureListener clickListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if(WavPlayer.exists() && e.getY() <= minimap.getHeight() ) {
                    float xPos = e.getX() / mainCanvas.getWidth();
                    int timeToSeekTo = Math.round(xPos * WavPlayer.getDuration());
                    WavPlayer.seekTo(timeToSeekTo);
                    minimap.setMiniMarkerLoc((float) (xPos * minimap.getWidth()));
                }
                minimapClicked = true;
                return true;
            }

        };

        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                userScale *= detector.getScaleFactor();
                return true;
            }
        };

        //gestureDetector = new GestureDetector(this, gestureListener);
        SGD = new ScaleGestureDetector(this, scaleListener);
        clickMinimap = new GestureDetector(this, clickListener);

        mainCanvas = (CanvasView) findViewById(R.id.main_canvas) instanceof WaveformView ? ((WaveformView) findViewById(R.id.main_canvas)) : null;
        minimap = (CanvasView) findViewById(R.id.minimap) instanceof MinimapView ? ((MinimapView) findViewById(R.id.minimap)) : null;
        manager = new UIDataManager(mainCanvas, minimap, this);

        findViewById(R.id.volumeBar).setVisibility(View.VISIBLE);
        findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        setButtonHandlers();
        enableButtons(false);
        anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1500);

        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(false);
        timerView = (TextView)findViewById(R.id.timerView);
        timerView.invalidate();
        filenameView = (TextView)findViewById(R.id.filenameView);
        filenameView.setText(outputName);
    }



    private void setButtonHandlers() {
        findViewById(R.id.btnRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnPause).setOnClickListener(btnClick);
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
        toSave.setText(outputName, TextView.BufferType.EDITABLE);

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
        outputName = newName;
        isSaved = true;
        recordedFilename = saveFile(outputName);
        filenameView.setText(outputName);
    }

    public String getName(){return outputName;}

    private void pauseRecording(){
        paused = true;
        timer.pause();
        isRecording = false;
        findViewById(R.id.btnRecording).setVisibility(View.VISIBLE);
        findViewById(R.id.btnPauseRecording).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnPauseRecording).setAnimation(null);

        stopService(new Intent(this, WavRecorder.class));
        try {
            RecordingQueues.writingQueue.put(new RecordingMessage(null, true, false));
            RecordingQueues.UIQueue.put(new RecordingMessage(null, true, false));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isPausedRecording = true;
    }
    private void pausePlayback(){
        paused = true;
        timePaused = System.currentTimeMillis();
        findViewById(R.id.btnPause).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
        WavPlayer.pause();
    }

    private void startRecording(){
        stopService(new Intent(this, WavRecorder.class));
        findViewById(R.id.volumeBar).setVisibility(View.VISIBLE);
        findViewById(R.id.btnPauseRecording).setVisibility(View.VISIBLE);
        findViewById(R.id.btnRecording).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnPauseRecording).setAnimation(anim);
        isRecording = true;
        manager.setIsRecording(true);

        if(!paused) {
            timer = new RecordingTimer();
            timer.startTimer();
    	    isSaved = false;
    	    RecordingQueues.writingQueue.clear();
            Intent intent = new Intent(this, WavFileWriter.class);
            intent.putExtra("audioFileName", getFilename());
            startService(new Intent(this, WavRecorder.class));
            System.out.println("Started the service");
            startService(intent);
            manager.listenForRecording(true);

            //mainCanvas.listenForRecording(this);
        }
        else {
            timer.resume();
            paused = false;
            isPausedRecording = false;
            startService(new Intent(this, WavRecorder.class));
        }

        Thread timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    if(timerView!= null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(timer != null) {
                                    long t = timer.getTimeElapsed();
                                    String time = String.format("%02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
                                    timerView.setText(time);
                                    timerView.invalidate();
                                }
                            }
                        });
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timerThread.start();

    }
    private void stopRecording() {
        isRecording = false;
        isPausedRecording = false;
        if (isPlaying) {
            isPlaying = false;
            WavPlayer.stop();
        }
        else {
            timer = null;
            findViewById(R.id.volumeBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.linearLayout10).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
            stopService(new Intent(this, WavRecorder.class));
            try {
                RecordingQueues.UIQueue.put(new RecordingMessage(null, false, true));
                RecordingQueues.writingQueue.put(new RecordingMessage(null, false, true));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                Boolean done = RecordingQueues.doneWriting.take();
                if (done.booleanValue()) {

                    WavPlayer.loadFile(recordedFilename);
                    manager.loadWavFromFile(recordedFilename);
                    manager.drawWaveformDuringPlayback(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void playRecording(){
        findViewById(R.id.btnPlay).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnPause).setVisibility(View.VISIBLE);
        WavPlayer.play();
        System.out.println ("paused is " + paused + " reset is " + resetPlaybackThread);
        isPlaying = true;
        if(paused && !resetPlaybackThread){
            totalTimePaused += System.currentTimeMillis() - timePaused;
            paused = false;
        }
        else if(!resetPlaybackThread) {
            startTime = System.currentTimeMillis();
        }
        else{
            final int duration = WavPlayer.getDuration();
            System.out.println("Recreating playback thread");
            resetPlaybackThread = false;
            playback = new Thread(new Runnable() {
                @Override
                public void run() {
                    int location = 0;
                    int oldLoc = 0;
                    startTime = System.currentTimeMillis();
                    long playback = System.currentTimeMillis();
                    int locationSkippedTo = 0;
                    while (location < duration) {
                        oldLoc = location;
                        if (minimapClicked) {
                            locationSkippedTo = WavPlayer.getLocation();
                        } else if (paused) {
                            location = oldLoc;
                        } else {
                            location = (int) Math.min(System.currentTimeMillis() - playback - totalTimePaused - locationSkippedTo, WavPlayer.getDuration());
                            minimapClicked = false;
                        }

                        float locPercentage = (float) location / (float) WavPlayer.getDuration();
                        minimap.setMiniMarkerLoc(locPercentage * minimap.getWidth());
                        if (mainCanvas.isDoneDrawing()) {
                            manager.drawWaveformDuringPlayback(location);
                            if (System.currentTimeMillis() >= 3000 + startTime) {
                               // System.out.println("FPS is:" + mainCanvas.getFps() / (double) ((System.currentTimeMillis() - startTime) / 1000.0));
                                //System.out.println("Frames are " + mainCanvas.getFps());
                                mainCanvas.resetFPS();
                                startTime = System.currentTimeMillis();
                            }
                        }

                        //System.out.println("location is :" + location + "duration is :" + WavPlayer.getDuration() + "average is :" + average);

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pausePlayback();
                            WavPlayer.seekToStart();
                        }
                    });

                    paused = false;
                    resetPlaybackThread = true;
                    totalTimePaused = 0;
                    minimapClicked = false;
                    System.out.println("Out of playback thread");
                }
            });
            playback.start();
        }
    }

    @Override
    public void onBackPressed() {
        if(!isSaved) {
            exitdialog dialog = new exitdialog(this, R.style.Theme_UserDialog);
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
            }
        }
    };

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
}
