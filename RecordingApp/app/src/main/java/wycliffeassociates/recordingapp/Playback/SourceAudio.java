package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognito.Record;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 4/13/2016.
 */
public class SourceAudio extends LinearLayout {

    private Activity mCtx;
    private SeekBar mSeekBar;
    private TextView mSrcTimeElapsed;
    private TextView mSrcTimeDuration;
    private MediaPlayer mSrcPlayer;
    private ImageButton mBtnSrcPlay;
    private ImageButton mBtnSrcPause;
    private TextView mNoSourceMsg;
    private Handler mHandler;
    private volatile boolean mPlayerReleased = false;
    private Project mProject;
    private String mFileName;
    private int mChapter;

    public SourceAudio(Context context) {
        this(context, null);
    }

    public SourceAudio(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SourceAudio(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.source_audio, this);
        mSrcTimeElapsed = (TextView) findViewById(R.id.timeProgress);
        mSrcTimeDuration = (TextView) findViewById(R.id.timeDuration);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mBtnSrcPlay = (ImageButton) findViewById(R.id.playButton);
        mBtnSrcPause = (ImageButton) findViewById(R.id.pauseButton);
        mNoSourceMsg = (TextView) findViewById(R.id.noSourceMsg);
        mSrcPlayer = new MediaPlayer();
        mCtx = (Activity) getContext();

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.playButton) {
                    playSource();
                } else if (v.getId() == R.id.pauseButton){
                    pauseSource();
                }
            }
        };

        mBtnSrcPlay.setOnClickListener(onClickListener);
        mBtnSrcPause.setOnClickListener(onClickListener);
    }

    private DocumentFile getSourceAudioDirectory(){
        String srcLoc = mProject.getSourceAudioPath();
        String sourceLang = mProject.getSourceLanguage();
        if(srcLoc == null || sourceLang == null || srcLoc.compareTo("") == 0 || sourceLang.compareTo("") == 0) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
            srcLoc = sp.getString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, null);
            sourceLang = sp.getString(Settings.KEY_PREF_GLOBAL_LANG_SRC, null);
        }
        if(srcLoc == null || sourceLang == null || srcLoc.compareTo("") == 0 || sourceLang.compareTo("") == 0) {
            return null;
        }
        Uri uri = Uri.parse(srcLoc);
        if(uri != null){
            DocumentFile df = DocumentFile.fromTreeUri(mCtx, uri);
            if(df != null) {
                DocumentFile langDf = df.findFile(sourceLang);
                if(langDf != null) {
                    DocumentFile srcDf = langDf.findFile(mProject.getSource());
                    if(srcDf != null) {
                        DocumentFile bookDf = srcDf.findFile(mProject.getSlug());
                        if(bookDf != null) {
                            DocumentFile chapDf = bookDf.findFile(FileNameExtractor.chapterIntToString(mProject, mChapter));
                            return chapDf;
                        }
                    }
                }
            }
        }
        return null;
    }

    private DocumentFile getSourceAudioFile(){
        DocumentFile directory = getSourceAudioDirectory();
        if(directory == null){
            return null;
        }
        String[] filetypes = {"wav", "mp3", "mp4", "m4a", "aac", "flac", "3gp", "ogg"};
        DocumentFile[] files = directory.listFiles();
        for(DocumentFile f : files){
            if(FileNameExtractor.getNameWithoutTake(f.getName()).compareTo(mFileName) == 0){
                //make sure the filetype is supported
                String ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
                for(String s : filetypes){
                    if(ext.compareTo(s) == 0){
                        return f;
                    }
                }
            }
        }

        return null;
    }

    private File getSourceAudioFileKitkat(){
        File directory = getSourceAudioFileDirectoryKitkat();
        if(directory == null || !directory.exists()){
            return null;
        } else {
            String[] filetypes = {"wav", "mp3", "mp4", "m4a", "aac", "flac", "3gp", "ogg"};
            File[] files = directory.listFiles();
            for(File f : files){
                if(FileNameExtractor.getNameWithoutTake(f.getName()).compareTo(mFileName) == 0){
                    //make sure the filetype is supported
                    String ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
                    for(String s : filetypes){
                        if(ext.compareTo(s) == 0){
                            return f;
                        }
                    }
                }
            }
        }
        return null;
    }

    private File getSourceAudioFileDirectoryKitkat(){
        File file = mProject.getProjectDirectory(mProject);
        return file;
    }

    private void switchPlayPauseBtn(boolean isPlaying) {
        if (isPlaying) {
            mBtnSrcPause.setVisibility(View.VISIBLE);
            mBtnSrcPlay.setVisibility(View.INVISIBLE);
        } else {
            mBtnSrcPlay.setVisibility(View.VISIBLE);
            mBtnSrcPause.setVisibility(View.INVISIBLE);
        }
    }

    public void initSrcAudio(Project project, String fileName, int chapter){
        mProject = project;
        mFileName = fileName;
        mChapter = chapter;
        Object src;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            src = getSourceAudioFile();
        } else {
            src = getSourceAudioFileKitkat();
        }
        //Uri sourceAudio = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ATranslationRecorder%2FSource%2Fen%2Fulb%2Fgen%2F01%2Fen_ulb_gen_01-01.wav");
        if(src == null || (src instanceof DocumentFile && !((DocumentFile)src).exists()) || (src instanceof File && !((File)src).exists())){
            showNoSource(true);
            return;
        }
        showNoSource(false);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mSrcPlayer != null && fromUser) {
                    mSrcPlayer.seekTo(progress);
                    final String time = String.format("%02d:%02d:%02d", progress / 3600000, (progress / 60000) % 60, (progress / 1000) % 60);
                    mCtx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSrcTimeElapsed.setText(time);
                            mSrcTimeElapsed.invalidate();
                        }
                    });
                }
            }
        });
        try {
            mSrcPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switchPlayPauseBtn(false);
                    mSeekBar.setProgress(mSeekBar.getMax());
                    int duration = mSeekBar.getMax();
                    final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
                    mCtx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSrcTimeDuration.setText(time);
                            mSrcTimeDuration.invalidate();
                        }
                    });
                    if(mSrcPlayer.isPlaying()) {
                        mSrcPlayer.seekTo(0);
                    }
                }
            });
            if(src != null && src instanceof DocumentFile) {
                mSrcPlayer.setDataSource(mCtx, ((DocumentFile) src).getUri());
            } else if (src != null && src instanceof File){
                mSrcPlayer.setDataSource(((File) src).getAbsolutePath());
            }
            mSrcPlayer.prepare();
            int duration = mSrcPlayer.getDuration();
            mSeekBar.setMax(duration);
            final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
            mCtx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSrcTimeDuration.setText(time);
                    mSrcTimeDuration.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup(){
        synchronized (mSrcPlayer){
            if(!mPlayerReleased && mSrcPlayer.isPlaying()){
                mSrcPlayer.pause();
            }
            mSrcPlayer.release();
            mPlayerReleased = true;
        }
    }

    public void playSource() {
        switchPlayPauseBtn(true);
        if (mSrcPlayer != null) {
            mSrcPlayer.start();
            mHandler = new Handler();
            mCtx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSeekBar.setProgress(0);
                    System.out.println(mSeekBar.getProgress());
                    mSeekBar.invalidate();
                    Runnable loop = new Runnable() {
                        @Override
                        public void run() {
                            if (mSrcPlayer != null && !mPlayerReleased) {
                                synchronized (mSrcPlayer) {
                                    int mCurrentPosition = mSrcPlayer.getCurrentPosition();
                                    if (mCurrentPosition > mSeekBar.getProgress()) {
                                        mSeekBar.setProgress(mCurrentPosition);
                                        final String time = String.format("%02d:%02d:%02d", mCurrentPosition / 3600000, (mCurrentPosition / 60000) % 60, (mCurrentPosition / 1000) % 60);
                                        mSrcTimeElapsed.setText(time);
                                        mSrcTimeElapsed.invalidate();
                                    }
                                }
                            }
                            mHandler.postDelayed(this, 200);
                        }
                    };
                    loop.run();
                }
            });
        }
    }

    public void pauseSource(){
        switchPlayPauseBtn(false);
        if(mSrcPlayer != null && !mPlayerReleased && mSrcPlayer.isPlaying()){
            mSrcPlayer.pause();
        }
    }

    public void reset(Project project, String fileName, int chapter){
        cleanup();
        mSrcPlayer = null;
        mSrcPlayer = new MediaPlayer();
        mPlayerReleased = false;
        mSeekBar.setProgress(0);
        switchPlayPauseBtn(false);
        mCtx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSrcTimeElapsed.setText("00:00:00");
                mSrcTimeElapsed.invalidate();
            }
        });
        initSrcAudio(project, fileName, chapter);
    }

    public void setEnabled(boolean enable) {
        mSeekBar.setEnabled(enable);
        mBtnSrcPlay.setEnabled(enable);
        if (enable) {
            mSrcTimeElapsed.setTextColor(getResources().getColor(R.color.text_light_disabled));
            mSrcTimeDuration.setTextColor(getResources().getColor(R.color.text_light_disabled));
        } else {
            mSrcTimeElapsed.setTextColor(getResources().getColor(R.color.text_light));
            mSrcTimeDuration.setTextColor(getResources().getColor(R.color.text_light));
        }
    }

    public void showNoSource(boolean noSource) {
        if (noSource) {
            mSeekBar.setVisibility(View.GONE);
            mSrcTimeElapsed.setVisibility(View.GONE);
            mSrcTimeDuration.setVisibility(View.GONE);
            mNoSourceMsg.setVisibility(View.VISIBLE);
            setEnabled(false);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
            mSrcTimeElapsed.setVisibility(View.VISIBLE);
            mSrcTimeDuration.setVisibility(View.VISIBLE);
            mNoSourceMsg.setVisibility(View.GONE);
            setEnabled(true);
        }
    }
}
