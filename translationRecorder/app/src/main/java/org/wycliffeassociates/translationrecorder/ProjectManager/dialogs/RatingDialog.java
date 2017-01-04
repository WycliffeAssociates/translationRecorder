package org.wycliffeassociates.translationrecorder.ProjectManager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;

/**
 * Created by leongv on 8/9/2016.
 */
public class RatingDialog extends DialogFragment {

    public static String TAKE_KEY = "key_take_name";
    public static String CURRENT_RATING_KEY = "key_current_rating";

    public interface DialogListener {
        void onPositiveClick(RatingDialog dialog);
        void onNegativeClick(RatingDialog dialog);
    }

    private int mRating;
    private DialogListener mListener;
    private String mTakeName;

    private FourStepImageView mOneStar, mTwoStar, mThreeStar;

    public static RatingDialog newInstance(String takeName, int currentRating){
        RatingDialog rate = new RatingDialog();
        Bundle args = new Bundle();
        args.putString(TAKE_KEY, takeName);
        args.putInt(CURRENT_RATING_KEY, currentRating);
        rate.setArguments(args);
        return rate;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTakeName = args.getString(TAKE_KEY);
        mRating = args.getInt(CURRENT_RATING_KEY);
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
                mOneStar = (FourStepImageView) alertDialog.findViewById(R.id.one_star_rating);
                mTwoStar = (FourStepImageView) alertDialog.findViewById(R.id.two_star_rating);
                mThreeStar = (FourStepImageView) alertDialog.findViewById(R.id.three_star_rating);

                mOneStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setRating(1);
                    }
                });

                mTwoStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setRating(2);
                    }
                });

                mThreeStar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setRating(3);
                    }
                });

                setRating(mRating);
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

    private void setRating(int rating) {
        mRating = rating;
        switch (mRating) {
            case 1:
                mOneStar.setStep(1);
                mTwoStar.setStep(0);
                mThreeStar.setStep(0);
                break;
            case 2:
                mOneStar.setStep(2);
                mTwoStar.setStep(2);
                mThreeStar.setStep(0);
                break;
            case 3:
                mOneStar.setStep(3);
                mTwoStar.setStep(3);
                mThreeStar.setStep(3);
                break;
            default:
                mOneStar.setStep(0);
                mTwoStar.setStep(0);
                mThreeStar.setStep(0);
        }
    }

    public int getRating() {
        return mRating;
    }

    public String getTakeName(){
        return mTakeName;
    }
}