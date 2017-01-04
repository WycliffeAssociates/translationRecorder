package org.wycliffeassociates.translationrecorder.Reporting;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.wycliffeassociates.translationrecorder.MainMenu;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class BugReportDialog extends DialogFragment implements View.OnClickListener {

    Button delete, cancel;
    MainMenu mm;
    EditText message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bug_report, null);

        mm = (MainMenu) getActivity();

        delete = (Button) view.findViewById(R.id.delete_confirm);
        cancel = (Button) view.findViewById(R.id.delete_cancel);
        message = (EditText) view.findViewById(R.id.crashReportTextField);


        delete.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.delete_confirm) {
            mm.report(message.getText().toString());
            this.dismiss();
        }
        else {
            mm.archiveStackTraces();
            this.dismiss();
        }
    }



}