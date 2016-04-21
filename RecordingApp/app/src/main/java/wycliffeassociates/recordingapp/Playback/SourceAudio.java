package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognito.Record;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 4/13/2016.
 */
public class SourceAudio {

    private Activity mCtx;
    private SeekBar mSeekBar;
    private TextView mSrcTimeElapsed;
    private TextView mSrcTimeDuration;
    private MediaPlayer mSrcPlayer;
    private ImageButton mBtnSrcPlay;
    private TextView mNoSourceMsg;
    private Handler mHandler;
    private volatile boolean mPlayerReleased = false;

    public SourceAudio(Activity ctx){
        mCtx = ctx;
        mSrcTimeElapsed = (TextView) ctx.findViewById(R.id.srcProgress);
        mSrcTimeDuration = (TextView) ctx.findViewById(R.id.srcDuration);
        mSeekBar = (SeekBar) ctx.findViewById(R.id.seekBar);
        mBtnSrcPlay = (ImageButton) ctx.findViewById(R.id.btnPlaySource);
        mNoSourceMsg = (TextView) ctx.findViewById(R.id.noSourceMsg);
        mSrcPlayer = new MediaPlayer();
    }

    private DocumentFile getSourceAudioDirectory(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
        String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
        String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
        String book = sp.getString(Settings.KEY_PREF_BOOK, "");
        String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String srcLoc = sp.getString(Settings.KEY_PREF_SRC_LOC, null);
        if(srcLoc == null){
            return null;
        }
        Uri uri = Uri.parse(srcLoc);
        if(uri != null){
            DocumentFile df = DocumentFile.fromTreeUri(mCtx, uri);
            if(df != null) {
                DocumentFile langDf = df.findFile(lang);
                if(langDf != null) {
                    DocumentFile srcDf = langDf.findFile(src);
                    if(srcDf != null) {
                        DocumentFile bookDf = srcDf.findFile(book);
                        if(bookDf != null) {
                            DocumentFile chapDf = bookDf.findFile(chap);
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
        String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
        String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
        String book = sp.getString(Settings.KEY_PREF_BOOK, "");
        String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
        if (sp.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") != 0) {
            chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_VERSE, "1")));
        }
        String filename = lang+"_"+src+"_"+book+"_"+chap+"-"+chunk;

        String[] filetypes = {".wav", ".mp3", ".mp4", ".m4a", ".aac", ".flac", ".3gp", ".ogg"};
        for(String type : filetypes){
            DocumentFile temp = directory.findFile(filename + type);
            if(temp != null) {
                if (temp.exists()) {
                    return directory.findFile(filename + type);
                }
            }
        }
        return null;
    }

    private File getSourceAudioFileKitkat(){
        File file = getSourceAudioFileDirectoryKitkat();
        if(file == null || !file.exists()){
            return null;
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
            String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
            String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
            String book = sp.getString(Settings.KEY_PREF_BOOK, "");
            String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
            String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
            if (sp.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") != 0) {
                chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_VERSE, "1")));
            }
            String filename = lang+"_"+src+"_"+book+"_"+chap+"-"+chunk;
            String[] filetypes = {".wav", ".mp3", ".mp4", ".m4a", ".aac", ".flac", ".3gp", ".ogg"};
            for(String type : filetypes) {
                File temp = new File(file, filename + type);
                if (temp != null) {
                    if (temp.exists()) {
                        return temp;
                    }
                }
            }
        }
        return null;
    }

    private File getSourceAudioFileDirectoryKitkat(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
        String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
        String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
        String book = sp.getString(Settings.KEY_PREF_BOOK, "");
        String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
        String filename = lang+"_"+src+"_"+book+"_"+chap+"-"+chunk;
        String path = sp.getString(Settings.KEY_PREF_SRC_LOC, "");
        File file = new File(path, lang + "/" + src + "/" + book + "/" + chap);
        return file;
    }

    public void initSrcAudio(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        int sdk = pref.getInt(Settings.KEY_SDK_LEVEL, 21);
        Object src;
        if(sdk >= 21) {
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
                    mCtx.findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
                    mCtx.findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
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
        mCtx.findViewById(R.id.btnPlaySource).setVisibility(View.INVISIBLE);
        mCtx.findViewById(R.id.btnPauseSource).setVisibility(View.VISIBLE);
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
        mCtx.findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
        mCtx.findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
        if(mSrcPlayer != null && !mPlayerReleased && mSrcPlayer.isPlaying()){
            mSrcPlayer.pause();
        }
    }

    public void reset(){
        cleanup();
        mSrcPlayer = null;
        mSrcPlayer = new MediaPlayer();
        mPlayerReleased = false;
        mSeekBar.setProgress(0);
        mCtx.findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
        mCtx.findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
        mCtx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSrcTimeElapsed.setText("00:00:00");
                mSrcTimeElapsed.invalidate();
            }
        });
        initSrcAudio();
    }

    public void setEnabled(boolean enable) {
        mSeekBar.setEnabled(enable);
        mBtnSrcPlay.setEnabled(enable);
        if (enable) {
            mSrcTimeElapsed.setTextColor(mCtx.getResources().getColor(R.color.text_light_disabled));
            mSrcTimeDuration.setTextColor(mCtx.getResources().getColor(R.color.text_light_disabled));
        } else {
            mSrcTimeElapsed.setTextColor(mCtx.getResources().getColor(R.color.text_light));
            mSrcTimeDuration.setTextColor(mCtx.getResources().getColor(R.color.text_light));
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
