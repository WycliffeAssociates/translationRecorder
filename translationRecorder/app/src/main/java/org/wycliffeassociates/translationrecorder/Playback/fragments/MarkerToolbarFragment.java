package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaController;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.VerseMarkerModeToggler;
import org.wycliffeassociates.translationrecorder.R;

import org.wycliffeassociates.translationrecorder.widgets.PlaybackTimer;

/**
 * Created by sarabiaj on 11/15/2016.
 */

public class MarkerToolbarFragment extends Fragment {

    private ImageView mPlaceMarker;

    public interface OnMarkerPlacedListener {
        void onMarkerPlaced();
    }

    private OnMarkerPlacedListener mOnMarkerPlacedListener;
    private VerseMarkerModeToggler mModeToggleCallback;
    private MediaController mMediaController;
    private ImageButton mPlayBtn;
    private ImageButton mPauseBtn;
    private ImageButton mSkipBackBtn;
    private ImageButton mSkipForwardBtn;
    private TextView mPlaybackElapsed;
    private TextView mPlaybackDuration;
    private PlaybackTimer mTimer;

    public static MarkerToolbarFragment newInstance(){
        MarkerToolbarFragment f = new MarkerToolbarFragment();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mModeToggleCallback = (VerseMarkerModeToggler) activity;
        mMediaController = (MediaController) activity;
        mOnMarkerPlacedListener = (OnMarkerPlacedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_marker_toolbar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        initViews();
        initTimer(mPlaybackElapsed, mPlaybackDuration);
        if (mMediaController.isPlaying()) {
            showPauseButton();
        } else {
            showPlayButton();
        }
    }

    private void findViews(){
        View view = getView();
        mPlayBtn = (ImageButton) view.findViewById(R.id.btn_play);
        mPauseBtn = (ImageButton) view.findViewById(R.id.btn_pause);
        mSkipBackBtn = (ImageButton) view.findViewById(R.id.btn_skip_back);
        mSkipForwardBtn = (ImageButton) view.findViewById(R.id.btn_skip_forward);

        mPlaybackElapsed = (TextView) view.findViewById(R.id.playback_elapsed);
        mPlaybackDuration = (TextView) view.findViewById(R.id.playback_duration);

        mPlaceMarker = (ImageView) view.findViewById(R.id.btn_drop_verse_marker);
    }

    public void showPlayButton(){
        Utils.swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }

    public void showPauseButton(){
        Utils.swapViews(new View[]{mPauseBtn}, new View[]{mPlayBtn});
    }

    private void initViews(){
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

        mPlaceMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnMarkerPlacedListener.onMarkerPlaced();
            }
        });
    }

    private void initTimer(final TextView elapsed, final TextView duration) {
        mTimer = new PlaybackTimer(elapsed, duration);
        mTimer.setElapsed(mMediaController.getLocationMs());
        mTimer.setDuration(mMediaController.getDurationMs());
    }

    public void onLocationUpdated(int ms){
        if(mTimer != null) {
            mTimer.setElapsed(ms);
        }
    }

    public void onDurationUpdated(int ms){
        if(mTimer != null){
            mTimer.setDuration(ms);
        }
    }

    public void invalidate(int ms) {

    }
}
