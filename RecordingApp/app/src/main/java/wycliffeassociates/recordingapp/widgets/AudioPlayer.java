package wycliffeassociates.recordingapp.widgets;

import android.media.MediaPlayer;
import android.media.TimedText;
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
    ImageButton mPlay, mPause;
    SeekBar mSeekBar;

    public AudioPlayer(TextView progress, TextView duration, ImageButton play, ImageButton pause, SeekBar seek){
        mProgress = progress;
        mDuration = duration;
        mPlay = play;
        mPause = pause;
        mSeekBar = seek;
        mMediaPlayer = new MediaPlayer();
    }

    private void switchPlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            mPause.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.INVISIBLE);
        } else {
            mPlay.setVisibility(View.VISIBLE);
            mPause.setVisibility(View.INVISIBLE);
        }
    }

    public void loadFile(File file){
        try {
//            if(mMediaPlayer.isPlaying()){
//                mMediaPlayer.pause();
//            }
//            mMediaPlayer.stop();
            switchPlayPauseButton(false);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(file.getCanonicalPath());
            mMediaPlayer.prepare();
            int duration = mMediaPlayer.getDuration();
            mSeekBar.setMax(duration);
            final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
            mDuration.setText(time);
            mDuration.invalidate();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switchPlayPauseButton(false);
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
        } catch (IOException e){
            e.printStackTrace();
        } catch (IllegalStateException e){

        }
    }

    public void play(){
        if(!mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.start();
                switchPlayPauseButton(true);
            } catch (IllegalStateException e){

            }
        }
    }

    public void pause(){
        if(mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.pause();
                switchPlayPauseButton(false);
            } catch (IllegalStateException e) {

            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }
}
