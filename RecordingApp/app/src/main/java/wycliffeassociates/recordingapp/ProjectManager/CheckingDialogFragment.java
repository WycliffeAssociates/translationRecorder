package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by leongv on 8/9/2016.
 */
public class CheckingDialogFragment extends DialogFragment {

    public static String TAKE_KEY = "key_take_name";
    public interface DialogListener {
        void onPositiveClick(CheckingDialogFragment dialog);
        void onNegativeClick(CheckingDialogFragment dialog);
    }

    private int mCheckingLevel;
    DialogListener mListener;
    private String mTakeName;

    public static CheckingDialogFragment newInstance(String takeName){
        CheckingDialogFragment check = new CheckingDialogFragment();
        Bundle args = new Bundle();
        args.putString(TAKE_KEY, takeName);
        check.setArguments(args);
        return check;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTakeName = args.getString(TAKE_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Set the checking level")
                .setView(inflater.inflate(R.layout.dialog_checking, null))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick(CheckingDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNegativeClick(CheckingDialogFragment.this);
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog= (AlertDialog) dialog;

                // Button positiveBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                // positiveBtn.setBackground(getResources().getDrawable(R.drawable.delete));

                final FourStepImageView levelOne = (FourStepImageView) alertDialog.findViewById(R.id.check_level_one);
                final FourStepImageView levelTwo = (FourStepImageView) alertDialog.findViewById(R.id.check_level_two);
                final FourStepImageView levelThree = (FourStepImageView) alertDialog.findViewById(R.id.check_level_three);

                levelOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        levelOne.setActivated(true);
                        levelTwo.setActivated(false);
                        levelThree.setActivated(false);
                        mCheckingLevel = 1;
                    }
                });

                levelTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        levelOne.setActivated(false);
                        levelTwo.setActivated(true);
                        levelThree.setActivated(false);
                        mCheckingLevel = 2;
                    }
                });

                levelThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        levelOne.setActivated(false);
                        levelTwo.setActivated(false);
                        levelThree.setActivated(true);
                        mCheckingLevel = 3;
                    }
                });
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CheckingDialogListener");
        }
    }

    public int getCheckingLevel() {
        return mCheckingLevel;
    }

    public String getTakeName(){
        return mTakeName;
    }
}