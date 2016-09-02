package wycliffeassociates.recordingapp.widgets;

import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 7/7/2016.
 */
public class AudioPlayer {

    // Constants
    private String DEFAULT_TIME_ELAPSED = "00:00:00";
    private String DEFAULT_TIME_DURATION = "00:00:00";

    // Views
    private MediaPlayer mMediaPlayer;
    private TextView mElapsedView, mDurationView;
    private ImageButton mPlayPauseBtn;
    private SeekBar mSeekBar;
    private Handler mHandler;

    // Attributes
    private int mCurrentProgress = 0;
    private String mElapsed, mDuration;

    // State
    private boolean mPlayerReleased = false;
    private boolean mIsLoaded = false;


    // Constructor
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
        updateProgress(mCurrentProgress);
    }

    public void setPlayPauseBtn(ImageButton playPauseBtn) {
        mPlayPauseBtn = playPauseBtn;
        togglePlayPauseButton(mMediaPlayer.isPlaying());
    }

    public void setElapsedView(TextView elapsedView) {
        mElapsedView = elapsedView;
        updateElapsedView(mElapsed);
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
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isLoaded() {
        return mIsLoaded;
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
                    mElapsed = convertTimeToString(progress);
                    updateElapsedView(mElapsed);
                }
            }
        });
    }

    private void attachMediaPlayerListener(){
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                togglePlayPauseButton(false);
                updateProgress(mSeekBar.getMax());
                int duration = mSeekBar.getMax();
                mDuration = convertTimeToString(duration);
                updateDurationView(mDuration);

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
        mPlayPauseBtn.setActivated(isPlaying);
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
            int duration = mMediaPlayer.getDuration();
            mSeekBar.setMax(duration);
            mDuration = convertTimeToString(duration);
            updateDurationView(mDuration);
        } catch (IOException e) {
            Logger.w(this.toString(), "loading a file threw an IO exception");
            e.printStackTrace();
        }
    }

    public void play(){
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.start();
                togglePlayPauseButton(true);
                mHandler = new Handler();
                updateProgress(0);
                Runnable loop = new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaPlayer != null && !mPlayerReleased) {
                            mCurrentProgress = mMediaPlayer.getCurrentPosition();
                            if (mCurrentProgress > mSeekBar.getProgress()) {
                                updateProgress(mCurrentProgress);
                                mElapsed = convertTimeToString(mCurrentProgress);
                                updateElapsedView(mElapsed);
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
            updateProgress(0);
            updateElapsedView(null);
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

    public void updateElapsedView(String elapsed) {
        mElapsedView.setText(elapsed == null ? DEFAULT_TIME_ELAPSED : elapsed);
        mElapsedView.invalidate();
    }

    public void updateDurationView(String duration) {
        mDurationView.setText(duration == null ? DEFAULT_TIME_DURATION : duration);
        mDurationView.invalidate();
    }

    public void updateProgress(int progress) {
        mSeekBar.setProgress(progress);
        mSeekBar.invalidate();
    }
}
