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
import android.view.ViewTreeObserver;
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
import wycliffeassociates.recordingapp.Playback.interfaces.MarkerMediator;
import wycliffeassociates.recordingapp.Playback.interfaces.MediaController;
import wycliffeassociates.recordingapp.Playback.interfaces.VerseMarkerModeToggler;
import wycliffeassociates.recordingapp.Playback.interfaces.ViewCreatedCallback;
import wycliffeassociates.recordingapp.Playback.overlays.MinimapLayer;
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
        ViewCreatedCallback, WaveformFragment.OnScrollDelegator, VerseMarkerModeToggler, MarkerToolbarFragment.OnMarkerPlacedListener,
        MinimapLayer.MinimapDrawDelegator, FragmentTabbedWidget.DelegateMinimapMarkerDraw, FragmentFileBar.RerecordCallback, FragmentFileBar.RatingCallback,
        FragmentFileBar.InsertCallback
{



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
    private MarkerMediator mMarkerMediator;
    private boolean mWaveformInflated = false;
    private boolean mMinimapInflated = false;

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
        mAudioController = new AudioVisualController(this, mWavFile);
        mMarkerMediator = new MarkerHolder(mAudioController);
        initializeFragments();
        initializeViews();
        wavFileLoader = new WavFileLoader(mWavFile);
        mCutOp = new CutOp();
        //wavVis = new WavVisualizer(wavFileLoader.getMappedFile(), wavFileLoader.getMappedCacheFile(), 1920, 490, 1920, mCutOp);
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
                mProject.getMode(),
                getUnitLabel());
        mFragmentContainerMapping.put(R.id.file_bar_fragment_holder, mFragmentFileBar);

        mWaveformFragment = WaveformFragment.newInstance(mMarkerMediator);
        mFragmentContainerMapping.put(R.id.waveform_fragment_holder, mWaveformFragment);

        mMarkerCounterFragment = MarkerCounterFragment.newInstance(mVersesLeft);
        mMarkerToolbarFragment = MarkerToolbarFragment.newInstance();
        attachFragments();
    }

    private String getUnitLabel(){
        if(mProject.getMode().equals("chunk")){
            FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
            return String.format("%d-%d",fne.getStartVerse(), fne.getEndVerse());
        } else {
            return String.valueOf(mUnit);
        }
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
                    location = mAudioController.getLocationInFrames();
                    mFragmentPlaybackTools.onLocationUpdated(location);
                    mWaveformFragment.onLocationUpdated(location);
                    mFragmentTabbedWidget.onLocationChanged();
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
        mWaveformFragment.onLocationUpdated(mAudioController.getLocationInFrames());
    }

    @Override
    public void onSeekBackward() {
        mAudioController.seekPrevious();
        mWaveformFragment.onLocationUpdated(mAudioController.getLocationInFrames());
    }

    @Override
    public void onSeekTo(float x) {
        mAudioController.seekTo((int)(x * mAudioController.getDurationInFrames()));
        onLocationUpdated(getLocation());
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
    public int getLocationInFrames() {
        return mAudioController.getLocationInFrames();
    }

    @Override
    public int getDurationInFrames() {
        return mAudioController.getDurationInFrames();
    }

    @Override
    public void setOnCompleteListner(Runnable onComplete) {
        //mAudioController.setOnCompleteListener(onComplete);
    }

    @Override
    public int getStartMarkerFrame() {
        return mAudioController.getLoopStart();
    }

    @Override
    public int getEndMarkerFrame() {
        return mAudioController.getLoopEnd();
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
        onClearMarkers();
    }

    @Override
    public void onDropStartMarker() {
        mAudioController.dropStartMarker();
        int location = mAudioController.getLoopStart();
        mWaveformFragment.addStartMarker(location);
        mWaveformFragment.onLocationUpdated(location);
    }

    @Override
    public void onDropEndMarker() {
        mAudioController.dropEndMarker();
        int location = mAudioController.getLoopEnd();
        mWaveformFragment.addEndMarker(location);
        mWaveformFragment.onLocationUpdated(location);
    }

    @Override
    public void setStartMarkerAt(int frame){
        mAudioController.setStartMarker(frame);
        mWaveformFragment.addStartMarker(frame);
        mWaveformFragment.onLocationUpdated(mAudioController.getLocation());
        mFragmentTabbedWidget.onLocationChanged();
    }

    @Override
    public void setEndMarkerAt(int frame) {
        mAudioController.setEndMarker(frame);
        mWaveformFragment.addEndMarker(frame);
        mWaveformFragment.onLocationUpdated(mAudioController.getLocation());
        mFragmentTabbedWidget.onLocationChanged();
    }

    @Override
    public void onClearMarkers() {
        mMarkerMediator.onRemoveSectionMarkers();
    }

    @Override
    public boolean hasSetMarkers() {
        if(mAudioController.getLoopStart() != 0 && mAudioController.getLoopEnd() != mAudioController.getDurationInFrames()) {
            return true;
        } else {
            return false;
        }
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
//        SectionMarkers.clearLoopPoints(mManager);
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        mRating = dialog.getRating();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), mRating);
        db.close();
        mFragmentFileBar.onRatingChanged(mRating);
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
////            clearLoopPoints();
//            mManager.release();
//            super.onBackPressed();
//        }
    }

    public void onOpenRating(FourStepImageView v) {
        RatingDialog dialog = RatingDialog.newInstance(mWavFile.getFile().getName(), mRating);
        dialog.show(getFragmentManager(), "single_unit_rating");
    }

    public void onRerecord() {
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

    public void onInsert() {
        Intent insertIntent = RecordingScreen.getInsertIntent(this, mProject, mWavFile, mChapter, mUnit, mAudioController.getLocation());
        save(insertIntent);
    }

    private boolean allVersesMarked() {
        return mVersesLeft <= 0;
    }

    private void getVersesLeft() {
        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
        mVersesLeft = fne.getEndVerse() - fne.getStartVerse();
    }

    private void setVerseMarkerCount(int count) {
        // - 1 because the first verse marker should be automatically dropped at the beginning
        //mVerseMarkerCount.setText(String.valueOf(count));
    }

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
    public void onViewCreated(Fragment ref) {
        if(ref instanceof WaveformFragment){
            mWaveformFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mWaveformInflated = true;
                    if(mWaveformInflated && mMinimapInflated) {
                        initializeRenderer();
                    }
                }
            });
        } else if (ref instanceof FragmentTabbedWidget){
            mWaveformFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMinimapInflated = true;
                    if(mWaveformInflated && mMinimapInflated) {
                        initializeRenderer();
                    }
                }
            });
        }
    }

    private void initializeRenderer(){
        wavVis = new WavVisualizer(wavFileLoader.getMappedFile(), wavFileLoader.getMappedCacheFile(), mWaveformFragment.getView().getWidth(), mWaveformFragment.getView().getHeight(), mFragmentTabbedWidget.getWidgetWidth(), mCutOp);
        mFragmentTabbedWidget.initializeTimecode(mAudioController.getDuration());
    }

    @Override
    public void delegateOnScroll(float distX) {
        mAudioController.scrollAudio(distX);
    }

    @Override
    public void onLocationUpdated(int location){
        mWaveformFragment.onLocationUpdated(mAudioController.getLocationInFrames());
        mFragmentTabbedWidget.onLocationChanged();
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
        int frame = mAudioController.getLocationInFrames();
        mAudioController.dropVerseMarker("Verse " + "1", frame);
        mWaveformFragment.addVerseMarker(mVersesLeft, frame);
        mMarkerCounterFragment.decrementVersesRemaining();
        mWaveformFragment.onLocationUpdated(frame);
        mVersesLeft--;
    }

    @Override
    public void onCueScroll(int id, float distX){
        mMarkerMediator.onCueScroll(id, distX);
    }

    @Override
    public boolean onDelegateMinimapDraw(Canvas canvas, Paint paint) {
        if (wavVis != null) {
            canvas.drawLines(wavVis.getMinimap(canvas.getHeight(), canvas.getWidth(), mAudioController.getDuration()), paint);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDelegateMinimapMarkerDraw(Canvas canvas, Paint location, Paint section, Paint verse) {
        float x = (getLocation()/(float)getDuration()) * canvas.getWidth();
        canvas.drawLine(x, 0, x, canvas.getHeight(), location);
        float start = (mAudioController.getLoopStart() / (float)mAudioController.getDurationInFrames()) * canvas.getWidth();
        float end = (mAudioController.getLoopEnd() / (float)mAudioController.getDurationInFrames()) * canvas.getWidth();
        canvas.drawLine(start, 0, start, canvas.getHeight(), section);
        canvas.drawLine(end, 0, end, canvas.getHeight(), section);
    }
}