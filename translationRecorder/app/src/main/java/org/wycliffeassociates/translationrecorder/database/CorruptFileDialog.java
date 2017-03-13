package org.wycliffeassociates.translationrecorder.database;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;

import java.io.File;

public class CorruptFileDialog extends Dialog implements View.OnClickListener {

    protected Context mCtx;
    protected File mFile = null;
    private TextView mFileName;
    private Button btnDelete;
    private Button btnIgnore;

    public static CorruptFileDialog Build(Context a, int theme, File file){
        CorruptFileDialog exit = new CorruptFileDialog(a, theme);
        exit.setFile(file);
        return exit;
    }

    public void setFile(File file){
        mFile = file;
    }

    public CorruptFileDialog(Context a, int theme) {
        super(a, theme);
        this.mCtx = a;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_corrupt_file);

        btnDelete = (Button) findViewById(R.id.ok_button);
        btnDelete.setOnClickListener(this);

        btnIgnore = (Button) findViewById(R.id.ignore_button);
        btnIgnore.setOnClickListener(this);

        mFileName = (TextView) findViewById(R.id.filename_view);
        mFileName.setText(mFile.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ignore_button:
                dismiss();
                break;
            case R.id.ok_button: {
                if (mFile != null){
                    mFile.delete();
                }
                dismiss();
                break;
            }
            default:{
                Logger.e(this.toString(), "Corrupt file dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }
}
