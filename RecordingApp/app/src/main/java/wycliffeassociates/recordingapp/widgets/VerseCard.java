package wycliffeassociates.recordingapp.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.WavFile;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class VerseCard extends FrameLayout {

    private SeekBar mSeekBar;
    MediaPlayer mMediaPlayer;
    List<File> mFiles;
    TextView mTakes, mTimeElapsed, mTimeDuration;
    ImageButton play, pause;
    int mTakeIndex = 0;

    public VerseCard(Context context) {
        this(context, null);
    }

    public VerseCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerseCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.verse_card_widget, this);

        mTakes = (TextView) findViewById(R.id.take_view);
        findViewById(R.id.expanded_card).setVisibility(View.GONE);
        findViewById(R.id.base_card).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View expanded = findViewById(R.id.expanded_card);
                if(expanded.getVisibility() == GONE){
                    expanded.setVisibility(VISIBLE);
                } else {
                    expanded.setVisibility(GONE);
                }
            }
        });
        findViewById(R.id.play_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PlaybackScreen.class);
                WavFile wavFile = new WavFile(mFiles.get(mTakeIndex));
                intent.putExtra("wavfile", wavFile);
                intent.putExtra("loadfile", true);
                v.getContext().startActivity(intent);

            }
        });

        findViewById(R.id.inc_take).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakeIndex++;
                mTakeIndex %= mFiles.size();
                mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
                try {
                    if(mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(mFiles.get(mTakeIndex).getAbsolutePath());
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }  catch (IllegalStateException e){

                }
            }
        });

        findViewById(R.id.dec_take).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakeIndex--;
                mTakeIndex %= mFiles.size();
                mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
                try {
                    if(mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(mFiles.get(mTakeIndex).getAbsolutePath());
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){

                }
            }
        });

        findViewById(R.id.playButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.INVISIBLE);
            }
        });

        findViewById(R.id.pauseButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pause();
                pause.setVisibility(View.INVISIBLE);
                play.setVisibility(View.VISIBLE);
            }
        });

        play = (ImageButton)findViewById(R.id.playButton);
        pause = (ImageButton)findViewById(R.id.pauseButton);

        findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile();
            }
        });

        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mTimeElapsed = (TextView)findViewById(R.id.timeProgress);
        mTimeDuration = (TextView)findViewById(R.id.timeDuration);
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
                    mTimeElapsed.setText(time);
                    mTimeElapsed.invalidate();
                }
            }
        });
    }

    public void initialize(List<File> files){
        mFiles = files;
        if(files.size() > 0) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(files.get(mTakeIndex).getCanonicalPath());
                mMediaPlayer.prepare();
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        pause.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.VISIBLE);
                        mSeekBar.setProgress(mSeekBar.getMax());
                        int duration = mSeekBar.getMax();
                        final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
                                mTimeDuration.setText(time);
                                mTimeDuration.invalidate();

                        if(mMediaPlayer.isPlaying()) {
                            mMediaPlayer.seekTo(0);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e){

            }
        }
        mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
    }

    public void refreshTakeText(){
        mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
    }

    public void deleteFile(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete recording?");
        builder.setIcon(R.drawable.ic_delete_black_36dp);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == dialog.BUTTON_POSITIVE){
                    mFiles.get(mTakeIndex).delete();
                    mFiles.remove(mTakeIndex);
                    mTakeIndex = 0;
                    refreshTakeText();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
