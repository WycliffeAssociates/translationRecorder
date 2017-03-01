package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.Playback.interfaces.AudioEditDelegator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.EditStateInformer;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaController;
import org.wycliffeassociates.translationrecorder.Utils;

import org.wycliffeassociates.translationrecorder.R;

import org.wycliffeassociates.translationrecorder.widgets.PlaybackTimer;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentPlaybackTools extends Fragment {

    MediaController mMediaController;
    AudioEditDelegator mAudioEditDelegator;
    ImageButton mPlayBtn, mPauseBtn, mSkipBackBtn, mSkipForwardBtn,
            mDropStartMarkBtn, mDropEndMarkBtn,
            mUndoBtn, mCutBtn, mClearBtn,

    mSaveBtn, mDropVerseMarkerBtn;
    private TextView mPlaybackDuration;

    private TextView mPlaybackElapsed;
    private PlaybackTimer mTimer;
    private boolean mUndoVisible = false;

    public static FragmentPlaybackTools newInstance() {
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
        } catch (ClassCastException e) {
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
        mAudioEditDelegator = null;
        //hold the visibility of the undo button for switching contexts to verse marker mode and back
        mUndoVisible = mUndoBtn.getVisibility() == View.VISIBLE;
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
        if (mMediaController.isPlaying()) {
            showPauseButton();
        } else {
            showPlayButton();
        }
        //restore undo button visibility from detach
        mUndoBtn.setVisibility((mUndoVisible) ? View.VISIBLE : View.INVISIBLE);
    }

    private void findViews() {
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
        mTimer.setElapsed(mMediaController.getLocationMs());
        mTimer.setDuration(mMediaController.getDurationMs());
    }

    public void onLocationUpdated(int ms) {
        mTimer.setElapsed(ms);
    }

    public void onDurationUpdated(int ms) {
        mTimer.setDuration(ms);
    }

    private void attachListeners() {
        attachMediaControllerListeners();
    }

    public void showPauseButton() {
        Utils.swapViews(new View[]{mPauseBtn}, new View[]{mPlayBtn});
    }

    public void showPlayButton() {
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }

    public void viewOnSetStartMarker() {
        Utils.swapViews(new View[]{mDropEndMarkBtn, mClearBtn}, new View[]{mDropStartMarkBtn});
    }

    public void viewOnSetEndMarker() {
        Utils.swapViews(new View[]{mCutBtn}, new View[]{mDropEndMarkBtn, mDropStartMarkBtn});
    }

    public void viewOnSetBothMarkers() {
        Utils.swapViews(new View[]{mCutBtn}, new View[]{mDropEndMarkBtn, mDropStartMarkBtn});
    }

    public void viewOnCut() {
        Utils.swapViews(new View[]{mDropStartMarkBtn, mUndoBtn}, new View[]{mCutBtn, mClearBtn});
    }

    public void viewOnUndo() {
        View[] toHide = {};
        if (!((EditStateInformer) mAudioEditDelegator).hasEdits()) {
            toHide = new View[]{mUndoBtn};
        }
        Utils.swapViews(new View[]{}, toHide);
    }

    public void viewOnClearMarkers() {
        Utils.swapViews(new View[]{mDropStartMarkBtn}, new View[]{mClearBtn, mCutBtn, mDropEndMarkBtn});
    }

    private void attachMediaControllerListeners() {
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPauseButton();
                mMediaController.onMediaPlay();
            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayButton();
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
                viewOnSetStartMarker();
                mAudioEditDelegator.onDropStartMarker();
            }
        });

        mDropEndMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnSetEndMarker();
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
                viewOnCut();
                mAudioEditDelegator.onCut();
            }
        });

        mUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioEditDelegator.onUndo();
                viewOnUndo();
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnClearMarkers();
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

    public void onPlayerPaused() {
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }

    public void invalidate(int ms) {

    }
}
