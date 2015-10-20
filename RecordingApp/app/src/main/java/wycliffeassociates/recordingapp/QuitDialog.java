package wycliffeassociates.recordingapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;

import wycliffeassociates.recordingapp.Recording.RecordingMessage;
import wycliffeassociates.recordingapp.Recording.RecordingQueues;
import wycliffeassociates.recordingapp.Recording.WavRecorder;

/**
 * Created by Abi on 7/21/2015.
 */
public class QuitDialog extends DialogFragment{
    AlertDialog dialog;
    private boolean isRecording = false;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.exit);
        builder
                .setPositiveButton(" ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .setNegativeButton(" ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(isRecording){
                            System.out.println("trying to stop the recording service");
                            boolean serviceStopped = getActivity().stopService(new Intent(getActivity(), WavRecorder.class));
                            if(serviceStopped == true){
                                System.out.println("Successfully stopped the service.");
                            }
                            else {
                                System.out.println("Could not stop the service.");
                            }

                            try {
                                RecordingQueues.UIQueue.put(new RecordingMessage(null, false, true));
                                RecordingQueues.writingQueue.put(new RecordingMessage(null, false, true));
                                Boolean done = RecordingQueues.doneWriting.take();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        getActivity().finish();
                    }
                });
        dialog = builder.create();
        // Create the AlertDialog object and return it
        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);


        nButton.setBackground(getResources().getDrawableForDensity(R.drawable.ic_ic_delete_black_48dp, 48));
        pButton.setBackground(getResources().getDrawableForDensity(R.drawable.ic_ic_save_black_48dp, 48));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);
    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }
}
