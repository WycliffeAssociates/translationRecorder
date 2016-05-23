package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
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
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.VolumeBar;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.Playback.SourceAudio;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.SettingsPage.Book;
import wycliffeassociates.recordingapp.SettingsPage.ParseJSON;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

public class RecordingScreen extends Activity implements InsertTaskFragment.Insert{
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
    private String mFilename = null;
    private boolean isSaved = false;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;
    private boolean hasStartedRecording = false;
    private boolean mDeleteTempFile = false;
    private volatile HashMap<String, Book> mBooks;
    private ArrayList<Pair<Integer,Integer>> mChunks;
    private volatile int mNumChunks;
    private volatile int mChunk;
    private volatile String mSlug;
    private volatile String mBook;
    private volatile String mSource;
    private volatile String mLang;
    private int mChapter;

    private TextView mSourceView;
    private TextView mLanguageView;
    private TextView mBookView;
    private volatile boolean mBookInfoLoaded = false;
    private volatile int lastNumber;
    private SourceAudio mSrcPlayer;
    private UnitPicker mChunkPicker;
    private UnitPicker mChapterPicker;
    private SharedPreferences pref;
    private FileNameExtractor mFileNameExtractor;
    private volatile ParseJSON mParsedJson;
    private int mInsertLoc = 0;
    private boolean mInsertMode = false;
    private InsertTaskFragment mInsertTaskFragment;
    private String TAG_INSERT_TASK_FRAGMENT = "insert_task_fragment";
    private String STATE_INSERTING = "state_inserting";
    private boolean mInserting = false;
    private ProgressDialog mPd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        initBookInfo();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mFilename = pref.getString(Settings.KEY_PREF_FILENAME, "en_udb_b01_gen_c01_v01");
        mInsertMode = getIntent().getBooleanExtra("insert_mode", false);
        if(mInsertMode){
            initializeInsert(getIntent().getStringExtra("old_name"), getIntent().getIntExtra("insert_location", 0));
        }

        initTaskFragment(savedInstanceState);
        initFileName();
        initViews();
        setButtonHandlers();
        enableButtons();

        manager = new UIDataManager(mainCanvas, minimap, mVolumeBar, null, null, this, UIDataManager.RECORDING_MODE, true);
        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(true);

        hasStartedRecording = false;
        mDeleteTempFile = false;
        if(!mInsertMode) {

        } else {
            mChunkPicker.displayIncrementDecrement(false);
            mChapterPicker.displayIncrementDecrement(false);
        }
        mSrcPlayer.initSrcAudio();
    }

    private void initFileName(){
        mFileNameExtractor = new FileNameExtractor(mFilename);
        mLang = mFileNameExtractor.getLang();
        mSource = mFileNameExtractor.getSource();
        mSlug = mFileNameExtractor.getBook();
        if(mBooks != null && mSlug != null) {
            mBook = mBooks.get(mSlug).getName();
        }
        mChapter = mFileNameExtractor.getChapter();
        mChunk = mFileNameExtractor.getStartVerse();
    }

    private void initTaskFragment(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        mInsertTaskFragment = (InsertTaskFragment)fm.findFragmentByTag(TAG_INSERT_TASK_FRAGMENT);
        if(mInsertTaskFragment == null){
            mInsertTaskFragment = new InsertTaskFragment();
            fm.beginTransaction().add(mInsertTaskFragment, TAG_INSERT_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        if(savedInstanceState != null){
            mInserting = savedInstanceState.getBoolean(STATE_INSERTING, false);
            if(mInserting){
                displayProgressDialog();
            }
        }
    }

    private void initViews(){
        mSrcPlayer = (SourceAudio) findViewById(R.id.srcAudioPlayer);
        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        mVolumeBar = (VolumeBar) findViewById((R.id.volumeBar1));

        filenameView = (TextView) findViewById(R.id.filenameView);
        mBookView = (TextView) findViewById(R.id.file_book);
        mSourceView = (TextView) findViewById(R.id.file_project);
        mLanguageView = (TextView) findViewById(R.id.file_language);

        mainCanvas.disableGestures();
        filenameView.setText(mFilename);

        mChunkPicker = (UnitPicker) findViewById(R.id.unit_picker);
        mChapterPicker= (UnitPicker) findViewById(R.id.chapter_picker);

        if(pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") == 0) {
            ((TextView) findViewById(R.id.file_unit_label)).setText("Chunk");
        } else {
            ((TextView) findViewById(R.id.file_unit_label)).setText("Verse");
        }
    }

    private void initBookInfo(){
        Thread loadJson = new Thread(new Runnable() {
            @Override
            public void run() {
                mParsedJson = new ParseJSON(context);
                mBooks = mParsedJson.getBooksMap();
                mBookInfoLoaded = true;
                initChunkPicker();
                initChapterPicker();
                initBookText();
            }
        });
        loadJson.start();
    }

    private void initBookText(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pref.getString(Settings.KEY_PREF_PROJECT, "obs").compareTo("obs") == 0) {
                    mBook = "OBS";
                    mSource = "";
                }
                mBookView.setText(mBook);
                mLanguageView.setText(mLang.toUpperCase());
                mSourceView.setText(mSource.toUpperCase());
            }
        });
    }

    private void initChunkPicker(){
        initFileName();
        final String verseOrChunk = pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk");
        if(pref.getString(Settings.KEY_PREF_PROJECT, "obs").compareTo("obs") != 0) {
            Book book = mBooks.get(mSlug);
            if (verseOrChunk.compareTo("chunk") == 0) {
                mChunks = mParsedJson.getChunks(book.getSlug(), mSource).get(mChapter - 1);
            } else {
                mChunks = mParsedJson.getVerses(book.getSlug(), mSource).get(mChapter - 1);
            }
        } else {
            int[] obsChunks = getResources().getIntArray(R.array.obs_chunks);
            mChunks = new ArrayList<>();
            for(int i = 0; i < obsChunks[mChapter-1]; i++){
                mChunks.add(new Pair<>(i+1, i+1));
            }
        }
        final String[] values = new String[mChunks.size()];
        for(int i = 0; i < mChunks.size(); i++){
            values[i] = String.valueOf(mChunks.get(i).first);
        }
        int mNumChunks = mChunks.size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (values != null && values.length > 0) {
                    mChunkPicker.setDisplayedValues(values);
                    mChunkPicker.setCurrent(getChunkIndex(mChunks, mChunk));
                    //reinitialize all of the filenames
                    initFileName();
                    mChunkPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                            setChunk(newVal + 1);
                            mSrcPlayer.reset();
                        }
                    });
                } else {
                    Logger.e(this.toString(), "values was null or of zero length");
                }
            }
        });
    }

    private void initChapterPicker(){
        int numChapters = getResources().getIntArray(R.array.obs_chunks).length;
        if(pref.getString(Settings.KEY_PREF_PROJECT, "obs").compareTo("obs") != 0) {
            Book book = mBooks.get(mSlug);
            initFileName();
            numChapters = mParsedJson.getNumChapters(book.getSlug());
        }
        final String[] values = new String[numChapters];
        for(int i = 0; i < numChapters; i++){
            values[i] = String.valueOf(i+1);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (values != null && values.length > 0) {
                    mChapterPicker.setDisplayedValues(values);
                    mChapterPicker.setCurrent(mChapter - 1);
                    //reinitialize all of the filenames
                    initFileName();
                    mChapterPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                        pref.edit().putString(Settings.KEY_PREF_CHUNK, "01").commit();
                        pref.edit().putString(Settings.KEY_PREF_VERSE, "01").commit();
                        pref.edit().putString(Settings.KEY_PREF_START_VERSE, "01").commit();
                        pref.edit().putString(Settings.KEY_PREF_END_VERSE, "01").commit();
                        mChunk = 1;
                        mChunkPicker.setCurrent(0);
                        setChapter(newVal + 1);
                        mSrcPlayer.reset();
                        }
                    });
                } else {
                    Logger.e(this.toString(), "values was null or of zero length");
                }
            }
        });
    }

    private void initializeInsert(String oldName, int location){
        mInsertLoc = location;
        mFilename = oldName.substring(oldName.lastIndexOf("/")+1, oldName.lastIndexOf("."));
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
        mSrcPlayer.pauseSource();
        finish();
    }

    @Override
    public void onDestroy(){
        mSrcPlayer.cleanup();
        if(mPd != null && mPd.isShowing()){
            mPd.dismiss();
            mPd = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_INSERTING, mInserting);
    }

    private int getChunkIndex(ArrayList<Pair<Integer,Integer>> chunks, int chunk) {
        for (int i = 0; i < chunks.size(); i++) {
            if (chunks.get(i).first == chunk) {
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
        mSrcPlayer.cleanup();
        mSrcPlayer.setEnabled(false);

        // Take away increment and decrement buttons
        mChunkPicker.displayIncrementDecrement(false);
        mChapterPicker.displayIncrementDecrement(false);
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
            isRecording = false;
            isPausedRecording = false;
            Intent intent = new Intent(this, PlaybackScreen.class);
            if(mInsertMode){
                finalizeInsert(mFilename, recordedFilename, mInsertLoc);
            } else {
                intent.putExtra("recordedFilename", recordedFilename);
                startActivity(intent);
                this.finish();
            }
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
        String fullpath;
        if (pref.getString(Settings.KEY_PREF_PROJECT, "obs").compareTo("obs") != 0){
            fullpath = root + "/" + mLang + "/" + mSource + "/" + mSlug + "/" + String.format("%02d", mChapter) + "/";
        } else {
            fullpath = root + "/" + mLang + "/obs/" + String.format("%02d", mChapter) + "/";
        }
        pref.edit().putString("current_directory", fullpath).commit();

        String take = String.format("%02d", FileNameExtractor.getLargestTake(new File (fullpath), new File(mFilename+"_t00.wav"))+1);
        File filepath = new File(fullpath);

        if (!filepath.exists()) {
            filepath.mkdirs();
        }

        if (recordedFilename != null)
            return (fullpath + recordedFilename);
        else {
            recordedFilename = (fullpath + mFileNameExtractor.getNameWithoutTake() + "_t" + take + AUDIO_RECORDER_FILE_EXT_WAV);
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
            int startVerse = mChunks.get(idx-1).first;
            int endVerse = mChunks.get(idx-1).second;
            String chunkOrVerse = pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk");
            if(chunkOrVerse.compareTo("chunk") == 0) {
                //pref.edit().putString(Settings.KEY_PREF_CHUNK, ).commit();
                pref.edit().putString(Settings.KEY_PREF_START_VERSE, String.valueOf(startVerse)).commit();
                pref.edit().putString(Settings.KEY_PREF_END_VERSE, String.valueOf(endVerse)).commit();
            } else {
                pref.edit().putString(Settings.KEY_PREF_START_VERSE, String.valueOf(String.valueOf(startVerse))).commit();
                pref.edit().putString(Settings.KEY_PREF_END_VERSE, null).commit();
                System.out.println(pref.getString(Settings.KEY_PREF_END_VERSE, ""));
            }

            pref.edit().putString(Settings.KEY_PREF_TAKE, "1").commit();
            Settings.updateFilename(context);
            mFilename = pref.getString(Settings.KEY_PREF_FILENAME, String.valueOf(R.string.pref_default_filename));
            initFileName();
        }
    }

    private void setChapter(int chapter){
        if(mChunks != null) {
            //Get the list of chunks by first getting the book and chapter from the preference
            pref.edit().putString(Settings.KEY_PREF_CHAPTER, String.valueOf(chapter)).commit();
            pref.edit().putString(Settings.KEY_PREF_TAKE, "1").commit();
            Settings.updateFilename(context);
            mFilename = pref.getString(Settings.KEY_PREF_FILENAME, String.valueOf(R.string.pref_default_filename));
            initFileName();
        }
    }

    private void displayProgressDialog(){
        mPd = new ProgressDialog(this);
        mPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mPd.setTitle("Inserting recording");
        mPd.setMessage("Please wait...");
        mPd.setIndeterminate(true);
        mPd.setCancelable(false);
        mPd.show();
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnRecording).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnRecording, true);
        enableButton(R.id.btnStop, true);
        enableButton(R.id.btnPauseRecording, true);
    }

    private void finalizeInsert(String to, String from, int insertLoc){
        mInserting = true;
        displayProgressDialog();
        to = FileNameExtractor.getFileFromFileName(pref, to).toString();
        writeInsert(from, to, insertLoc);
    }

    public void insertCallback(String resultingFilename){
        mInserting = false;
        mPd.dismiss();
        Intent intent = new Intent(this, PlaybackScreen.class);
        File old = new File(resultingFilename);

        intent.putExtra("recordedFilename", old.toString());
        intent.putExtra("loadFile", true);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void writeInsert(String to, String from, int insertLoc){
        mInsertTaskFragment.writeInsert(to, from, insertLoc, pref);
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
        }
        }
    };
}
