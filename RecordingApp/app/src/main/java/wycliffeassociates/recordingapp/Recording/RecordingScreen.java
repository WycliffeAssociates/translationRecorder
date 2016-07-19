package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import wycliffeassociates.recordingapp.AudioVisualization.VolumeBar;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.Playback.SourceAudio;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;

public class RecordingScreen extends Activity implements InsertTaskFragment.Insert{

    public static final String KEY_PROJECT = "key_project";
    public static final String KEY_WAV_FILE = "key_wav_file";
    public static final String KEY_CHAPTER = "key_chapter";
    public static final String KEY_UNIT = "key_unit";
    public static final String KEY_INSERT_LOCATION = "key_insert_location";
    private static final String TAG_INSERT_TASK_FRAGMENT = "insert_task_fragment";
    private static final String STATE_INSERTING = "state_inserting";
    private static final int DEFAULT_CHAPTER = 1;
    private static final int DEFAULT_UNIT = 1;

    //View
    private WaveformView mMainWavView;
    private VolumeBar mVolumeBar;
    private MinimapView mMinimapWavView;
    private TextView mSourceView;
    private TextView mLanguageView;
    private TextView mBookView;
    private UnitPicker mChunkPicker;
    private UnitPicker mChapterPicker;

    //Controller
    private UIDataManager manager;

    //State
    private boolean isSaved = false;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;
    private boolean hasStartedRecording = false;
    private boolean mInserting = false;
    private boolean mInsertMode = false;

    private SourceAudio mSrcPlayer;
    private InsertTaskFragment mInsertTaskFragment;
    private ProgressDialog mProgressDialog;
    private WavFile mNewRecording;
    private WavFile mLoadedWav;
    ConstantsDatabaseHelper mConstantsDB;
    private Project mProject;
    private int mChapter = DEFAULT_CHAPTER;
    private int mUnit = DEFAULT_UNIT;
    private int mInsertLocation = 0;
    private Chunks mChunks;
    private List<Map<String, String>> mChunksList;
    private int mNumChapters;
    private String mStartVerse;
    private TextView mModeView;

    public static Intent getInsertIntent(Context ctx, Project project, WavFile wavFile, int chapter, int unit, int locationMs){
        Intent intent = getRerecordIntent(ctx, project, wavFile, chapter, unit);
        intent.putExtra(KEY_INSERT_LOCATION, locationMs);
        return intent;
    }

    private String mEndVerse;

    public static Intent getNewRecordingIntent(Context ctx, Project project, int chapter, int unit){
        Intent intent = new Intent(ctx, RecordingScreen.class);
        intent.putExtra(KEY_PROJECT, project);
        intent.putExtra(KEY_CHAPTER, chapter);
        intent.putExtra(KEY_UNIT, unit);
        return intent;
    }

    public static Intent getRerecordIntent(Context ctx, Project project, WavFile wavFile, int chapter, int unit){
        Intent intent = getNewRecordingIntent(ctx, project, chapter, unit);
        intent.putExtra(KEY_WAV_FILE, wavFile);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.recording_screen);

        initialize(getIntent());
        initializeTaskFragment(savedInstanceState);

        manager = new UIDataManager(mMainWavView, mMinimapWavView, mVolumeBar, null, null, this, UIDataManager.RECORDING_MODE);
        startService(new Intent(this, WavRecorder.class));
        manager.listenForRecording(true);
        mSrcPlayer.initSrcAudio(mProject, FileNameExtractor.getNameFromProject(mProject, mChapter,
                Integer.parseInt(mStartVerse), Integer.parseInt(mEndVerse)), mChapter);
    }

    private void initialize(Intent intent){
        mConstantsDB = new ConstantsDatabaseHelper(this);
        findViews();
        parseIntent(intent);
        initializeViews();
        setButtonHandlers();
        enableButtons();
        try {
            initializeUnitPickers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseIntent(Intent intent){
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mChapter = intent.getIntExtra(KEY_CHAPTER, DEFAULT_CHAPTER);
        mUnit = intent.getIntExtra(KEY_UNIT, DEFAULT_UNIT);
        if(intent.hasExtra(KEY_WAV_FILE)) {
            mLoadedWav = intent.getParcelableExtra(KEY_WAV_FILE);
        }
        if(intent.hasExtra(KEY_INSERT_LOCATION)){
            mInsertLocation = intent.getIntExtra(KEY_INSERT_LOCATION, 0);
            mInsertMode = true;
        }
    }

    private void initializeTaskFragment(Bundle savedInstanceState){
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

    private void findViews(){
        mSrcPlayer = (SourceAudio) findViewById(R.id.srcAudioPlayer);
        mMainWavView = ((WaveformView) findViewById(R.id.main_canvas));
        mMinimapWavView = ((MinimapView) findViewById(R.id.minimap));
        mVolumeBar = (VolumeBar) findViewById((R.id.volumeBar1));
        mBookView = (TextView) findViewById(R.id.file_book);
        mSourceView = (TextView) findViewById(R.id.file_project);
        mLanguageView = (TextView) findViewById(R.id.file_language);
        mChunkPicker = (UnitPicker) findViewById(R.id.unit_picker);
        mChapterPicker = (UnitPicker) findViewById(R.id.chapter_picker);
        mModeView = (TextView) findViewById(R.id.file_unit_label);
        mMainWavView.disableGestures();
        if(mInsertMode) {
            mChunkPicker.displayIncrementDecrement(false);
            mChapterPicker.displayIncrementDecrement(false);
        }
    }

    private void initializeViews(){
        String languageCode = mProject.getTargetLanguage();
        mLanguageView.setText(languageCode);
        mLanguageView.postInvalidate();

        String bookCode = mProject.getSlug();
        String bookName = mConstantsDB.getBookName(bookCode);
        mBookView.setText(bookName);
        mBookView.postInvalidate();

        if(mProject.getMode().compareTo("chunk") == 0){
            mModeView.setText("Chunk");
        } else {
            mModeView.setText("Verse");
        }

        mSourceView.setText(mProject.getSource().toUpperCase());
    }

    private void initializeUnitPickers() throws IOException{
        if(mProject.isOBS()){
            //mNumChapters = OBS_SIZE;
        } else {
            mChunks = new Chunks(this, mProject.getSlug());
            mNumChapters = mChunks.getNumChapters();
            mChunksList = mChunks.getChunks(mProject,  mChapter);
        }
        initializeChunkPicker();
        initializeChapterPicker();
    }

    private void initializeChunkPicker(){
        final String[] values = new String[mChunksList.size()];
        for(int i = 0; i < mChunksList.size(); i++){
            values[i] = mChunksList.get(i).get(Chunks.FIRST_VERSE);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (values != null && values.length > 0) {
                    mChunkPicker.setDisplayedValues(values);
                    mChunkPicker.setCurrent(getChunkIndex(mChunksList, mUnit));
                    setChunk(getChunkIndex(mChunksList, mUnit) + 1);
                    //reinitialize all of the filenames
                    mChunkPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                            setChunk(newVal + 1);
                            mUnit = newVal + 1;
//                            mSrcPlayer.reset(mProject, FilenameUtils.removeExtension(mNewRecording.getFile().getName()), mChapter);
                        }
                    });
                } else {
                    Logger.e(this.toString(), "values was null or of zero length");
                }
            }
        });
    }

    private void initializeChapterPicker(){
        int numChapters = mChunks.getNumChapters();
        final String[] values = new String[numChapters];
        for(int i = 0; i < numChapters; i++){
            values[i] = String.valueOf(i+1);
        }
        if (values != null && values.length > 0) {
            mChapterPicker.setDisplayedValues(values);
            mChapterPicker.setCurrent(mChapter - 1);
            mChapterPicker.setOnValueChangedListener(new UnitPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(UnitPicker picker, int oldVal, int newVal) {
                    mUnit = 1;
                    mChapter = newVal + 1;
                    mChunkPicker.setCurrent(0);
                    mChunksList = mChunks.getChunks(mProject, mChapter);
                    initializeChunkPicker();
                    mSrcPlayer.reset(mProject, FileNameExtractor.getNameFromProject(mProject, mChapter,
                            Integer.parseInt(mStartVerse), Integer.parseInt(mEndVerse)), mChapter);
                }
            });
        } else {
            Logger.e(this.toString(), "values was null or of zero length");
        }
    }

    private int getChunkIndex(List<Map<String,String>> chunks, int chunk) {
        for (int i = 0; i < chunks.size(); i++) {
            if (Integer.parseInt(chunks.get(i).get(Chunks.FIRST_VERSE)) == chunk) {
                return i;
            }
        }
        return 1;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isRecording) {
            isRecording = false;
            stopService(new Intent(this, WavRecorder.class));
            RecordingQueues.stopQueues(this);
        } else if(isPausedRecording){
            RecordingQueues.stopQueues(this);
        } else if(!hasStartedRecording){
            stopService(new Intent(this, WavRecorder.class));
            RecordingQueues.stopVolumeTest();
        }
        mSrcPlayer.pauseSource();
        finish();
    }

    @Override
    public void onDestroy(){
        mSrcPlayer.cleanup();
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_INSERTING, mInserting);
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
            try {
                File file = FileNameExtractor.createFile(mProject, mChapter, Integer.parseInt(mStartVerse), Integer.parseInt(mEndVerse));
                mNewRecording = WavFile.createNewWavFile(file, mProject, String.valueOf(mChapter), mStartVerse, mEndVerse);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startService(new Intent(this, WavRecorder.class));
            startService(WavFileWriter.getIntent(this, mNewRecording));
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
            if(mInsertMode){
                finalizeInsert(mLoadedWav, mNewRecording, mInsertLocation);
            } else {
                Intent intent = PlaybackScreen.getPlaybackIntent(this, mNewRecording, mProject, mChapter, mUnit);
                startActivity(intent);
                this.finish();
            }
        }
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
     * Sets the chunk by indexing the chunk list with the provided index
     * @param idx
     */
    private void setChunk(int idx){
        if(mChunks != null) {
            mStartVerse = mChunksList.get(idx-1).get(Chunks.FIRST_VERSE);
            mEndVerse = mChunksList.get(idx-1).get(Chunks.LAST_VERSE);
        }
    }

    private void displayProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Inserting recording");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
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

    private void finalizeInsert(WavFile base, WavFile insertClip, int insertLoc){
        //need to reparse the sizes after recording; updates to the object aren't reflected due to parceling to the writing service
        mNewRecording.parseChunkSizes();
        mLoadedWav.parseChunkSizes();
        mInserting = true;
        displayProgressDialog();
        writeInsert(base, insertClip, insertLoc);
    }

    @Override
    public void writeInsert(WavFile base, WavFile insertClip, int insertLoc){
        mInsertTaskFragment.writeInsert(base, insertClip, insertLoc);
    }

    public void insertCallback(WavFile result){
        mInserting = false;
        mProgressDialog.dismiss();
        Intent intent = PlaybackScreen.getPlaybackIntent(this, mNewRecording, mProject, mChapter, mUnit);
        startActivity(intent);
        this.finish();
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
