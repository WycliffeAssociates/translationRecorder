package wycliffeassociates.recordingapp.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
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
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.WavFile;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class VerseCard extends FrameLayout {

    private SeekBar mSeekBar;
    List<File> mFiles;
    TextView mTakes, mTimeElapsed, mTimeDuration;
    ImageButton mPlay, mPause;
    AudioPlayer mAudioPlayer;
    int mTakeIndex = 0;
    Handler mHandler;
    private Project mProject;
    private int mChapter;
    private int mUnit;

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
                View play = findViewById(R.id.play_button);
                if(expanded.getVisibility() == GONE){
                    if(mFiles.size() > 0) {
                        mAudioPlayer.reset();
                        mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
                    }
                    expanded.setVisibility(VISIBLE);
                    play.setVisibility(GONE);
                } else {
                    expanded.setVisibility(GONE);
                    play.setVisibility(VISIBLE);
                }
            }
        });
        findViewById(R.id.play_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    WavFile wavFile = new WavFile(mFiles.get(mFiles.size() - 1));
                    Intent intent = PlaybackScreen.getPlaybackIntent(v.getContext(), wavFile, mProject, mChapter, mUnit);
                    v.getContext().startActivity(intent);
                }

            }
        });

        findViewById(R.id.edit_take_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    WavFile wavFile = new WavFile(mFiles.get(mTakeIndex));
                    Intent intent = PlaybackScreen.getPlaybackIntent(v.getContext(), wavFile, mProject, mChapter, mUnit);
                    v.getContext().startActivity(intent);
                }
            }
        });

        findViewById(R.id.inc_take).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    mTakeIndex++;
                    if (mTakeIndex >= mFiles.size()) {
                        mTakeIndex = 0;
                    }
                    refreshTakeText();
                    mAudioPlayer.reset();
                    mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
                }
            }
        });

        findViewById(R.id.dec_take).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    mTakeIndex--;
                    if (mTakeIndex < 0) {
                        mTakeIndex = mFiles.size() - 1;
                    }
                    refreshTakeText();
                    mAudioPlayer.reset();
                    mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
                }
            }
        });


        findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    deleteFile();
                }
            }
        });

        mPlay = (ImageButton)findViewById(R.id.playButton);
        mPause = (ImageButton)findViewById(R.id.pauseButton);

        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFiles.size() > 0) {
                    mAudioPlayer.play();
                }
            }
        });

        mPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.pause();
            }
        });

        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mTimeElapsed = (TextView)findViewById(R.id.timeProgress);
        mTimeDuration = (TextView)findViewById(R.id.timeDuration);

        mAudioPlayer = new AudioPlayer(mTimeElapsed, mTimeDuration, mPlay, mPause, mSeekBar);

    }

    public void initialize(List<File> files, Project project, int chapter, int unit){
        mProject = project;
        mChapter = chapter;
        mUnit = unit;
        mFiles = files;
        refreshTakeText();
    }

    public void refreshTakeText(){
        if(mFiles.size() > 0) {
            mTakes.setText("Take " + (mTakeIndex + 1) + " of " + mFiles.size());
        } else {
            mTakes.setText("Take 0 of " + mFiles.size());
        }
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
                    //keep the same index in the list, unless the one removed was the last take.
                    if(mTakeIndex > mFiles.size()-1){
                        mTakeIndex--;
                    }
                    refreshTakeText();
                    if(mFiles.size() > 0){
                        mAudioPlayer.reset();
                        mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
                    }
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
