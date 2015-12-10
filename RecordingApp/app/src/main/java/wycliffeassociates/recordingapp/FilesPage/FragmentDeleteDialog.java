package wycliffeassociates.recordingapp.FilesPage;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class FragmentDeleteDialog extends DialogFragment implements View.OnClickListener {

    Button delete, cancel;
    AudioFiles af;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_confirm, null);

        af = (AudioFiles) getActivity();

        delete = (Button) view.findViewById(R.id.delete_confirm);
        cancel = (Button) view.findViewById(R.id.delete_cancel);

        delete.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.delete_confirm) {
            Toast.makeText(getActivity(), "Deleting files ...", Toast.LENGTH_SHORT).show();
            af.confirmDelete();
            this.dismiss();
        }
        else {
            Toast.makeText(getActivity(), "No file has been deleted", Toast.LENGTH_SHORT).show();
            this.dismiss();
        }
    }



}