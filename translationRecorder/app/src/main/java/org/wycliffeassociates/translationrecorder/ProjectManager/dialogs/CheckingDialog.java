package org.wycliffeassociates.translationrecorder.ProjectManager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.wycliffeassociates.translationrecorder.ProjectManager.Project;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;

/**
 * Created by leongv on 8/9/2016.
 */
public class CheckingDialog extends DialogFragment {

    public static String CHAPTERS_KEY = "key_chapters";
    public static String PROJECT_KEY = "key_project";
    public static String CURRENT_LEVEL_KEY = "key_current_checking_level";
    public static int NO_LEVEL_SELECTED = -1;

    public interface DialogListener {
        void onPositiveClick(CheckingDialog dialog);
        void onNegativeClick(CheckingDialog dialog);
    }

    private int mCheckingLevel;
    DialogListener mListener;
    private int[] mChapterIndicies;
    private Project mProject;
    private ImageButton mLevelZero;
    private FourStepImageView mLevelOne, mLevelTwo, mLevelThree;
    private Button mPositiveBtn;

    public static CheckingDialog newInstance(Project project, int[] chapterIndicies, int checkingLevel){
        Bundle args = new Bundle();
        args.putIntArray(CHAPTERS_KEY, chapterIndicies);
        args.putParcelable(PROJECT_KEY, project);
        args.putInt(CURRENT_LEVEL_KEY, checkingLevel);
        CheckingDialog check = new CheckingDialog();
        check.setArguments(args);
        return check;
    }

    public static CheckingDialog newInstance(Project project, int chapterIndex, int checkingLevel){
        int[] chapterIndicies = {chapterIndex};
        return newInstance(project, chapterIndicies, checkingLevel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mChapterIndicies = args.getIntArray(CHAPTERS_KEY);
        mProject = args.getParcelable(PROJECT_KEY);
        mCheckingLevel = args.getInt(CURRENT_LEVEL_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Set the checking level")
                .setView(inflater.inflate(R.layout.dialog_checking, null))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick(CheckingDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNegativeClick(CheckingDialog.this);
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog= (AlertDialog) dialog;

                mLevelZero = (ImageButton) alertDialog.findViewById(R.id.check_level_zero);
                mLevelOne = (FourStepImageView) alertDialog.findViewById(R.id.check_level_one);
                mLevelTwo = (FourStepImageView) alertDialog.findViewById(R.id.check_level_two);
                mLevelThree = (FourStepImageView) alertDialog.findViewById(R.id.check_level_three);
                mPositiveBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                mLevelZero.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCheckingLevel(0);
                    }
                });

                mLevelOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCheckingLevel(1);
                    }
                });

                mLevelTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCheckingLevel(2);
                    }
                });

                mLevelThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCheckingLevel(3);
                    }
                });

                setCheckingLevel(mCheckingLevel);

                if (mCheckingLevel == NO_LEVEL_SELECTED) {
                    mPositiveBtn.setEnabled(false);
                }
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

    private void setCheckingLevel(int checkingLevel) {
        mCheckingLevel = checkingLevel;
        mPositiveBtn.setEnabled(true);
        mLevelZero.setActivated(checkingLevel == 0);
        mLevelOne.setActivated(checkingLevel == 1);
        mLevelTwo.setActivated(checkingLevel == 2);
        mLevelThree.setActivated(checkingLevel == 3);
    }

    public int getCheckingLevel() {
        return mCheckingLevel;
    }

    public int[] getChapterIndicies(){
        return mChapterIndicies;
    }

    public Project getProject(){
        return mProject;
    }

}