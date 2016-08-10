package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by leongv on 8/9/2016.
 */
public class RatingDialogFragment extends DialogFragment {

    public interface DialogListener {
        public void onPositiveClick(RatingDialogFragment dialog);
        public void onNegativeClick(RatingDialogFragment dialog);
    }

    private int mRating;
    DialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: Code to check current rating here. Maybe?
        // mRating = ??
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
            .setTitle("Rate this take")
            .setView(inflater.inflate(R.layout.dialog_rating, null))
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPositiveClick(RatingDialogFragment.this);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onNegativeClick(RatingDialogFragment.this);
                }
            })
            .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog= (AlertDialog) dialog;

                // Button positiveBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                // positiveBtn.setBackground(getResources().getDrawable(R.drawable.delete));

                final FourStepImageView oneStar = (FourStepImageView) alertDialog.findViewById(R.id.one_star_rating);
                final FourStepImageView twoStar = (FourStepImageView) alertDialog.findViewById(R.id.two_star_rating);
                final FourStepImageView threeStar = (FourStepImageView) alertDialog.findViewById(R.id.three_star_rating);

                oneStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("One star");
                        oneStar.setStep(1);
                        twoStar.setStep(0);
                        threeStar.setStep(0);
                        mRating = 1;
                    }
                });

                twoStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Two star");
                        oneStar.setStep(2);
                        twoStar.setStep(2);
                        threeStar.setStep(0);
                        mRating = 2;
                    }
                });

                threeStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Three star");
                        oneStar.setStep(3);
                        twoStar.setStep(3);
                        threeStar.setStep(3);
                        mRating = 3;
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
            throw new ClassCastException(activity.toString() + " must implement RatingDialogListener");
        }
    }

    public int getRating() {
        return mRating;
    }
}