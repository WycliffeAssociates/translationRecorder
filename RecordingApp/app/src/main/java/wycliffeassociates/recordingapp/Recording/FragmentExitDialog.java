package wycliffeassociates.recordingapp.Recording;

import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/10/2015.
 */
public class FragmentExitDialog extends DialogFragment implements View.OnClickListener{

    ImageButton delete, save;
    RecordingScreen rs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exit_record, null);

        rs = (RecordingScreen) getActivity();

        delete = (ImageButton) view.findViewById(R.id.btnDelete);
        save = (ImageButton) view.findViewById(R.id.btnSave);

        delete.setOnClickListener(this);
        save.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                dismiss();
                break;
            case R.id.btnDelete: {
                rs.deleteTempFile();
                rs.finish();
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
