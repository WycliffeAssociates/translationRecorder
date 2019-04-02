package org.wycliffeassociates.translationrecorder.FilesPage;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.R;

public class FeedbackDialog extends DialogFragment implements View.OnClickListener {

    public static final String DIALOG_TITLE = "dialogTitle";
    public static final String DIALOG_MESSAGE = "dialogMessage";

    public static FeedbackDialog newInstance(String title, String message){
        FeedbackDialog dialog = new FeedbackDialog();

        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String mTitle = getArguments().getString(DIALOG_TITLE);
        String mMessage = getArguments().getString(DIALOG_MESSAGE);

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
