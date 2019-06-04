package org.wycliffeassociates.translationrecorder.ProjectManager.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.R;

import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 12/14/2016.
 */

public class RequestLanguageNameDialog extends DialogFragment{

    private String mCode;
    private BlockingQueue<String> mResponse;

    public static RequestLanguageNameDialog newInstance(String languageCode, BlockingQueue<String> response) {
        RequestLanguageNameDialog dialog = new RequestLanguageNameDialog();
        dialog.setLanguageCode(languageCode);
        dialog.setResponseQueue(response);
        return dialog;
    }

    private void setLanguageCode(String code){
        mCode = code;
    }

    private void setResponseQueue(BlockingQueue<String> responseQueue) {
        mResponse = responseQueue;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_language_not_found, null);

        final TextView languageCode = (TextView) view.findViewById(R.id.language_code);
        final EditText languageName = (EditText) view.findViewById(R.id.language_name);
        final Button addButton = (Button) view.findViewById(R.id.ok_button);

        languageCode.setText(mCode);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mResponse.put(languageName.getText().toString());
                    dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
