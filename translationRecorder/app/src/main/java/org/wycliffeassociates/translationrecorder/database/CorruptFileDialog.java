package org.wycliffeassociates.translationrecorder.database;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.R;
import com.door43.tools.reporting.Logger;

import java.io.File;

public class CorruptFileDialog extends DialogFragment implements View.OnClickListener {

    protected File mFile = null;

    public static CorruptFileDialog newInstance(File file){
        CorruptFileDialog dialog = new CorruptFileDialog();
        dialog.setFile(file);
        return dialog;
    }

    public void setFile(File file){
        mFile = file;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_corrupt_file, null);
        final Button btnDelete = (Button) view.findViewById(R.id.ok_button);
        btnDelete.setOnClickListener(this);

        final Button btnIgnore = (Button) view.findViewById(R.id.ignore_button);
        btnIgnore.setOnClickListener(this);

        final TextView mFileName = (TextView) view.findViewById(R.id.filename_view);
        mFileName.setText(mFile.getName());
        builder.setView(view);
        return builder.create();
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
