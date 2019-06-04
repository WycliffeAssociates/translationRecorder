package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wycliffeassociates.translationrecorder.Playback.SourceAudio;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentSourceAudio extends Fragment {

    private SourceAudio mSourcePlayer;

    public static FragmentSourceAudio newInstance(){
        FragmentSourceAudio f = new FragmentSourceAudio();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_source_audio, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSourcePlayer = (SourceAudio)getView().findViewById(R.id.source_audio_player);
    }

    public void loadAudio(Project project, String filename, int chapter){
        mSourcePlayer.initSrcAudio(project, filename, chapter);
    }

    public void disableSourceAudio() {
        mSourcePlayer.cleanup();
        mSourcePlayer.setEnabled(false);
    }

    public void resetSourceAudio(Project project, String filename, int chapter) {
        mSourcePlayer.reset(project, filename, chapter);
    }

    public void initialize(Project project, String filename, int chapter) {
        mSourcePlayer.initSrcAudio(project, filename, chapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSourcePlayer.pauseSource();
        mSourcePlayer.cleanup();
    }
}
