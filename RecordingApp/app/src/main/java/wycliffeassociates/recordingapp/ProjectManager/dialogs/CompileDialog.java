package wycliffeassociates.recordingapp.ProjectManager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import wycliffeassociates.recordingapp.ProjectManager.Project;

/**
 * Created by leongv on 8/17/2016.
 */
public class CompileDialog extends DialogFragment {

    public static String CHAPTERS_KEY = "key_chapters";
    public static String COMPILED_KEY = "key_compiled";
    public static String PROJECT_KEY = "key_project";

    public interface DialogListener {
        void onPositiveClick(CompileDialog dialog);
        void onNegativeClick(CompileDialog dialog);
    }

    DialogListener mListener;
    private int[] mChapterIndices;
    private Project mProject;
    private boolean mAlreadyCompiled;

    public static CompileDialog newInstance(Project project, int[] chapterIndices, boolean[] isCompiled){
        Bundle args = new Bundle();
        args.putIntArray(CHAPTERS_KEY, chapterIndices);
        args.putParcelable(PROJECT_KEY, project);
        args.putBooleanArray(COMPILED_KEY, isCompiled);
        CompileDialog check = new CompileDialog();
        check.setArguments(args);
        return check;
    }

    public static CompileDialog newInstance(Project project, int chapterIndex, boolean isCompiled){
        int[] chapterIndices = {chapterIndex};
        boolean[] compiled = {isCompiled};
        return newInstance(project, chapterIndices, compiled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mChapterIndices = args.getIntArray(CHAPTERS_KEY);
        mProject = args.getParcelable(PROJECT_KEY);
        boolean[] compiled = args.getBooleanArray(COMPILED_KEY);
        mAlreadyCompiled = false;
        for(int i = 0; i < compiled.length; i++){
            if(compiled[i] == true){
                mAlreadyCompiled = true;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // NOTE: This is commented out because we don't want the compile to execute without the user
        //    clicking "OK" on the dialog. If Joe agrees, he can delete this.
        // if(!mAlreadyCompiled){
        //     mListener.onPositiveClick(CompileDialog.this);
        // }
        String message;
        if (mAlreadyCompiled) {
            message = "Re-compiling a chapter will over-write the current audio and reset the checking level.";
        } else {
            message = "Compile all units into one chapter audio?";
        }
        return new AlertDialog.Builder(getActivity())
            .setTitle("Warning")
            .setMessage(message)
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

    public int[] getChapterIndicies(){
        return mChapterIndices;
    }

    public Project getProject(){
        return mProject;
    }
}