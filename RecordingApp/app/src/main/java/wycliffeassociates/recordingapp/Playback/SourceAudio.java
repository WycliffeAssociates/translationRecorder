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
    private ImageButton mBtnSrcPause;
    private TextView mNoSourceMsg;
    private Handler mHandler;
    private volatile boolean mPlayerReleased = false;
    private Project mProject;
    private String mFileName;
    private int mChapter;
    private File mTemp;

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
            mTemp.delete();
        }
    }

    private void init(){
        inflate(getContext(), R.layout.source_audio, this);
        mSrcTimeElapsed = (TextView) findViewById(R.id.timeProgress);
        mSrcTimeDuration = (TextView) findViewById(R.id.timeDuration);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mBtnSrcPlay = (ImageButton) findViewById(R.id.playButton);
        mBtnSrcPause = (ImageButton) findViewById(R.id.pauseButton);
        mNoSourceMsg = (TextView) findViewById(R.id.noSourceMsg);
        mSrcPlayer = new AudioPlayer(mSrcTimeElapsed, mSrcTimeDuration, mBtnSrcPlay, mBtnSrcPause, mSeekBar);
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

    private Uri getSourceAudioDirectory(){
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
            return uri;
        }
        return null;
    }

    private boolean filesMatch(FileNameExtractor one, FileNameExtractor two){
        if(one == null || two == null){
            return false;
        }
        if(one.getBookNumber() == two.getBookNumber()) {
            if (one.getChapter() == two.getChapter()) {
                if(one.getStartVerse() == two.getStartVerse()){
                    if(one.getEndVerse() == two.getEndVerse()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private File getSourceAudioFile() {
        Uri uri = getSourceAudioDirectory();
        if (uri == null) {
            return null;
        }
        String[] filetypes = {"wav", "mp3", "mp4", "m4a", "aac", "flac", "3gp", "ogg"};

        try {
            InputStream is = mCtx.getContentResolver().openInputStream(uri);
            XarFile xar = new XarFile(is, "cmn_ulb_b55_2ti_c04_v19_t01.wav");
            XarEntry entry = xar.getEntry();
            InputStream file = xar.getInputStream(entry);
            System.out.println(System.currentTimeMillis());

            mTemp = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/temp.wav");
            FileOutputStream fos = new FileOutputStream(mTemp);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = file.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            bos.close();
            fos.close();

//            ZipInputStream zis = new ZipInputStream(is);
//            ZipEntry ze;
//            String extractedName;
//            do {
//                ze = zis.getNextEntry();
//                extractedName = ze.getName();
//                try {
//                    extractedName = extractedName.substring(extractedName.lastIndexOf("/"), extractedName.lastIndexOf("."));
//                } catch (StringIndexOutOfBoundsException e) {
//                    extractedName = "";
//                }
//            } while (extractedName.compareTo(mFileName) != 0 && ze != null);
//
//            long size = ze.getSize();
//            bos.write(zis.read(new byte[(int) size]));
//            zis.closeEntry();
//            bos.close();
//            fos.close();
//            zis.close();
//            //bis.close();
//            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mTemp;
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
        if(mTemp != null && mTemp.exists()){
            mTemp.delete();
        }
        mProject = project;
        mFileName = fileName;
        mChapter = chapter;
        File src = getSourceAudioFile();
        if(src == null || !src.exists()){
            showNoSource(true);
            return;
        }
        showNoSource(false);
        mSrcPlayer.loadFile(src);
    }


    public void playSource() {
        mSrcPlayer.play();
    }

    public void pauseSource(){
        mSrcPlayer.pause();
    }

    public void reset(Project project, String fileName, int chapter){
        mSrcPlayer.cleanup();
        mSeekBar.setProgress(0);
        switchPlayPauseBtn(false);
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
