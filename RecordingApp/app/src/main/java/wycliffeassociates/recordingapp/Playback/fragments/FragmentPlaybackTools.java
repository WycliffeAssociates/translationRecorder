package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.Playback.interfaces.MediaController;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.widgets.PlaybackTimer;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentPlaybackTools extends Fragment{

    MediaController mMediaController;
    ImageButton mPlayBtn, mPauseBtn, mSkipBackBtn, mSkipForwardBtn,
            mDropStartMarkBtn, mDropEndMarkBtn,
            mUndoBtn, mCutBtn, mClearBtn,
            mSaveBtn, mDropVerseMarkerBtn;

    private TextView mPlaybackDuration;
    private TextView mPlaybackElapsed;

    private PlaybackTimer mTimer;

    public static FragmentPlaybackTools newInstance(){
        FragmentPlaybackTools f = new FragmentPlaybackTools();
        return f;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mMediaController = (MediaController) activity;
            mMediaController.setOnCompleteListner(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
                        }
                    });
                }
            });
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement MediaController");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaController = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_toolbar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        attachListeners();
        initTimer(mPlaybackElapsed, mPlaybackDuration);
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
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

        mDropVerseMarkerBtn = (ImageButton) view.findViewById(R.id.btn_drop_verse_marker);

        mPlaybackElapsed = (TextView) view.findViewById(R.id.playback_elapsed);
        mPlaybackDuration = (TextView) view.findViewById(R.id.playback_duration);

        mSaveBtn = (ImageButton) view.findViewById(R.id.btn_save);
    }

    private void initTimer(final TextView elapsed, final TextView duration) {
        mTimer = new PlaybackTimer(elapsed, duration);
        mTimer.setElapsed(0);
        mTimer.setDuration(mMediaController.getDuration());
    }

    private void attachListeners(){
        attachMediaControllerListeners();
    }

    private void attachMediaControllerListeners(){
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.onPlay();
                Utils.swapViews(new View[]{mPauseBtn}, new View[]{mPlayBtn});
            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.onPause();
                Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
            }
        });

        mSkipBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.onSeekBackward();
            }
        });

        mSkipForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.onSeekForward();
            }
        });
    }

    public void onPlayerPaused(){
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }
}
