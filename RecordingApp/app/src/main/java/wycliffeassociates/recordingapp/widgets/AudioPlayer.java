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

/**
 * Created by sarabiaj on 7/7/2016.
 */
public class AudioPlayer {

    MediaPlayer mMediaPlayer;
    TextView mProgress, mDuration;
    ImageButton mPlay;
    SeekBar mSeekBar;
    private Handler mHandler;
    private boolean mPlayerReleased= false;

    public AudioPlayer(TextView progress, TextView duration, ImageButton play, SeekBar seek){
        mProgress = progress;
        mDuration = duration;
        mPlay = play;
        mSeekBar = seek;
        mMediaPlayer = new MediaPlayer();
        attachListeners();
    }

    private void attachListeners(){
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
                    final String time = String.format("%02d:%02d:%02d", progress / 3600000, (progress / 60000) % 60, (progress / 1000) % 60);
                    mProgress.setText(time);
                    mProgress.invalidate();
                }
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                togglePlayPauseButton(false);
                mSeekBar.setProgress(mSeekBar.getMax());
                int duration = mSeekBar.getMax();
                final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);

                mDuration.setText(time);
                mDuration.invalidate();

                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(0);
                }
            }
        });
    }

    private void togglePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            mPlay.setActivated(true);
        } else {
            mPlay.setActivated(false);
        }
    }

    public void loadFile(File file){
        try {
            togglePlayPauseButton(false);
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.prepare();
            int duration = mMediaPlayer.getDuration();
            mSeekBar.setMax(duration);
            final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
            mDuration.setText(time);
            mDuration.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(){
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.start();
                togglePlayPauseButton(true);
                mHandler = new Handler();
                mSeekBar.setProgress(0);
                System.out.println(mSeekBar.getProgress());
                mSeekBar.invalidate();
                Runnable loop = new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaPlayer != null && !mPlayerReleased) {
                            int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                            if (mCurrentPosition > mSeekBar.getProgress()) {
                                mSeekBar.setProgress(mCurrentPosition);
                                final String time = String.format("%02d:%02d:%02d", mCurrentPosition / 3600000, (mCurrentPosition / 60000) % 60, (mCurrentPosition / 1000) % 60);
                                mProgress.setText(time);
                                mProgress.invalidate();
                            }
                        }
                        mHandler.postDelayed(this, 200);
                    }
                };
                loop.run();
            } catch (IllegalStateException e){

            }
        }
    }

    public void pause(){
        if(mMediaPlayer != null && !mPlayerReleased && mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.pause();
                togglePlayPauseButton(false);
            } catch (IllegalStateException e) {

            }
        }
    }

    public void reset(){
        synchronized (mMediaPlayer){
            if(!mPlayerReleased && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
            mMediaPlayer.reset();
        }
    }

    public void cleanup(){
        synchronized (mMediaPlayer){
            if(!mPlayerReleased && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
            mMediaPlayer.release();
            mPlayerReleased = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cleanup();
    }
}
