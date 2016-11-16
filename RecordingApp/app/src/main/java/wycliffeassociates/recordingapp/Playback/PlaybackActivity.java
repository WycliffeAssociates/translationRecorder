package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.WindowManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import wycliffeassociates.recordingapp.AudioVisualization.WavVisualizer;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentFileBar;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentPlaybackTools;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentTabbedWidget;
import wycliffeassociates.recordingapp.Playback.fragments.MarkerCounterFragment;
import wycliffeassociates.recordingapp.Playback.fragments.MarkerToolbarFragment;
import wycliffeassociates.recordingapp.Playback.fragments.WaveformFragment;
import wycliffeassociates.recordingapp.Playback.interfaces.AudioEditDelegator;
import wycliffeassociates.recordingapp.Playback.interfaces.AudioStateCallback;
import wycliffeassociates.recordingapp.Playback.interfaces.EditStateInformer;
import wycliffeassociates.recordingapp.Playback.interfaces.MediaController;
import wycliffeassociates.recordingapp.Playback.interfaces.VerseMarkerModeToggler;
import wycliffeassociates.recordingapp.Playback.interfaces.ViewCreatedCallback;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.dialogs.RatingDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.wav.WavFile;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;



/**
 * Created by sarabiaj on 10/27/2016.
 */

public class PlaybackActivity extends Activity implements RatingDialog.DialogListener, MediaController,
        AudioStateCallback, AudioEditDelegator, EditStateInformer, WaveformFragment.WaveformDrawDelegator,
        ViewCreatedCallback, WaveformFragment.OnScrollDelegator, VerseMarkerModeToggler, MarkerToolbarFragment.OnMarkerPlacedListener {



    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_WAV_FILE = "wavfile";
    private static final String KEY_CHAPTER = "key_chapter";
    private static final String KEY_UNIT = "key_unit";

    private volatile boolean isSaved = true;
    private boolean isPlaying = false;
    private boolean isInVerseMarkerMode = false;

    private WavVisualizer wavVis;
    private WavFile mWavFile;
    private WavFileLoader wavFileLoader;
    private CutOp mCutOp;
    private Project mProject;
    private int mChapter, mUnit, mRating, mVersesLeft;
    private AudioVisualController mAudioController;
    private HashMap<Integer, Fragment> mFragmentContainerMapping;
    private FragmentPlaybackTools mFragmentPlaybackTools;
    private FragmentTabbedWidget mFragmentTabbedWidget;
    private FragmentFileBar mFragmentFileBar;
    private WaveformFragment mWaveformFragment;
    private MarkerCounterFragment mMarkerCounterFragment;
    private MarkerToolbarFragment mMarkerToolbarFragment;

    public static Intent getPlaybackIntent(Context ctx, WavFile file, Project project, int chapter, int unit) {
        Intent intent = new Intent(ctx, PlaybackActivity.class);
        intent.putExtra(KEY_PROJECT, project);
        intent.putExtra(KEY_WAV_FILE, file);
        intent.putExtra(KEY_CHAPTER, chapter);
        intent.putExtra(KEY_UNIT, unit);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_playback_screen);
        initialize(getIntent());
    }

    private void initialize(Intent intent) {
        isSaved = true;
        parseIntent(intent);
        getVersesLeft();
        initializeFragments();
        initializeViews();
        wavFileLoader = new WavFileLoader(mWavFile);
        mCutOp = new CutOp();
        wavVis = new WavVisualizer(wavFileLoader.getMappedFile(), wavFileLoader.getMappedCacheFile(), 1920, 490, 1920, mCutOp);
        mAudioController = new AudioVisualController(this, mWavFile);
    }

    private void parseIntent(Intent intent) {
        mWavFile = intent.getParcelableExtra(KEY_WAV_FILE);
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mUnit = intent.getIntExtra(KEY_UNIT, 1);
        mChapter = intent.getIntExtra(KEY_CHAPTER, 1);
    }

    private void initializeFragments() {
        mFragmentContainerMapping = new HashMap<>();

        mFragmentPlaybackTools = FragmentPlaybackTools.newInstance();
        mFragmentContainerMapping.put(R.id.playback_tools_fragment_holder, mFragmentPlaybackTools);

        mFragmentTabbedWidget = FragmentTabbedWidget.newInstance(mProject, FileNameExtractor.getNameWithoutTake(mWavFile.getFile().getName()), mChapter);
        mFragmentContainerMapping.put(R.id.tabbed_widget_fragment_holder, mFragmentTabbedWidget);

        mFragmentFileBar = FragmentFileBar.newInstance(mProject.getTargetLanguage(),
                mProject.getVersion(), mProject.getSlug(), "Chapter", String.valueOf(mChapter),
                mProject.getMode(), String.valueOf(mUnit));
        mFragmentContainerMapping.put(R.id.file_bar_fragment_holder, mFragmentFileBar);

        mWaveformFragment = WaveformFragment.newInstance();
        mFragmentContainerMapping.put(R.id.waveform_fragment_holder, mWaveformFragment);

        mMarkerCounterFragment = MarkerCounterFragment.newInstance(mVersesLeft);
        mMarkerToolbarFragment = MarkerToolbarFragment.newInstance();
        attachFragments();
    }

    private void attachFragments() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Set<Map.Entry<Integer, Fragment>> entrySet = mFragmentContainerMapping.entrySet();
        for(Map.Entry<Integer, Fragment> pair : entrySet) {
            ft.add(pair.getKey(), pair.getValue());
        }
        ft.commit();
    }

    private void initializeViews() {

        // By default, select the minimap view over the source playback

//        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
//        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
//        mRating = db.getTakeRating(fne);
//        mRateBtn.setStep(mRating);
//        mRateBtn.invalidate();
//        db.close();
    }

    @Override
    public void onMediaPause(){
        mAudioController.pause();
    }

    @Override
    public void onMediaPlay() {
        mAudioController.play();
        Thread playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int location;
                while (mAudioController.isPlaying()) {
                    location = mAudioController.getLocation();
                    mFragmentPlaybackTools.onLocationUpdated(location);
                    mWaveformFragment.onLocationUpdated(location);
                    //getLocationMs();
                    //draw();
                    //             System.out.println(mPlayer.getLocationMs());
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        playbackThread.start();
    }

    @Override
    public void onSeekForward() {
        mAudioController.seekNext();
    }

    @Override
    public void onSeekBackward() {
        mAudioController.seekPrevious();
    }

    @Override
    public int getDuration() {
        return mAudioController.getDuration();
    }

    @Override
    public int getLocation() {
        return mAudioController.getLocation();
    }

    @Override
    public void setOnCompleteListner(Runnable onComplete) {
        //mAudioController.setOnCompleteListener(onComplete);
    }

    @Override
    public void onPlayerPaused() {
        mFragmentPlaybackTools.onPlayerPaused();
    }

    @Override
    public void onSave(){
        save(null);
    }

    @Override
    public void onCut() {
        mAudioController.cut();
        mFragmentPlaybackTools.onLocationUpdated(mAudioController.getLocation());
        mFragmentPlaybackTools.onDurationUpdated(mAudioController.getDuration());
    }

    @Override
    public void onDropStartMarker() {
        mAudioController.dropStartMarker();
        mWaveformFragment.addStartMarker(getLocation());
    }

    @Override
    public void onDropEndMarker() {
        mAudioController.dropEndMarker();
        mWaveformFragment.addEndMarker(getLocation());
    }

    @Override
    public void onClearMarkers() {
        mAudioController.clearMarkers();
    }

    @Override
    public void onDropVerseMarker() {

    }

    @Override
    public void onUndo() {
        mAudioController.undo();
    }

    @Override
    public boolean hasEdits(){
        return mAudioController.mCutOp.hasCut();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mManager.release();
//        SectionMarkers.clearMarkers(mManager);
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        mRating = dialog.getRating();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), mRating);
        db.close();
        //mRateBtn.setStep(mRating);
    }

    @Override
    public void onNegativeClick(RatingDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
        if(isInVerseMarkerMode) {
            onDisableVerseMarkerMode();
        } else {
            super.onBackPressed();
        }
//        if (!isSaved && mManager.hasCut()) {
//            Logger.i(this.toString(), "Asking if user wants to save before going back");
//            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_AppCompat_Light_Dialog, true, isPlaying, mWavFile.getFile());
//            exit.show();
//        } else {
////            clearMarkers();
//            mManager.release();
//            super.onBackPressed();
//        }
    }

    private void openRating(FourStepImageView v) {
        RatingDialog dialog = RatingDialog.newInstance(mWavFile.getFile().getName(), mRating);
        dialog.show(getFragmentManager(), "single_unit_rating");
    }

    private void rerecord() {
        Intent intent = RecordingScreen.getRerecordIntent(this, mProject, mWavFile, mChapter, mUnit);
        save(intent);
    }

    private void save(Intent intent) {
        //no changes were made, so just exit
        if (isSaved) {
            if (intent == null) {
                this.finish();
                return;
            } else {
                startActivity(intent);
                this.finish();
                return;
            }
        }

        File dir = new File(Project.getProjectDirectory(mProject), FileNameExtractor.chapterIntToString(mProject, mChapter));
        File from = mWavFile.getFile();
        int takeInt = FileNameExtractor.getLargestTake(dir, from) + 1;
        String take = String.format("%02d", takeInt);
        FileNameExtractor fne = new FileNameExtractor(from);

        File to = new File(dir, fne.getNameWithoutTake() + "_t" + take + AUDIO_RECORDER_FILE_EXT_WAV);
        writeCutToFile(to, mWavFile, intent);
    }

    /**
     * Names the currently recorded .wav file.
     *
     * @return the absolute path of the file created
     */
    public void writeCutToFile(final File to, final WavFile from, final Intent intent) {

//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("Saving");
//        pd.setMessage("Writing changes to file, please wait...");
//        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        pd.setProgressNumberFormat(null);
//        pd.show();
//        Thread saveThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (mAudioController.hasCut()) {
//                    try {
//                        File dir = Project.getProjectDirectory(mProject);
//                        File toTemp = new File(dir, "temp.wav");
//                        mAudioController.writeCut(toTemp, from, pd);
//                        to.delete();
//                        toTemp.renameTo(to);
//                        ProjectDatabaseHelper db = new ProjectDatabaseHelper(PlaybackActivity.this);
//                        db.addTake(new FileNameExtractor(to), to.getName(), to.lastModified(), 0);
//                        db.close();
//                        String oldName = from.getFile().getName();
//                        oldName = oldName.substring(0, oldName.lastIndexOf("."));
//                        File toVis = new File(Utils.VISUALIZATION_DIR, oldName + ".vis");
//                        toVis.delete();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                isSaved = true;
//                pd.dismiss();
//                if (intent == null) {
//                    finish();
//                } else {
//                    WavFile result = new WavFile(to);
//                    intent.putExtra(RecordingScreen.KEY_WAV_FILE, result);
//                    startActivity(intent);
//                    finish();
//                }
//            }
//        });
//        saveThread.start();
    }

    public void insert() {
//        Intent insertIntent = RecordingScreen.getInsertIntent(this, mProject, mWavFile, mChapter, mUnit, mManager.getAdjustedLocation());
//        save(insertIntent);
    }

    private boolean allVersesMarked() {
        return mVersesLeft <= 0;
    }

    private void getVersesLeft() {
        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
        mVersesLeft = fne.getEndVerse() - fne.getEndVerse();
    }

    private void setVerseMarkerCount(int count) {
        // - 1 because the first verse marker should be automatically dropped at the beginning
        //mVerseMarkerCount.setText(String.valueOf(count));
    }

//    private View[] getViewsToHideInMarkerMode() {
//        return new View[]{mLangView, mSourceView, mBookView, mChapterView, mChapterLabel,
//                mUnitView, mUnitLabel, mEnterVerseMarkerMode, mRateBtn, mRerecordBtn, mInsertBtn,
//                mDropStartMarkBtn, mSaveBtn, mDropStartMarkBtn};
//    }
//
//    private View[] getViewsToHideInNormalMode() {
//        return new View[]{mExitVerseMarkerMode, mVerseMarkerCount, mVerseMarkerLabel, mDropVerseMarkerBtn,
//                mCompleteVerseMarkerBtn};
//    }

//    private void enterVerseMarkerMode() {
//        isInVerseMarkerMode = true;
//        Utils.showView(getViewsToHideInNormalMode());
//        Utils.hideView(getViewsToHideInMarkerMode());
//        Utils.hideView(allVersesMarked() ? mDropVerseMarkerBtn : mCompleteVerseMarkerBtn);
//        mToolbar.setBackgroundColor(getResources().getColor(R.color.tertiary));
//    }
//
//    private void exitVerseMarkerMode() {
//        isInVerseMarkerMode = false;
//        Utils.showView(getViewsToHideInMarkerMode());
//        Utils.hideView(getViewsToHideInNormalMode());
//        mToolbar.setBackgroundColor(getResources().getColor(R.color.primary));
//    }

    private void dropVerseMarker() {
        //mMainCanvas.dropVerseMarker(mManager.getLocationMs());
        //mManager.updateUI();
    }

    private void saveVerseMarkerPosition() {
        // NOTE: Put real code here
        System.out.println("Save verse marker position here");
    }

    @Override
    public void onDrawWaveform(Canvas canvas, Paint paint) {
        canvas.drawLines(wavVis.getDataToDraw(mAudioController.getLocation()), paint);
    }

    @Override
    public void onViewCreated(Object ref, int width, int height) {
//        if(ref instanceof WaveformFragment){
//            wavVis = new WavVisualizer(wavFileLoader.getMappedFile(), wavFileLoader.getMappedCacheFile(), width, height, width, mCutOp);
//        } else {
//            wavVis.
//        }
    }

    @Override
    public void delegateOnScroll(float distX) {
        mAudioController.scrollAudio(distX);
    }

    @Override
    public void onLocationUpdated(int location){
        mWaveformFragment.onLocationUpdated(location);
    }

    @Override
    public void onEnableVerseMarkerMode() {
        isInVerseMarkerMode = true;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .remove(mFragmentFileBar)
                .add(R.id.file_bar_fragment_holder, mMarkerCounterFragment)
                .remove(mFragmentPlaybackTools)
                .add(R.id.playback_tools_fragment_holder, mMarkerToolbarFragment)
                .commit();
    }

    @Override
    public void onDisableVerseMarkerMode() {
        isInVerseMarkerMode = false;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .remove(mMarkerCounterFragment)
                .add(R.id.file_bar_fragment_holder, mFragmentFileBar)
                .remove(mMarkerToolbarFragment)
                .add(R.id.playback_tools_fragment_holder, mFragmentPlaybackTools)
                .commit();
    }

    @Override
    public void onMarkerPlaced() {
        mAudioController.dropVerseMarker("Verse " + "1", mAudioController.getLocationInFrames());
        mWaveformFragment.addVerseMarker(1);
    }
}
