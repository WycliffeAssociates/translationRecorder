package wycliffeassociates.recordingapp.FilesPage;

import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/4/2015.
 */
public class FragmentActions extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_actions, container, false);
        return view;
    }
}
