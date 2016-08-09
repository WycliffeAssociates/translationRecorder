package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.provider.DocumentFile;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.wycliffeassociates.io.ArchiveOfHolding;
import com.wycliffeassociates.io.ArchiveOfHoldingEntry;
import com.wycliffeassociates.io.LanguageLevel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Recording.WavFile;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.project.adapters.SourceTextAdapter;
import wycliffeassociates.recordingapp.widgets.AudioPlayer;

/**
 * Created by sarabiaj on 4/13/2016.
 */
public class SourceAudio extends LinearLayout {

    private Activity mCtx;
    private SeekBar mSeekBar;
    private TextView mSrcTimeElapsed;
    private TextView mSrcTimeDuration;
    private AudioPlayer mSrcPlayer;
    private ImageButton mBtnSrcPlay;
    private TextView mNoSourceMsg;
    private Handler mHandler;
    private volatile boolean mPlayerReleased = false;
    private Project mProject;
    private String mFileName;
    private int mChapter;
    private File mTemp;
    private static final String[] filetypes = {"wav", "mp3", "mp4", "m4a", "aac", "flac", "3gp", "ogg"};

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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(mTemp != null && mTemp.exists()) {
            //mTemp.delete();
        }
    }

    private void init(){
        inflate(getContext(), R.layout.source_audio, this);
        mSrcTimeElapsed = (TextView) findViewById(R.id.timeProgress);
        mSrcTimeDuration = (TextView) findViewById(R.id.timeDuration);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mBtnSrcPlay = (ImageButton) findViewById(R.id.playButton);
        mNoSourceMsg = (TextView) findViewById(R.id.noSourceMsg);
        mSrcPlayer = new AudioPlayer(mSrcTimeElapsed, mSrcTimeDuration, mBtnSrcPlay, mSeekBar);
        mCtx = (Activity) getContext();

        mBtnSrcPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBtnSrcPlay.isActivated()){
                    pauseSource();
                } else {
                    playSource();
                }
            }
        });
    }

    private Uri getUriFromString(String sourceLanguage, String sourceLocation){
        if(sourceLocation == null || sourceLanguage == null || sourceLocation.compareTo("") == 0 || sourceLanguage.compareTo("") == 0) {
            return null;
        }
        Uri uri = Uri.parse(sourceLocation);
        if(uri != null){
            return uri;
        }
        return null;
    }

    private String getExtensionIfValid(String filename){
        String extension = null;
        for(int i = 0; i < filetypes.length; i++){
            if(filename.contains(filetypes[i])){
                extension = filetypes[i];
                break;
            }
        }
        return extension;
    }

    private boolean getAudioFromUri(String sourceLanguage, Uri uri) throws IOException, FileNotFoundException{
        InputStream is = mCtx.getContentResolver().openInputStream(uri);
        LanguageLevel ll = new LanguageLevel();
        ArchiveOfHolding aoh = new ArchiveOfHolding(is, ll);
        String importantSection = mFileName.substring(mFileName.lastIndexOf("_b"), mFileName.length());
        ArchiveOfHoldingEntry entry = aoh.getEntry(importantSection, sourceLanguage,
                mProject.getSource(), mProject.getSlug(), FileNameExtractor.chapterIntToString(mProject, mChapter));
        if(entry == null){
            return false;
        }
        String extension = getExtensionIfValid(entry.getName());
        InputStream file = entry.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(file);
        mTemp = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/temp" + extension);
        FileOutputStream fos = new FileOutputStream(mTemp);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bis.close();
        is.close();
        file.close();
        bos.flush();
        bos.close();
        fos.close();

        return true;
    }

    private void loadAudioFile() throws IOException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);

        String projectSourceLocation = mProject.getSourceAudioPath();
        String projectSourceLanguage = mProject.getSourceLanguage();
        String globalSourceLocation = sp.getString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, null);
        String globalSourceLanguage = sp.getString(Settings.KEY_PREF_GLOBAL_LANG_SRC, null);

        Uri projectUri = getUriFromString(projectSourceLanguage, projectSourceLocation);
        Uri globalUri = getUriFromString(globalSourceLanguage, globalSourceLocation);

        boolean gotFile = false;
        if (projectUri != null) {
            gotFile = getAudioFromUri(projectSourceLanguage, projectUri);
        } if(!gotFile && globalUri != null){
            getAudioFromUri(globalSourceLanguage, globalUri);
        }
    }

    public void initSrcAudio(Project project, String fileName, int chapter){
        if(mTemp != null && mTemp.exists()){
            mTemp.delete();
            mTemp = null;
        }
        mProject = project;
        mFileName = fileName;
        mChapter = chapter;
        try {
            loadAudioFile();
        } catch (IOException e) {
            mTemp = null;
        }
        if(mTemp == null || !mTemp.exists()){
            showNoSource(true);
            return;
        }
        showNoSource(false);
        mSrcPlayer.loadFile(mTemp);
    }


    public void playSource() {
        mSrcPlayer.play();
    }

    public void pauseSource(){
        mSrcPlayer.pause();
    }

    public void reset(Project project, String fileName, int chapter){
        mSrcPlayer.reset();
        mSeekBar.setProgress(0);
        mSrcTimeElapsed.setText("00:00:00");
        mSrcTimeElapsed.invalidate();
        initSrcAudio(project, fileName, chapter);
    }

    public void cleanup(){
        mSrcPlayer.cleanup();
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
