package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.VolumeBar;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.SettingsPage.Book;
import wycliffeassociates.recordingapp.SettingsPage.ParseJSON;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

public class RecordingScreen extends Activity {
    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";

    private final Context context = this;
    private TextView filenameView;
    private WaveformView mainCanvas;
    private VolumeBar mVolumeBar;
    private MinimapView minimap;
    private UIDataManager manager;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean isSaved = false;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;
    private boolean hasStartedRecording = false;
    private boolean mDeleteTempFile = false;
    private volatile HashMap<String, Book> mBooks;
    private volatile int mNumChunks;
    private volatile int mChunk;
    private volatile int lastNumber;
    private ArrayList<Integer> mChunks;
    private NumberPicker numPicker;
    private MediaPlayer mSrcPlayer;
    private Handler mHandler;
    private SeekBar mSeekBar;
    private TextView mSrcTimeElapsed;
    private TextView mSrcTimeDuration;
    private volatile boolean mPlayerReleased = false;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        suggestedFilename = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PREF_FILENAME, "en_mat_1-1_1");
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        initViews();
        setButtonHandlers();
        enableButtons();

        mSrcPlayer = new MediaPlayer();
        manager = new UIDataManager(mainCanvas, minimap, mVolumeBar, null, null, this, UIDataManager.RECORDING_MODE, true);
        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(true);

        hasStartedRecording = false;
        mDeleteTempFile = false;
        initChunkPicker();
        initSrcAudio();
    }

    private void initViews(){
        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        mVolumeBar = (VolumeBar) findViewById((R.id.volumeBar1));
        mSrcTimeElapsed = (TextView) findViewById(R.id.srcProgress);
        mSrcTimeDuration = (TextView) findViewById(R.id.srcDuration);
        filenameView = (TextView) findViewById(R.id.filenameView);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mainCanvas.disableGestures();
        filenameView.setText(suggestedFilename);
    }

    private void initChunkPicker(){
        Thread getNumChunks = new Thread(new Runnable() {
            @Override
            public void run() {
                ParseJSON parse = new ParseJSON(context);
                mBooks = parse.getBooksMap();

                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                Book book = mBooks.get(pref.getString(Settings.KEY_PREF_BOOK, "gen"));
                int chapter = Integer.parseInt(pref.getString(Settings.KEY_PREF_CHAPTER, "1"));
                String src = pref.getString(Settings.KEY_PREF_SOURCE, "udb");
                final String verseOrChunk = pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk");
                if(verseOrChunk.compareTo("chunk") == 0) {
                    mChunks = parse.getChunks(book.getSlug(), src).get(chapter - 1);
                } else {
                    mChunks = parse.getVerses(book.getSlug(), src).get(chapter - 1);
                }
                final String[] values = new String[mChunks.size()];
                for(int i = 0; i < mChunks.size(); i++){
                    values[i] = String.valueOf(mChunks.get(i));
                }
                mNumChunks = mChunks.size();
                numPicker = (NumberPicker)findViewById(R.id.numberPicker);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(values != null && values.length > 0) {
                            numPicker.setDisplayedValues(values);
                            numPicker.setMinValue(1);
                            numPicker.setMaxValue(mNumChunks);
                            if (verseOrChunk.compareTo("chunk") == 0) {
                                // Chunk
                                int chunk = Integer.parseInt(pref.getString(Settings.KEY_PREF_CHUNK, "1"));
                                mChunk = getChunkIndex(mChunks, chunk);
                            } else {
                                // Verse
                                int verse = Integer.parseInt(pref.getString(Settings.KEY_PREF_VERSE, "1"));
                                mChunk = getChunkIndex(mChunks, verse);
                            }
                            Settings.updateFilename(context);
                            suggestedFilename = pref.getString(Settings.KEY_PREF_FILENAME, String.valueOf(R.string.pref_default_filename));
                            filenameView.setText(suggestedFilename);
                            numPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                                @Override
                                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                                    cleanupPlayer();
                                    setChunk(newVal);
                                    mSrcPlayer = null;
                                    mSrcPlayer = new MediaPlayer();
                                    mPlayerReleased = false;
                                    mSeekBar.setProgress(0);
                                    findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
                                    findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSrcTimeElapsed.setText("00:00:00");
                                            mSrcTimeElapsed.invalidate();
                                        }
                                    });
                                    initSrcAudio();
                                }
                            });
                            numPicker.setValue(mChunk + 1);
                        } else {
                            Logger.e(this.toString(), "values was null or of zero length");
                        }
                    }
                });
            }
        });
        getNumChunks.start();
    }

    private DocumentFile getSourceAudioDirectory(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
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
            DocumentFile df = DocumentFile.fromTreeUri(this, uri);
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
        String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
        String book = sp.getString(Settings.KEY_PREF_BOOK, "");
        String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
            String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
            String book = sp.getString(Settings.KEY_PREF_BOOK, "");
            String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
            String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = sp.getString(Settings.KEY_PREF_LANG_SRC, "");
        String src = sp.getString(Settings.KEY_PREF_SOURCE, "");
        String book = sp.getString(Settings.KEY_PREF_BOOK, "");
        String chap = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(sp.getString(Settings.KEY_PREF_CHUNK, "1")));
        String filename = lang+"_"+src+"_"+book+"_"+chap+"-"+chunk;
        String path = sp.getString(Settings.KEY_PREF_SRC_LOC, "");
        File file = new File(path, lang + "/" + src +"/" + book + "/" + chap);
        return file;
    }

    private void initSrcAudio(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int sdk = pref.getInt(Settings.KEY_SDK_LEVEL, 21);
        Object src;
        if(sdk >= 21) {
            src = getSourceAudioFile();
        } else {
            src = getSourceAudioFileKitkat();
        }
        //Uri sourceAudio = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ATranslationRecorder%2FSource%2Fen%2Fulb%2Fgen%2F01%2Fen_ulb_gen_01-01.wav");
        if(src == null || (src instanceof DocumentFile && !((DocumentFile)src).exists()) || (src instanceof File && !((File)src).exists())){
            findViewById(R.id.srcAudioPlayer).setVisibility(View.INVISIBLE);
            return;
        }
        findViewById(R.id.srcAudioPlayer).setVisibility(View.VISIBLE);
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
                    runOnUiThread(new Runnable() {
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
                    findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
                    mSeekBar.setProgress(mSeekBar.getMax());
                    int duration = mSeekBar.getMax();
                    final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSrcTimeDuration.setText(time);
                            mSrcTimeDuration.invalidate();
                        }
                    });
                }
            });
            if(src != null && src instanceof DocumentFile) {
                mSrcPlayer.setDataSource(this, ((DocumentFile) src).getUri());
            } else if (src != null && src instanceof File){
                mSrcPlayer.setDataSource(((File) src).getAbsolutePath());
            }
            mSrcPlayer.prepare();
            int duration = mSrcPlayer.getDuration();
            mSeekBar.setMax(duration);
            final String time = String.format("%02d:%02d:%02d", duration / 3600000, (duration / 60000) % 60, (duration / 1000) % 60);
            runOnUiThread(new Runnable() {
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

    @Override
    public void onPause(){
        super.onPause();
        if(isRecording) {
            isRecording = false;
            stopService(new Intent(this, WavRecorder.class));
            long start = System.currentTimeMillis();
            Logger.w(this.toString(), "Stopping recording");
            RecordingQueues.stopQueues(this);
        } else if(isPausedRecording){
            RecordingQueues.stopQueues(this);
        } else if(!hasStartedRecording){
            stopService(new Intent(this, WavRecorder.class));
            RecordingQueues.stopVolumeTest();
        }
        if(mDeleteTempFile){
            mDeleteTempFile = false;
            File file = new File(recordedFilename);
            if(file.exists()) {
                boolean result = file.delete();
                Logger.w(this.toString(), "deleted the temporary file before exiting: " + result);
            } else {
                Logger.w(this.toString(), "temp file did not exist?");
            }
        }
        if(mSrcPlayer != null && !mPlayerReleased && mSrcPlayer.isPlaying()){
            mSrcPlayer.pause();
        }
    }

    @Override
    public void onDestroy(){
        cleanupPlayer();
        super.onDestroy();
    }

    private void cleanupPlayer(){
        synchronized (mSrcPlayer){
            if(!mPlayerReleased && mSrcPlayer.isPlaying()){
                mSrcPlayer.pause();
            }
            mSrcPlayer.release();
            mPlayerReleased = true;
        }
    }

    private int getChunkIndex(ArrayList<Integer> chunks, int chunk) {
        for (int i = 0; i < chunks.size(); i++) {
            if (chunks.get(i) == chunk) {
                return i;
            }
        }
        return 1;
    }

    private void pauseRecording() {
        isPausedRecording = true;
        manager.pauseTimer();
        isRecording = false;
        int toShow[] = {R.id.btnRecording, R.id.btnStop};
        int toHide[] = {R.id.btnPauseRecording};
        manager.swapViews(toShow, toHide);
        stopService(new Intent(this, WavRecorder.class));
        RecordingQueues.pauseQueues();
        Logger.w(this.toString(), "Pausing recording");
    }

    private void startRecording() {
        cleanupPlayer();
        findViewById(R.id.srcAudioPlayer).setVisibility(View.INVISIBLE);
        findViewById(R.id.numberPicker).setVisibility(View.INVISIBLE);
        hasStartedRecording = true;
        stopService(new Intent(this, WavRecorder.class));
        int toShow[] = {R.id.btnPauseRecording};
        int toHide[] = {R.id.btnRecording, R.id.btnStop};
        manager.swapViews(toShow, toHide);
        isRecording = true;
        manager.setIsRecording(true);
        Logger.w(this.toString(), "Starting recording");

        if (!isPausedRecording) {
            RecordingQueues.stopVolumeTest();
            manager.startTimer();
            isSaved = false;
            RecordingQueues.clearQueues();
            Intent intent = new Intent(this, WavFileWriter.class);
            intent.putExtra("audioFileName", getFilename());
            intent.putExtra("screenWidth", AudioInfo.SCREEN_WIDTH);
            startService(new Intent(this, WavRecorder.class));
            startService(intent);
            manager.listenForRecording(false);
        } else {
            manager.resumeTimer();
            isPausedRecording = false;
            startService(new Intent(this, WavRecorder.class));
        }
    }

    private void stopRecording() {
        if(isPausedRecording || isRecording) {
            //Stop recording, load the recorded file, and draw
            stopService(new Intent(this, WavRecorder.class));
            long start = System.currentTimeMillis();
            Logger.w(this.toString(), "Stopping recording");
            RecordingQueues.stopQueues(this);
            System.out.println("took " + (System.currentTimeMillis() - start) + " to finish writing");
            Intent intent = new Intent(this, PlaybackScreen.class);
            intent.putExtra("recordedFilename", recordedFilename);
            isRecording = false;
            isPausedRecording = false;
            startActivity(intent);
        }
    }

    public void deleteTempFile(){
        mDeleteTempFile = true;
    }

    @Override
    public void onBackPressed() {
        if (!isSaved && hasStartedRecording) {
            FragmentManager fm = getFragmentManager();
            FragmentExitDialog d = new FragmentExitDialog();
            d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            d.show(fm, "Exit Dialog");
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Retrieves the filename of the recorded audio file.
     * If the AudioRecorder folder does not exist, it is created.
     *
     * @return the absolute filepath to the recorded .wav file
     */
    public String getFilename() {
        String root = pref.getString("root_directory", Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString());
        String lang = pref.getString(Settings.KEY_PREF_LANG, "en");
        String src = pref.getString(Settings.KEY_PREF_SOURCE, "udb");
        String book = pref.getString(Settings.KEY_PREF_BOOK, "mat");
        String chap = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_CHUNK, "1")));
        if(pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") != 0){
            chunk = chap = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_VERSE, "1")));
        }
        String fullpath = root + "/" + lang + "/" + src + "/" + book + "/" + chap + "/";

        File filepath = new File(fullpath);

        if (!filepath.exists()) {
            filepath.mkdirs();
        }

        if (recordedFilename != null)
            return (fullpath + recordedFilename);
        else {
            recordedFilename = (fullpath + pref.getString(Settings.KEY_PREF_FILENAME, "en_ulb_mat_01-01") + AUDIO_RECORDER_FILE_EXT_WAV);
            System.out.println("filename is " + recordedFilename);
            return recordedFilename;
        }
    }

    /**
     * Sets the chunk by indexing the chunk list with the provided index
     * @param idx
     */
    private void setChunk(int idx){
        if(mChunks != null) {
            //Get the list of chunks by first getting the book and chapter from the preference
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            int chunk = mChunks.get(idx-1);
            String chunkOrVerse = pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk");
            if(chunkOrVerse.compareTo("chunk") == 0) {
                pref.edit().putString(Settings.KEY_PREF_CHUNK, String.valueOf(chunk)).commit();
            } else {
                pref.edit().putString(Settings.KEY_PREF_VERSE, String.valueOf(chunk)).commit();
            }

            pref.edit().putString(Settings.KEY_PREF_TAKE, "1").commit();
            Settings.updateFilename(context);
            suggestedFilename = pref.getString(Settings.KEY_PREF_FILENAME, String.valueOf(R.string.pref_default_filename));
            filenameView.setText(suggestedFilename);
            numPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        }
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnPlaySource).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseSource).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnRecording, true);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPauseRecording, true);
        enableButton(R.id.btnPlaySource, true);
        enableButton(R.id.btnPauseSource, true);
    }

    public void playSource() {
        findViewById(R.id.btnPlaySource).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnPauseSource).setVisibility(View.VISIBLE);
        if (mSrcPlayer != null) {
            mSrcPlayer.start();
            mHandler = new Handler();
            mSeekBar.setProgress(0);
            runOnUiThread(new Runnable() {
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
            });
        }
    }

    public void pauseSource(){
        findViewById(R.id.btnPlaySource).setVisibility(View.VISIBLE);
        findViewById(R.id.btnPauseSource).setVisibility(View.INVISIBLE);
        if(mSrcPlayer != null && mSrcPlayer.isPlaying()){
            mSrcPlayer.pause();
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRecording: {
                startRecording();
                break;
            }
            case R.id.btnStop: {
                stopRecording();
                break;
            }
            case R.id.btnPauseRecording: {
                pauseRecording();
                break;
            }
            case R.id.btnPlaySource: {
                playSource();
                break;
            }
            case R.id.btnPauseSource: {
                pauseSource();
                break;
            }
        }
        }
    };
}
