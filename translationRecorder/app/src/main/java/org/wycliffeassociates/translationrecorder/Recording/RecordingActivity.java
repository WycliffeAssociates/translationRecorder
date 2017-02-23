package org.wycliffeassociates.translationrecorder.Recording;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.wycliffeassociates.translationrecorder.AudioVisualization.ActiveRecordingRenderer;
import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.Playback.PlaybackActivity;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingControls;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingFileBar;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingWaveform;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentSourceAudio;
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentVolumeBar;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.wav.WavMetadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class RecordingActivity extends AppCompatActivity implements
        FragmentRecordingControls.RecordingControlCallback, InsertTaskFragment.Insert,
        FragmentRecordingFileBar.OnUnitChangedListener
{

    public static final String KEY_PROJECT = "key_project";
    public static final String KEY_WAV_FILE = "key_wav_file";
    public static final String KEY_CHAPTER = "key_chapter";
    public static final String KEY_UNIT = "key_unit";
    public static final String KEY_INSERT_LOCATION = "key_insert_location";
    private static final String TAG_INSERT_TASK_FRAGMENT = "insert_task_fragment";
    private static final String STATE_INSERTING = "state_inserting";
    private static final int DEFAULT_CHAPTER = 1;
    private static final int DEFAULT_UNIT = 1;
    private Project mProject;
    private int mChapter;
    private int mUnit;
    private WavFile mLoadedWav;
    private int mInsertLocation;
    private boolean mInsertMode;
    private boolean isChunkMode;
    private InsertTaskFragment mInsertTaskFragment;
    private boolean mInserting;
    private ProgressDialog mProgressDialog;
    private HashMap<Integer, Fragment> mFragmentHolder;
    private FragmentRecordingFileBar mFragmentRecordingFileBar;
    private FragmentVolumeBar mFragmentVolumeBar;
    private FragmentRecordingControls mFragmentRecordingControls;
    private FragmentSourceAudio mFragmentSourceAudio;
    private FragmentRecordingWaveform mFragmentRecordingWaveform;
    private ActiveRecordingRenderer mRecordingRenderer;
    private boolean isRecording = false;
    private boolean onlyVolumeTest = true;
    private WavFile mNewRecording;
    private boolean isPausedRecording;
    private boolean isSaved;
    private boolean hasStartedRecording = false;

    public static Intent getInsertIntent(Context ctx, Project project, WavFile wavFile, int chapter, int unit, int locationMs) {
        Logger.w("RecordingActivity", "Creating Insert Intent");
        Intent intent = getRerecordIntent(ctx, project, wavFile, chapter, unit);
        intent.putExtra(KEY_INSERT_LOCATION, locationMs);
        return intent;
    }

    public static Intent getNewRecordingIntent(Context ctx, Project project, int chapter, int unit) {
        Logger.w("RecordingActivity", "Creating New Recording Intent");
        Intent intent = new Intent(ctx, RecordingActivity.class);
        intent.putExtra(KEY_PROJECT, project);
        intent.putExtra(KEY_CHAPTER, chapter);
        intent.putExtra(KEY_UNIT, unit);
        return intent;
    }

    public static Intent getRerecordIntent(Context ctx, Project project, WavFile wavFile, int chapter, int unit) {
        Logger.w("RecordingActivity", "Creating Rerecord Intent");
        Intent intent = getNewRecordingIntent(ctx, project, chapter, unit);
        intent.putExtra(KEY_WAV_FILE, wavFile);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_recording_screen);

        initialize(getIntent());
        initializeTaskFragment(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onlyVolumeTest = true;
        Intent volumeTestIntent = new Intent(this, WavRecorder.class);
        volumeTestIntent.putExtra(WavRecorder.KEY_VOLUME_TEST, onlyVolumeTest);
        startService(volumeTestIntent);
        mRecordingRenderer.listenForRecording(onlyVolumeTest);
    }

    @Override
    public void onPause() {
        Logger.w(this.toString(), "Recording screen onPauseRecording");
        super.onPause();
        if (isRecording) {
            isRecording = false;
            stopService(new Intent(this, WavRecorder.class));
            RecordingQueues.stopQueues(this);
        } else if (isPausedRecording) {
            RecordingQueues.stopQueues(this);
        } else if (!hasStartedRecording) {
            stopService(new Intent(this, WavRecorder.class));
            RecordingQueues.stopVolumeTest();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        Logger.w(this.toString(), "User pressed back");
        if (!isSaved && hasStartedRecording) {
            FragmentManager fm = getFragmentManager();
            FragmentExitDialog d = new FragmentExitDialog();
            d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            d.show(fm, "Exit Dialog");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initialize(Intent intent) {
        parseIntent(intent);
        initializeFragments();
        attachFragments();
        mRecordingRenderer = new ActiveRecordingRenderer(mFragmentRecordingControls, mFragmentVolumeBar, mFragmentRecordingWaveform);
    }

    private void initializeFragments() {
        mFragmentHolder = new HashMap<>();

        mFragmentRecordingControls = FragmentRecordingControls.newInstance(
                (mInsertMode)? FragmentRecordingControls.Mode.INSERT_MODE : FragmentRecordingControls.Mode.RECORDING_MODE
        );
        mFragmentHolder.put(R.id.fragment_recording_controls_holder, mFragmentRecordingControls);

        mFragmentSourceAudio = FragmentSourceAudio.newInstance();
        mFragmentHolder.put(R.id.fragment_source_audio_holder, mFragmentSourceAudio);

        mFragmentRecordingFileBar = FragmentRecordingFileBar.newInstance(
                mProject,
                mChapter,
                mUnit,
                (mInsertMode)? FragmentRecordingControls.Mode.INSERT_MODE : FragmentRecordingControls.Mode.RECORDING_MODE,
                isChunkMode
        );
        mFragmentHolder.put(R.id.fragment_recording_file_bar_holder, mFragmentRecordingFileBar);

        mFragmentVolumeBar = FragmentVolumeBar.newInstance();
        mFragmentHolder.put(R.id.fragment_volume_bar_holder, mFragmentVolumeBar);

        mFragmentRecordingWaveform = FragmentRecordingWaveform.newInstance();
        mFragmentHolder.put(R.id.fragment_recording_waveform_holder, mFragmentRecordingWaveform);
    }

    private void attachFragments(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Set<Map.Entry<Integer, Fragment>> entrySet = mFragmentHolder.entrySet();
        for (Map.Entry<Integer, Fragment> pair : entrySet) {
            ft.add(pair.getKey(), pair.getValue());
        }
        ft.commit();
    }

    private void parseIntent(Intent intent) {
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mChapter = intent.getIntExtra(KEY_CHAPTER, DEFAULT_CHAPTER);
        mUnit = intent.getIntExtra(KEY_UNIT, DEFAULT_UNIT);
        if (intent.hasExtra(KEY_WAV_FILE)) {
            mLoadedWav = intent.getParcelableExtra(KEY_WAV_FILE);
        }
        if (intent.hasExtra(KEY_INSERT_LOCATION)) {
            mInsertLocation = intent.getIntExtra(KEY_INSERT_LOCATION, 0);
            mInsertMode = true;
        }
        isChunkMode = mProject.getMode().equals("chunk");
    }

    private void initializeTaskFragment(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();
        mInsertTaskFragment = (InsertTaskFragment) fm.findFragmentByTag(TAG_INSERT_TASK_FRAGMENT);
        if (mInsertTaskFragment == null) {
            mInsertTaskFragment = new InsertTaskFragment();
            fm.beginTransaction().add(mInsertTaskFragment, TAG_INSERT_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        if (savedInstanceState != null) {
            mInserting = savedInstanceState.getBoolean(STATE_INSERTING, false);
            if (mInserting) {
                displayProgressDialog();
            }
        }
    }

    private void displayProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Inserting recording");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void onStartRecording() {
        hasStartedRecording = true;
        mFragmentSourceAudio.disableSourceAudio();
        mFragmentRecordingFileBar.disablePickers();
        onlyVolumeTest = false;
        isRecording = true;
        stopService(new Intent(this, WavRecorder.class));
        mRecordingRenderer.setIsRecording(true);
        if (!isPausedRecording) {
            RecordingQueues.stopVolumeTest();
            isSaved = false;
            RecordingQueues.clearQueues();
            String startVerse = mFragmentRecordingFileBar.getStartVerse();
            String endVerse = mFragmentRecordingFileBar.getEndVerse();
            File file = FileNameExtractor.createFile(mProject, mChapter, Integer.parseInt(startVerse), Integer.parseInt(endVerse));
            mNewRecording = new WavFile(file, new WavMetadata(mProject, String.valueOf(mChapter), startVerse, endVerse));
            startService(new Intent(this, WavRecorder.class));
            startService(WavFileWriter.getIntent(this, mNewRecording));
            mRecordingRenderer.listenForRecording(false);
        } else {
            isPausedRecording = false;
            startService(new Intent(this, WavRecorder.class));
        }
    }

    @Override
    public void onPauseRecording() {
        isPausedRecording = true;
        isRecording = false;
        stopService(new Intent(this, WavRecorder.class));
        RecordingQueues.pauseQueues();
        Logger.w(this.toString(), "Pausing recording");
    }

    @Override
    public void onStopRecording() {
        //Stop recording, load the recorded file, and draw
        stopService(new Intent(this, WavRecorder.class));
        long start = System.currentTimeMillis();
        Logger.w(this.toString(), "Stopping recording");
        RecordingQueues.stopQueues(this);
        Logger.w(this.toString(), "SUCCESS: exited queues, took " + (System.currentTimeMillis() - start) + " to finish writing");
        isRecording = false;
        isPausedRecording = false;
        addTakeToDb();
        mNewRecording.parseHeader();
        if (mInsertMode) {
            finalizeInsert(mLoadedWav, mNewRecording, mInsertLocation);
        } else {
            Intent intent = PlaybackActivity.getPlaybackIntent(this, mNewRecording, mProject, mChapter, mUnit);
            startActivity(intent);
            this.finish();
        }
    }

    private void addTakeToDb() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        FileNameExtractor fne = new FileNameExtractor(mNewRecording.getFile());
        db.addTake(fne, mNewRecording.getFile().getName(), mNewRecording.getMetadata().getMode(), mNewRecording.getFile().lastModified(), 0);
        db.close();
    }

    private void finalizeInsert(WavFile base, WavFile insertClip, int insertFrame) {
        //need to reparse the sizes after recording; updates to the object aren't reflected due to parceling to the writing service
        mNewRecording.parseHeader();
        mLoadedWav.parseHeader();
        mInserting = true;
        displayProgressDialog();
        writeInsert(base, insertClip, insertFrame);
    }

    @Override
    public void writeInsert(WavFile base, WavFile insertClip, int insertFrame) {
        mInsertTaskFragment.writeInsert(base, insertClip, insertFrame);
    }

    public void insertCallback(WavFile result) {
        mInserting = false;
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            Logger.e(this.toString(), "Tried to dismiss insert progress dialog", e);
        }
        Intent intent = PlaybackActivity.getPlaybackIntent(this, result, mProject, mChapter, mUnit);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onUnitChanged(Project project, String fileName, int chapter) {
        mFragmentSourceAudio.resetSourceAudio(project, fileName, chapter);
    }
}
