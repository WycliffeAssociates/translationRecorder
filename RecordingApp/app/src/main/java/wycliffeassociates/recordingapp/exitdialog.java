package wycliffeassociates.recordingapp;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.Recording.*;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

/**
 * Created by Emmanuel on 8/5/2015.
 */
public class ExitDialog extends Dialog implements View.OnClickListener {

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isPausedRecording = false;
    private String filename = null;


    private Activity activity;

    private ImageButton btnSave, btnDelete;

    public ExitDialog(Activity a, int theme) {
        super(a, theme);
        this.activity = a;

    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exitdialog);

        btnSave = (ImageButton) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);

    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }
    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
    public void setIsPausedRecording(boolean isPausedRecording) {
        this.isPausedRecording = isPausedRecording;
    }
    public void setFilename(String filename){
        this.filename = filename;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                dismiss();
                break;
            case R.id.btnDelete: {
                if (isRecording) {
                    System.out.println("trying to stop the recording service");
                    boolean serviceStopped = activity.stopService(new Intent(activity, WavRecorder.class));
                    if(serviceStopped == true){
                        System.out.println("Successfully stopped the service.");
                    }
                    else {
                        System.out.println("Could not stop the service.");
                    }

                    RecordingQueues.stopQueues();

                }
                else if (isPlaying) {
                    WavPlayer.stop();
                    WavPlayer.release();
                }
                else {
                    WavPlayer.stop();
                    WavPlayer.release();
                    if(isPausedRecording){
                        RecordingQueues.stopQueues();
                    }
                }
                if (filename != null){
                    File file = new File(filename);
                    file.delete();
                }
                activity.finish();
                break;
            }
            default:{
                System.out.println("Exit dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }
}