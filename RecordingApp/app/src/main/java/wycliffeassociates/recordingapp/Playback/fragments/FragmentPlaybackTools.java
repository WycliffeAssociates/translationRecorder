package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentPlaybackTools extends Fragment {

    ImageButton mPlayBtn, mPauseBtn, mSkipBackBtn, mSkipForwardBtn,
            mDropStartMarkBtn, mDropEndMarkBtn,
            mUndoBtn, mCutBtn, mClearBtn,
            mSaveBtn, mDropVerseMarkerBtn;

    private TextView mPlaybackDuration;
    private TextView mPlaybackElapsed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_toolbar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
    }

    private void findViews(){
        View view = getView();
        mPlayBtn = (ImageButton) view.findViewById(R.id.btn_play);
        mPauseBtn = (ImageButton) view.findViewById(R.id.btn_pause);
        mSkipBackBtn = (ImageButton) view.findViewById(R.id.btn_skip_back);
        mSkipForwardBtn = (ImageButton) view.findViewById(R.id.btn_skip_forward);
        mDropStartMarkBtn = (ImageButton) view.findViewById(R.id.btn_start_mark);
        mDropEndMarkBtn = (ImageButton) view.findViewById(R.id.btn_end_mark);
        mUndoBtn = (ImageButton) view.findViewById(R.id.btn_undo);
        mCutBtn = (ImageButton) view.findViewById(R.id.btn_cut);
        mClearBtn = (ImageButton) view.findViewById(R.id.btn_clear);
        mSaveBtn = (ImageButton) view.findViewById(R.id.btn_save);
        mDropVerseMarkerBtn = (ImageButton) view.findViewById(R.id.btn_drop_verse_marker);
        mPlaybackElapsed = (TextView) view.findViewById(R.id.playback_elapsed);
        mPlaybackDuration = (TextView) view.findViewById(R.id.playback_duration);
    }
}
