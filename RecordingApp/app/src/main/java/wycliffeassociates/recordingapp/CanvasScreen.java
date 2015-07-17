package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CanvasScreen extends Activity {

    private String recordedFilename = null;
    private WavRecorder recorder = null;
    final Context context = this;
    private String outputName = null;
    private CanvasView customCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waveform);

        customCanvas = (CanvasView) findViewById(R.id.signature_canvas);
        setButtonHandlers();
        enableButtons(false);
    }

    public void clearCanvas(View v) {
        customCanvas.clearCanvas();
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
        if(recorder != null){
            recorder.release();
        }
        recorder = null;
        Toast.makeText(getApplicationContext(), "Starting Recording", Toast.LENGTH_LONG).show();
        recorder = new WavRecorder();
        recorder.record();
    }
    private void stopRecording(){
        recorder.stop();
        Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_LONG).show();
        recordedFilename = recorder.getFilename();


        WavFileLoader temp = new WavFileLoader(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/test.wav");
        WavVisualizer vis = new WavVisualizer(temp.getAudioData(), temp.getNumChannels(), temp.getLargestValue());




        double xsf = 1.75*vis.getXScaleFactor(customCanvas.getWidth());
        double ysf = vis.getYScaleFactor(customCanvas.getHeight());
        customCanvas.setXScale(xsf);
        customCanvas.setYScale(ysf);
        int inc = vis.getIncrement(xsf);
        vis.sampleAudio(inc, ysf);
        customCanvas.setSamples(vis.getSamples());
        System.out.println("get width is returning " + customCanvas.getWidth());
        System.out.println("get Height is returning " + customCanvas.getHeight());
        System.out.println("X scale is " + xsf);
        System.out.println("X scale SHOULD BE" + vis.getXScaleFactor(customCanvas.getWidth()));
        System.out.println("Incremment is being set to  " + inc);


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
