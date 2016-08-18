package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by leongv on 8/17/2016.
 */
public class CompileDialog extends DialogFragment {

    public static String CHAPTER_NAMES_KEY = "key_chapter_names";

    public interface DialogListener {
        void onPositiveClick(CompileDialog dialog);
        void onNegativeClick(CompileDialog dialog);
    }

    DialogListener mListener;
    private String[] mChapterNames;

    public static CompileDialog newInstance(String[] chapterNames){
        Bundle args = new Bundle();
        args.putStringArray(CHAPTER_NAMES_KEY, chapterNames);
        CompileDialog check = new CompileDialog();
        check.setArguments(args);
        return check;
    }

    public static CompileDialog newInstance(String chapterName){
        String[] chapterNames = {chapterName};
        return newInstance(chapterNames);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mChapterNames = args.getStringArray(CHAPTER_NAMES_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle("Warning")
            .setMessage("Re-compiling a chapter will over-write the current audio and reset the checking level.")
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPositiveClick(CompileDialog.this);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onNegativeClick(CompileDialog.this);
                }
            })
            .create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CompileDialogListener");
        }
    }

    public String[] getChapterNames(){
        return mChapterNames;
    }

    public String getChapterName(int position) {
        return mChapterNames[position];
    }
}