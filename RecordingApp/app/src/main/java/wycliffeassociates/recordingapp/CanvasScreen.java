package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CanvasScreen extends Activity {

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
    private WavFileLoader waveLoader;
    private WavVisualizer waveVis;


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
        setContentView(R.layout.waveform);
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


        mainCanvas = (CanvasView) findViewById(R.id.signature_canvas);
        minimap = (CanvasView) findViewById(R.id.minimap);
        setButtonHandlers();
        enableButtons(false);
    }



    private void setButtonHandlers() {
        findViewById(R.id.btnRecord).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnRecord, !isRecording);
        enableButton(R.id.btnStop, isRecording);
        enableButton(R.id.btnPlay, !isRecording);
        enableButton(R.id.btnSave, true);
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
        recordedFilename= recorder.saveFile(outputName);
    }

    public String getName(){return outputName;}

    private void startRecording(){
        waveLoader = null;
        waveVis = null;
        if(recorder != null){
            recorder.release();
        }
        recorder = null;
        Toast.makeText(getApplicationContext(), "Starting Recording", Toast.LENGTH_LONG).show();
        recorder = new WavRecorder();
        recorder.record();

    }
    private void stopRecording(){
        waveLoader = null;
        waveVis = null;
        recorder.stop();
        Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_LONG).show();
        recordedFilename = recorder.getFilename();

        waveLoader = new WavFileLoader(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/test.wav");
        //waveLoader = new WavFileLoader(recordedFilename);
        waveVis = new WavVisualizer(waveLoader.getAudioData(), waveLoader.getNumChannels());




        double xsf = waveVis.getXScaleFactor(mainCanvas.getWidth(), 10);
        mainCanvas.setXScale(xsf);
        int inc = waveVis.getIncrement(xsf);
        waveVis.sampleAudio(inc);
        double ysf = waveVis.getYScaleFactor(mainCanvas.getHeight());
        mainCanvas.setYScale(ysf);
        mainCanvas.setSamples(waveVis.getSamples());
        System.out.println("get width is returning " + mainCanvas.getWidth());
        System.out.println("get Height is returning " + mainCanvas.getHeight());
        System.out.println("X scale is " + xsf);
        System.out.println("Y scale is " + ysf);
        System.out.println("Y scale SHOULD be " + (mainCanvas.getHeight() / 65536.0));
        System.out.println("X scale SHOULD BE" + waveVis.getXScaleFactor(mainCanvas.getWidth(), 10));
        System.out.println("Increment is being set to  " + inc);
        mainCanvas.invalidate();


        WavVisualizer miniVis = new WavVisualizer(waveLoader.getAudioData(), waveLoader.getNumChannels());

        double xsf2 = miniVis.getXScaleFactor(minimap.getWidth(), 0);
        minimap.setXScale(xsf2);
        int inc2 = miniVis.getIncrement(xsf2);
        miniVis.sampleAudio(inc2);
        double ysf2 = miniVis.getYScaleFactor(minimap.getHeight());
        minimap.setYScale(ysf2);
        minimap.setSamples(miniVis.getSamples());
        minimap.invalidate();


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
                    break;
                }
                case R.id.btnSave:{
                    saveRecording();
                    break;
                }
            }
        }
    };
}
