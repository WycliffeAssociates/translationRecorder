package wycliffeassociates.recordingapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

/**
 * Created by Abi on 7/21/2015.
 */
public class QuitDialog extends DialogFragment{

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
                        getActivity().finish();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        nButton.setBackground(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        pButton.setBackground(getResources().getDrawable(R.drawable.abc_ic_clear_mtrl_alpha));

        int width = getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material);
        int height = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
        getDialog().getWindow().setLayout(width*6, height*5);
    }
}
