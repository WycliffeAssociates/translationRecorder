package org.wycliffeassociates.translationrecorder.widgets;

import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.door43.tools.reporting.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by sarabiaj on 7/7/2016.
 */
public class AudioPlayer {

    // Views
    private TextView mElapsedView, mDurationView;
    private ImageButton mPlayPauseBtn;
    private SeekBar mSeekBar;

    // Attributes
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private int mCurrentProgress = 0;
    private int mDuration;

    // State
    private boolean mPlayerReleased = false;
    private boolean mIsLoaded = false;


    // Constructor
    public AudioPlayer() {
        mMediaPlayer = new MediaPlayer();
    }

    public AudioPlayer(TextView elapsedView, TextView durationView, ImageButton playPauseBtn, SeekBar seekBar){
        mMediaPlayer = new MediaPlayer();
        refreshView(elapsedView, durationView, playPauseBtn, seekBar);
    }


    // Overrides
    @Override
    protected void finalize() throws Throwable {
        try {
            cleanup();
        } finally {
            super.finalize();
        }
    }


    // Setters
    public void setSeekBarView(SeekBar seekBar) {
        mSeekBar = seekBar;
        if (isLoaded()) {
            mSeekBar.setMax(mMediaPlayer.getDuration());
        }
        updateSeekBar(mCurrentProgress);
    }

    public void setPlayPauseBtn(ImageButton playPauseBtn) {
        mPlayPauseBtn = playPauseBtn;
        togglePlayPauseButton(mMediaPlayer.isPlaying());
    }

    public void setElapsedView(TextView elapsedView) {
        mElapsedView = elapsedView;
        updateElapsedView(mCurrentProgress);
        if (mSeekBar != null) {
            attachSeekBarListener();
        }
    }

    public void setDurationView(TextView durationView) {
        mDurationView = durationView;
        updateDurationView(mDuration);
        if (mMediaPlayer != null && mSeekBar != null) {
            attachMediaPlayerListener();
        }
    }


    // Getters
    public boolean isLoaded() {
        return mIsLoaded;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }


    // Private Methods
    private void attachSeekBarListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    updateElapsedView(progress);
                }
            }
        });
    }

    private void attachMediaPlayerListener(){
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                togglePlayPauseButton(false);
                if (mSeekBar != null) {
                    mMediaPlayer.seekTo(0);
                    int max = mSeekBar.getMax();
                    updateDurationView(max);
                    updateSeekBar(0);
                    updateElapsedView(0);
                }
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(0);
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.e(mp.toString(), "onError called, error what is " + what + " error extra is " + extra);
                return false;
            }
        });
    }

    private void togglePlayPauseButton(boolean isPlaying) {
        if (mPlayPauseBtn != null) {
            mPlayPauseBtn.setActivated(isPlaying);
        }
    }

    private String convertTimeToString(int time) {
        return String.format("%02d:%02d:%02d", time / 3600000, (time / 60000) % 60, (time / 1000) % 60);
    }


    // Public API
    public void refreshView(TextView elapsedView, TextView durationView, ImageButton playPauseBtn, SeekBar seekBar) {
        setSeekBarView(seekBar);
        setElapsedView(elapsedView);
        setDurationView(durationView);
        setPlayPauseBtn(playPauseBtn);
        attachSeekBarListener();
        attachMediaPlayerListener();
    }

    public void loadFile(File file){
        try {
            togglePlayPauseButton(false);
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mIsLoaded = true;
            mMediaPlayer.prepare();
            if (mSeekBar != null) {
                mDuration = mMediaPlayer.getDuration();
                mSeekBar.setMax(mDuration);
                updateDurationView(mDuration);
            }
        } catch (IOException e) {
            Logger.w(this.toString(), "loading a file threw an IO exception");
            e.printStackTrace();
        }
    }

    public void play(){
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.start();
                mHandler = new Handler();
                togglePlayPauseButton(true);
                updateSeekBar(0);
                Runnable loop = new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaPlayer != null && !mPlayerReleased) {
                            mCurrentProgress = mMediaPlayer.getCurrentPosition();
                            updateElapsedView(mCurrentProgress);
                            if (mSeekBar != null && mCurrentProgress > mSeekBar.getProgress()) {
                                updateSeekBar(mCurrentProgress);
                            }
                        }
                        mHandler.postDelayed(this, 200);
                    }
                };
                loop.run();
            } catch (IllegalStateException e){
                Logger.w(this.toString(), "playing threw an illegal state exception");
            }
        }
    }

    public void pause(){
        if(mMediaPlayer != null && !mPlayerReleased && mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.pause();
                togglePlayPauseButton(false);
            } catch (IllegalStateException e) {
                Logger.w(this.toString(), "Pausing threw an illegal state exception");
            }
        }
    }

    public void reset(){
        synchronized (mMediaPlayer){
            if(!mPlayerReleased && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
            mMediaPlayer.reset();
            mIsLoaded = false;
            updateSeekBar(0);
            updateElapsedView(0);
        }
    }

    public void cleanup(){
        synchronized (mMediaPlayer){
            if(!mPlayerReleased) {
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
            }
            mPlayerReleased = true;
        }
    }

    public void updateElapsedView(int elapsed) {
        if (mElapsedView == null) {
            return;
        }
        mElapsedView.setText(convertTimeToString(elapsed));
        mElapsedView.invalidate();
    }

    public void updateDurationView(int duration) {
        if (mDurationView == null) {
            return;
        }
        mDurationView.setText(convertTimeToString(duration));
        mDurationView.invalidate();
    }

    public void updateSeekBar(int progress) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
            mSeekBar.invalidate();
        }
    }
}
