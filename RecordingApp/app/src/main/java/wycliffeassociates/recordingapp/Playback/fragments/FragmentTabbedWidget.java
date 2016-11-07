package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import wycliffeassociates.recordingapp.Playback.SourceAudio;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentTabbedWidget extends Fragment {

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_FILENAME = "key_filename";
    private static final String KEY_CHAPTER = "key_chapter";

    private ImageButton mSwitchToMinimap;
    private ImageButton mSwitchToPlayback;
    private SourceAudio mSrcPlayer;

    String mFilename = "";
    Project mProject;
    int mChapter = 0;

    public static FragmentTabbedWidget newInstance(Project project, String filename, int chapter){
        FragmentTabbedWidget f = new FragmentTabbedWidget();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, project);
        args.putString(KEY_FILENAME, filename);
        args.putInt(KEY_CHAPTER, chapter);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_tabbed_widget, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        if(!view.isInEditMode()){
            mSrcPlayer.initSrcAudio(mProject, mFilename, mChapter);
        }
    }

    void findViews(){
        View view = getView();
        mSwitchToMinimap = (ImageButton) view.findViewById(R.id.switch_minimap);
        mSwitchToPlayback = (ImageButton) view.findViewById(R.id.switch_source_playback);
        mSrcPlayer = (SourceAudio) view.findViewById(R.id.srcAudioPlayer);

    }
}
