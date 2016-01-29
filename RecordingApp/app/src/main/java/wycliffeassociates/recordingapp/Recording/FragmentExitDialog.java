package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by leongv on 12/10/2015.
 */
public class FragmentExitDialog extends DialogFragment implements View.OnClickListener{

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isPausedRecording = false;
    private String filename = null;
    private boolean isALoadedFile = false;
    ImageButton delete, save;
    RecordingScreen rs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exit_record, null);

        rs = (RecordingScreen) getActivity();

        delete = (ImageButton) view.findViewById(R.id.btnDelete);
        save = (ImageButton) view.findViewById(R.id.btnSave);

        delete.setOnClickListener(this);
        save.setOnClickListener(this);

        return view;
    }

    // TODO: this function may need some refactoring
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                dismiss();
                break;
            case R.id.btnDelete: {
                if (isRecording) {
//                    System.out.println("trying to stop the recording service");
//                    boolean serviceStopped = rs.stopService(new Intent(rs, WavRecorder.class));
//                    if(serviceStopped){
//                        System.out.println("Successfully stopped the service.");
//                    }
//                    else {
//                        System.out.println("Could not stop the service.");
//                    }
//                    RecordingQueues.stopQueues();
                }
                else if (isPlaying) {
                    WavPlayer.release();
                }
                else {
                    WavPlayer.release();
//                    if(isPausedRecording){
//                        RecordingQueues.stopQueues();
//                    }
                }
                if (filename != null && !isALoadedFile){
                    rs.deleteTempFile();
                }
                rs.finish();
                break;
            }
            default:{
                System.out.println("Exit dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }

    public void setLoadedFile(boolean loadedFile){ this.isALoadedFile = loadedFile;}

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
}
