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
    ImageButton mPlay, mPause;
    AudioPlayer mAudioPlayer;
    int mTakeIndex = 0;
    Handler mHandler;

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
                WavFile wavFile = new WavFile(mFiles.get(mFiles.size()-1));
                intent.putExtra("wavfile", wavFile);
                intent.putExtra("loadfile", true);
                v.getContext().startActivity(intent);

            }
        });

        findViewById(R.id.edit_take_button).setOnClickListener(new OnClickListener() {
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
                if(mTakeIndex >= mFiles.size()){
                    mTakeIndex = 0;
                }
                mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
                mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
            }
        });

        findViewById(R.id.dec_take).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakeIndex--;
                if(mTakeIndex < 0){
                    mTakeIndex = mFiles.size()-1;
                }
                mTakes.setText("Take " + (mTakeIndex+1) + " of " + mFiles.size());
                mAudioPlayer.loadFile(mFiles.get(mTakeIndex));
            }
        });


        findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile();
            }
        });

        mPlay = (ImageButton)findViewById(R.id.playButton);
        mPause = (ImageButton)findViewById(R.id.pauseButton);

        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.play();
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

    public void initialize(List<File> files){
        mFiles = files;
        if(files.size() > 0) {
            mAudioPlayer.loadFile(files.get(mTakeIndex));
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
