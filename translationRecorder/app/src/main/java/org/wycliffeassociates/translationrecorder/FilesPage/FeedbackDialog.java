package org.wycliffeassociates.translationrecorder.FilesPage;


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

public class FeedbackDialog extends DialogFragment implements View.OnClickListener {

    protected String mTitle = "";
    protected String mMessage = "";

    public static FeedbackDialog newInstance(String title, String message){
        FeedbackDialog dialog = new FeedbackDialog();
        dialog.setTitle(title);
        dialog.setMessage(message);
        return dialog;
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public void setMessage(String message){
        mMessage = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);

        final Button btnOk = (Button) view.findViewById(R.id.ok_button);
        btnOk.setOnClickListener(this);

        final TextView mTitleText = (TextView) view.findViewById(R.id.title);
        mTitleText.setText(mTitle);

        final TextView mMessageText = (TextView) view.findViewById(R.id.message);
        mMessageText.setText(mMessage);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_button: {
                dismiss();
                break;
            }
            default:{
                Logger.e(this.toString(), "Feedback dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }
}
