package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

public class CanvasScreen extends Activity {
    //Constants for WAV format
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private String recordedFilename = null;
    private WavRecorder recorder = null;
    final Context context = this;
    private String outputName = null;
    private CanvasView mainCanvas;
    private CanvasView minimap;
    private float userScale;
    private float xTranslation;
    private ScaleGestureDetector SGD;
    private GestureDetector gestureDetector;
    private boolean paused = false;
    private boolean isSaved = false;
    private boolean isPlaying = false;



    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getPointerCount() > 1.0){
            SGD.onTouchEvent(ev);
        }
        else {
            gestureDetector.onTouchEvent(ev);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recordedFilename = savedInstanceState.getString("outputFileName", null);

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);
        userScale = 1.f;

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                xTranslation += distanceX;
                mainCanvas.setXTranslation(xTranslation);
                mainCanvas.invalidate();
                return true;
            }

        };

        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                userScale *= detector.getScaleFactor();
                mainCanvas.setUserScale(userScale);
                mainCanvas.invalidate();

                return true;
            }
        };

        gestureDetector = new GestureDetector(this, gestureListener);
        SGD = new ScaleGestureDetector(this, scaleListener);


        mainCanvas = (CanvasView) findViewById(R.id.main_canvas);
        minimap = (CanvasView) findViewById(R.id.minimap);
        setButtonHandlers();
        enableButtons(false);
    }



    private void setButtonHandlers() {
        findViewById(R.id.btnRecord).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
        //findViewById(R.id.btnPause).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {

        enableButton(R.id.btnRecord, !isRecording);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, !isRecording);
        //enableButton(R.id.btnPause, isRecording);
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
    }

    public String getName(){return outputName;}

    private void pauseRecording(){
        paused = true;
        stopService(new Intent(this, WavRecorder.class));
        try {
            RecordingQueues.writingQueue.put(new RecordingMessage(null, true, false));
            RecordingQueues.UIQueue.put(new RecordingMessage(null, true, false));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void startRecording(){
        if(!paused) {
    	    isSaved = false;
    	    RecordingQueues.writingQueue.clear();
            Intent intent = new Intent(this, WavFileWriter.class);
            intent.putExtra("audioFileName", getFilename());
            startService(new Intent(this, WavRecorder.class));
            System.out.println("Started the service");
            startService(intent);
            mainCanvas.listenForRecording(this);
        }
        else {
            paused = false;
            startService(new Intent(this, WavRecorder.class));
        }

    }
    private void stopRecording() {
        if (isPlaying) {
            isPlaying = false;
            WavPlayer.stop();
        }
        else {
            findViewById(R.id.linearLayout10).setVisibility(View.VISIBLE);
            findViewById(R.id.linearLayout9).setVisibility(View.INVISIBLE);
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
                    mainCanvas.loadWavFromFile(recordedFilename);
                    final int base = -mainCanvas.getWidth()/8;
                    mainCanvas.setXTranslation(base);
                    mainCanvas.displayWaveform(10);
                    mainCanvas.shouldDrawMaker(true);
                    minimap.loadWavFromFile(recordedFilename);
                    minimap.displayWaveform(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void playRecording(){
        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
        WavPlayer.play(recordedFilename);
        isPlaying = true;
        final int base = -mainCanvas.getWidth()/8;
        mainCanvas.setXTranslation(base);
        mainCanvas.invalidate();
        Thread playback = new Thread(new Runnable() {
            @Override
            public void run() {
                int translation = 0;
                while(WavPlayer.isPlaying()){
                    double location = (double)WavPlayer.getLocation()/ (double)WavPlayer.getDuration();
                    double scaleFactor = (WavPlayer.getDuration() / 10000.0) * mainCanvas.getWidth();
                    System.out.println("Scalefactor is " + scaleFactor);
                    translation = (int)(location * scaleFactor);
                    mainCanvas.setXTranslation(base+translation);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainCanvas.invalidate();
                        }
                    });

                }
            }
        });
        playback.start();
    }

    @Override
    public void onBackPressed() {
        if(!isSaved) {
            QuitDialog dialog = new QuitDialog();
            dialog.show(this.getFragmentManager(), "reallyQuitDialog");
        }
        else
            super.onBackPressed();
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println("Pressed something");
            switch(v.getId()){
                case R.id.btnRecord:{
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
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(filepath, AUDIO_RECORDER_FOLDER);
        System.out.println(recordedFilename);
        File from = new File(recordedFilename);
        File to = new File(dir, name + AUDIO_RECORDER_FILE_EXT_WAV);
        Boolean out = from.renameTo(to);
        recordedFilename = to.getAbsolutePath();
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
