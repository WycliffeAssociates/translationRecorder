package org.wycliffeassociates.translationrecorder.SettingsPage;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

/**
 * Created by sarabiaj on 12/14/2016.
 */

public class AddTargetLanguageDialog extends DialogFragment {

    public static final int LANGUAGE_CODE_SIZE = 6;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_temp_language, null);
        final ProjectDatabaseHelper db = ((TranslationRecorderApp)getActivity().getApplication()).getDatabase();

        final EditText languageCode = (EditText) view.findViewById(R.id.language_code);
        final EditText languageName = (EditText) view.findViewById(R.id.language_name);
        final TextView errorCodeExists = (TextView) view.findViewById(R.id.error_code_exists);
        final TextView errorCodeTooShort = (TextView) view.findViewById(R.id.error_code_too_short);

        final Button addButton = (Button) view.findViewById(R.id.ok_button);
        final Button cancelButton = (Button) view.findViewById(R.id.close_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = languageCode.getText().toString();
                String name = languageName.getText().toString();
                boolean error = false;
                if(code.length() < LANGUAGE_CODE_SIZE) {
                    errorCodeTooShort.setVisibility(View.VISIBLE);
                    error = true;
                } else {
                    errorCodeTooShort.setVisibility(View.GONE);
                }
                code = "qaa-x-tR" + code;
                if(db.languageExists(code)) {
                    errorCodeExists.setVisibility(View.VISIBLE);
                    error = true;
                } else {
                    errorCodeExists.setVisibility(View.GONE);
                }
                if (!error) {
                    db.addLanguage(code, name);
                    dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
