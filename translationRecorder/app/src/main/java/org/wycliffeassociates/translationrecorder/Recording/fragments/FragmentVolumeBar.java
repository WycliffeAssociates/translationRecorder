package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wycliffeassociates.translationrecorder.AudioVisualization.VolumeBar;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentVolumeBar extends Fragment {

    VolumeBar mVolume;

    public static FragmentVolumeBar newInstance() {
        FragmentVolumeBar f = new FragmentVolumeBar();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_volume_bar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
    }

    private void findViews() {
        View view = getView();
        mVolume = (VolumeBar) view.findViewById(R.id.volumeBar1);
    }

    public void updateDb(int db){
        mVolume.setDb(db);
        mVolume.postInvalidate();
    }
}
