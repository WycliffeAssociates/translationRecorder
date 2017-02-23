package org.wycliffeassociates.translationrecorder.Recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by leongv on 12/10/2015.
 */
public class FragmentExitDialog extends DialogFragment implements View.OnClickListener{

    ImageButton delete, save;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exit_record, null);

        activity = getActivity();

        delete = (ImageButton) view.findViewById(R.id.btnDelete);
        save = (ImageButton) view.findViewById(R.id.btn_save);

        delete.setOnClickListener(this);
        save.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                dismiss();
                break;
            case R.id.btnDelete: {
                //rs.deleteTempFile();
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
