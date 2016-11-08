package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import wycliffeassociates.recordingapp.Playback.interfaces.AudioEditDelegator;
import wycliffeassociates.recordingapp.Playback.interfaces.EditStateInformer;
import wycliffeassociates.recordingapp.Playback.interfaces.MediaController;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.widgets.PlaybackTimer;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentPlaybackTools extends Fragment{

    MediaController mMediaController;
    AudioEditDelegator mAudioEditDelegator;
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
        try {
            mAudioEditDelegator = (AudioEditDelegator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AudioEditDelegator");
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

    public void onLocationUpdated(int ms){
        mTimer.setElapsed(ms);
    }

    public void onDurationUpdated(int ms){
        mTimer.setDuration(ms);
    }

    private void attachListeners(){
        attachMediaControllerListeners();
    }

    private void attachMediaControllerListeners(){
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.swapViews(new View[]{mPauseBtn}, new View[]{mPlayBtn});
                mMediaController.onMediaPlay();
            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
                mMediaController.onMediaPause();
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

        mDropStartMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.swapViews(new View[]{mDropEndMarkBtn, mClearBtn}, new View[]{mDropStartMarkBtn});
                mAudioEditDelegator.onDropStartMarker();
            }
        });

        mDropEndMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.swapViews(new View[]{mCutBtn}, new View[]{mDropEndMarkBtn});
                mAudioEditDelegator.onDropEndMarker();
            }
        });

        mDropVerseMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioEditDelegator.onDropVerseMarker();
            }
        });

        mCutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.swapViews(new View[]{mDropStartMarkBtn, mUndoBtn}, new View[]{mCutBtn, mClearBtn});
                mAudioEditDelegator.onCut();
            }
        });

        mUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View[] toHide = {};
                if(((EditStateInformer)mAudioEditDelegator).hasEdits()){
                    toHide = new View[]{mUndoBtn};
                }
                Utils.swapViews(new View[]{}, toHide);
                mAudioEditDelegator.onUndo();
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.swapViews(new View[]{mDropStartMarkBtn}, new View[]{mClearBtn, mDropStartMarkBtn, mCutBtn});
                mAudioEditDelegator.onClearMarkers();
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioEditDelegator.onSave();
            }
        });
    }

    public void onPlayerPaused(){
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }
}
