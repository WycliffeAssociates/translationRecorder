package wycliffeassociates.recordingapp.FilesPage;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

import wycliffeassociates.recordingapp.R;

/**
 * Created by Emmanuel on 8/5/2015.
 */
public class ExitDialog extends Dialog implements View.OnClickListener {

    protected boolean mIsPlaying = false;
    protected File mFile = null;
    protected boolean mKeepFile = false;

    public static ExitDialog Build(Activity a, int theme, boolean keepFile, boolean isPlaying, File file){
        ExitDialog exit = new ExitDialog(a, theme);
        exit.setFile(file);
        exit.setIsPlaying(isPlaying);
        exit.setKeepFile(keepFile);
        return exit;
    }

    public void setKeepFile(boolean loadedFile){ mKeepFile = loadedFile;}

    public void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    public void setFile(File file){
        mFile = file;
    }

    protected Activity activity;

    protected ImageButton btnSave, btnDelete;

    public ExitDialog(Activity a, int theme) {
        super(a, theme);
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
                    mFile.delete();
                }
                activity.finish();
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
