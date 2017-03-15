package org.wycliffeassociates.translationrecorder.FilesPage;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import org.wycliffeassociates.translationrecorder.R;

import java.io.File;

/**
 * Created by Emmanuel on 8/5/2015.
 */
public class ExitDialog extends Dialog implements View.OnClickListener {

    public interface DeleteFileCallback {
        void onDeleteRecording();
    }

    protected boolean mIsPlaying = false;
    protected File mFile = null;
    protected boolean mKeepFile = false;
    protected ImageButton btnSave, btnDelete;
    protected DeleteFileCallback activity;

    public static ExitDialog Build(DeleteFileCallback a, int theme, boolean keepFile, boolean isPlaying, File file){
        ExitDialog exit = new ExitDialog(a, theme);
        exit.setFile(file);
        exit.setIsPlaying(isPlaying);
        exit.setKeepFile(keepFile);
        exit.setDeleteCallback(a);
        return exit;
    }

    private void setDeleteCallback(DeleteFileCallback dfc) {
        this.activity = dfc;
    }

    public void setKeepFile(boolean loadedFile){ mKeepFile = loadedFile;}

    public void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    public void setFile(File file){
        mFile = file;
    }

    public ExitDialog(DeleteFileCallback a, int theme) {
        super((Activity)a, theme);
        this.activity = a;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_exit_record);

        btnSave = (ImageButton) findViewById(R.id.btn_save);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);

        btnSave.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                dismiss();
                break;
            case R.id.btnDelete: {
                if (mFile != null && !mKeepFile){
                    activity.onDeleteRecording();
                }
                break;
            }
            default:{
                System.out.println("Exit dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }
}
