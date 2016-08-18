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
public class RatingDialog extends DialogFragment {

    public static String TAKE_KEY = "key_take_name";
    public interface DialogListener {
        void onPositiveClick(RatingDialog dialog);
        void onNegativeClick(RatingDialog dialog);

    }
    private int mRating;
    private DialogListener mListener;
    private String mTakeName;

    public static RatingDialog newInstance(String takeName){
        RatingDialog rate = new RatingDialog();
        Bundle args = new Bundle();
        args.putString(TAKE_KEY, takeName);
        rate.setArguments(args);
        return rate;
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
            .setTitle("Rate this take")
            .setView(inflater.inflate(R.layout.dialog_rating, null))
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPositiveClick(RatingDialog.this);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onNegativeClick(RatingDialog.this);
                }
            })
            .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog= (AlertDialog) dialog;
                final FourStepImageView oneStar = (FourStepImageView) alertDialog.findViewById(R.id.one_star_rating);
                final FourStepImageView twoStar = (FourStepImageView) alertDialog.findViewById(R.id.two_star_rating);
                final FourStepImageView threeStar = (FourStepImageView) alertDialog.findViewById(R.id.three_star_rating);

                oneStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        oneStar.setStep(1);
                        twoStar.setStep(0);
                        threeStar.setStep(0);
                        mRating = 1;
                    }
                });

                twoStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        oneStar.setStep(2);
                        twoStar.setStep(2);
                        threeStar.setStep(0);
                        mRating = 2;
                    }
                });

                threeStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

    public String getTakeName(){
        return mTakeName;
    }
}