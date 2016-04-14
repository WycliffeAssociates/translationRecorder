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
    private SourceAudio mSrcPlayer;
    private SharedPreferences pref;

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


        pref = PreferenceManager.getDefaultSharedPreferences(this);
        suggestedFilename = pref.getString(Settings.KEY_PREF_FILENAME, "en_mat_1-1");

        mInsertMode = getIntent().getBooleanExtra("insert_mode", false);
        if(mInsertMode){
            initializeInsert(getIntent().getStringExtra("old_name"), getIntent().getIntExtra("insert_location", 0));
        }

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        initViews();
        setButtonHandlers();
        enableButtons();

        manager = new UIDataManager(mainCanvas, minimap, mVolumeBar, null, null, this, UIDataManager.RECORDING_MODE, true);
        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(true);

        hasStartedRecording = false;
        mDeleteTempFile = false;
        if(!mInsertMode) {
            initChunkPicker();
        } else {
            findViewById(R.id.numberPicker).setVisibility(View.INVISIBLE);
        }
        mSrcPlayer = new SourceAudio(this);
        mSrcPlayer.initSrcAudio();
    }

    private void initViews(){
        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        mVolumeBar = (VolumeBar) findViewById((R.id.volumeBar1));

        filenameView = (TextView) findViewById(R.id.filenameView);
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
                                    setChunk(newVal);
                                    mSrcPlayer.reset();
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

    private void initializeInsert(String oldName, int location){
        mInsertLoc = location;
        suggestedFilename = oldName.substring(oldName.lastIndexOf("/")+1, oldName.lastIndexOf("."));
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
        mSrcPlayer.cleanup();
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
            isRecording = false;
            isPausedRecording = false;
            Intent intent = new Intent(this, PlaybackScreen.class);
            if(mInsertMode){
                finalizeInsert(suggestedFilename, recordedFilename, mInsertLoc);
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
        String lang = pref.getString(Settings.KEY_PREF_LANG, "en");
        String src = pref.getString(Settings.KEY_PREF_SOURCE, "udb");
        String book = pref.getString(Settings.KEY_PREF_BOOK, "mat");
        String chap = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_CHAPTER, "1")));
        String chunk = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_CHUNK, "1")));
        if(pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") != 0){
            chunk = chap = String.format("%02d", Integer.parseInt(pref.getString(Settings.KEY_PREF_VERSE, "1")));
        }
        String fullpath = root + "/" + lang + "/" + src + "/" + book + "/" + chap + "/";
        pref.edit().putString("current_directory", fullpath).commit();
        String filename = pref.getString(Settings.KEY_PREF_FILENAME, "en_ulb_mat_01-01");

        String take = String.format("%02d", FileNameExtractor.getLargestTake(new File (fullpath), new File(filename+"_00.wav"))+1);
        File filepath = new File(fullpath);

        if (!filepath.exists()) {
            filepath.mkdirs();
        }

        if (recordedFilename != null)
            return (fullpath + recordedFilename);
        else {
            recordedFilename = (fullpath + filename + "_" + take + AUDIO_RECORDER_FILE_EXT_WAV);
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
            case R.id.btnPlaySource: {
                mSrcPlayer.playSource();
                break;
            }
            case R.id.btnPauseSource: {
                mSrcPlayer.pauseSource();
                break;
            }
        }
        }
    };
}
