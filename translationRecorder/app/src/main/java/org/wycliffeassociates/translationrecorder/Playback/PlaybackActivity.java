package org.wycliffeassociates.translationrecorder.Playback;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import org.wycliffeassociates.translationrecorder.AudioVisualization.WavVisualizer;
import org.wycliffeassociates.translationrecorder.FilesPage.ExitDialog;
import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.Playback.fragments.FragmentFileBar;
import org.wycliffeassociates.translationrecorder.Playback.fragments.FragmentPlaybackTools;
import org.wycliffeassociates.translationrecorder.Playback.fragments.FragmentTabbedWidget;
import org.wycliffeassociates.translationrecorder.Playback.fragments.MarkerCounterFragment;
import org.wycliffeassociates.translationrecorder.Playback.fragments.MarkerToolbarFragment;
import org.wycliffeassociates.translationrecorder.Playback.fragments.WaveformFragment;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.AudioEditDelegator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.AudioStateCallback;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.EditStateInformer;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MarkerMediator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaController;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.VerseMarkerModeToggler;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.ViewCreatedCallback;
import org.wycliffeassociates.translationrecorder.Playback.markers.MarkerHolder;
import org.wycliffeassociates.translationrecorder.Playback.overlays.MinimapLayer;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RatingDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingScreen;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.WavFileLoader;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.wav.WavCue;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableImageView;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableMarker;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by sarabiaj on 10/27/2016.
 */

public class PlaybackActivity extends Activity implements RatingDialog.DialogListener, MediaController,
        AudioStateCallback, AudioEditDelegator, EditStateInformer,
        ViewCreatedCallback, WaveformFragment.OnScrollDelegator, VerseMarkerModeToggler, MarkerToolbarFragment.OnMarkerPlacedListener,
        MinimapLayer.MinimapDrawDelegator, FragmentTabbedWidget.DelegateMinimapMarkerDraw, FragmentFileBar.RerecordCallback, FragmentFileBar.RatingCallback,
        FragmentFileBar.InsertCallback, DraggableImageView.OnMarkerMovementRequest {

    public enum MODE {
        EDIT,
        VERSE_MARKER
    }

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_WAV_FILE = "wavfile";
    private static final String KEY_CHAPTER = "key_chapter";
    private static final String KEY_UNIT = "key_unit";

    private volatile boolean isSaved = true;
    private boolean isPlaying = false;
    private MODE mode;

    private WavVisualizer wavVis;
    private WavFile mWavFile;
    private WavFileLoader wavFileLoader;
    private Project mProject;
    private int mChapter, mUnit, mRating, startVerse, endVerse, mTotalVerses;
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
    private DrawThread mDrawLoop;

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
        Logger.w(this.toString(), "onCreate");
    }

    private void initialize(Intent intent) {
        isSaved = true;
        parseIntent(intent);
        getVerseRange();
        mAudioController = new AudioVisualController(this, mWavFile, this);
        mMarkerMediator = new MarkerHolder(mAudioController, this, mFragmentPlaybackTools, mTotalVerses);
        initializeFragments();
        wavFileLoader = mAudioController.getWavLoader();
        mMarkerMediator.setMarkerButtons(mFragmentPlaybackTools);
        mode = MODE.EDIT;
    }

    public void startDrawThread() {
        if(mDrawLoop != null) {
            mDrawLoop.finish();
        }
        mDrawLoop = new DrawThread();
        Thread draw = new Thread(mDrawLoop);
        draw.start();
    }

    private void initializeMarkers() {
        List<WavCue> cues = mWavFile.getMetadata().getCuePoints();
        for (WavCue cue : cues) {
            mWaveformFragment.addVerseMarker(Integer.valueOf(cue.getLabel()), cue.getLocation());
        }
        if (cues.size() == 0) {
            mWaveformFragment.addVerseMarker(0, 0);
        }
    }

    private void parseIntent(Intent intent) {
        mWavFile = intent.getParcelableExtra(KEY_WAV_FILE);
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mUnit = intent.getIntExtra(KEY_UNIT, 1);
        mChapter = intent.getIntExtra(KEY_CHAPTER, 1);
        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
    }

    private void initializeFragments() {
        mFragmentContainerMapping = new HashMap<>();

        mFragmentPlaybackTools = FragmentPlaybackTools.newInstance();
        mFragmentContainerMapping.put(R.id.playback_tools_fragment_holder, mFragmentPlaybackTools);

        mFragmentTabbedWidget = FragmentTabbedWidget.newInstance(mMarkerMediator, mProject, FileNameExtractor.getNameWithoutTake(mWavFile.getFile().getName()), mChapter);
        mFragmentContainerMapping.put(R.id.tabbed_widget_fragment_holder, mFragmentTabbedWidget);

        mFragmentFileBar = FragmentFileBar.newInstance(mProject.getTargetLanguage(),
                mProject.getVersion(), mProject.getSlug(), "Chapter", String.valueOf(mChapter),
                mProject.getMode(),
                getUnitLabel());

        mFragmentContainerMapping.put(R.id.file_bar_fragment_holder, mFragmentFileBar);

        mWaveformFragment = WaveformFragment.newInstance(mMarkerMediator);
        mFragmentContainerMapping.put(R.id.waveform_fragment_holder, mWaveformFragment);

        mMarkerCounterFragment = MarkerCounterFragment.newInstance(mMarkerMediator);
        mMarkerToolbarFragment = MarkerToolbarFragment.newInstance();
        attachFragments();
    }

    @Override
    public void finish() {
        super.finish();
        mDrawLoop.finish();
        if(mAudioController.isPlaying()) {
            mAudioController.pause();
        }
    }

    private String getUnitLabel() {
        if (mProject.getMode().equals("chunk")) {
            FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
            return String.format("%d-%d", fne.getStartVerse(), fne.getEndVerse());
        } else {
            return String.valueOf(mUnit);
        }
    }

    private void attachFragments() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Set<Map.Entry<Integer, Fragment>> entrySet = mFragmentContainerMapping.entrySet();
        for (Map.Entry<Integer, Fragment> pair : entrySet) {
            ft.add(pair.getKey(), pair.getValue());
        }
        ft.commit();
    }

    @Override
    public void onMediaPause() {
        mAudioController.pause();
    }

    @Override
    public void onMediaPlay() {
        mAudioController.play();
    }

    @Override
    public void onSeekForward() {
        mAudioController.seekNext();
        mWaveformFragment.invalidateFrame(mAudioController.getRelativeLocationInFrames(), mAudioController.getAbsoluteLocationMs());
    }

    @Override
    public void onSeekBackward() {
        mAudioController.seekPrevious();
        mWaveformFragment.invalidateFrame(mAudioController.getRelativeLocationInFrames(), mAudioController.getAbsoluteLocationMs());
    }

    @Override
    public void onSeekTo(float x) {
        mAudioController.seekTo(mAudioController.mCutOp.relativeLocToAbsolute((int) (x * mAudioController.getRelativeDurationInFrames()), false));
        onLocationUpdated();
    }

    @Override
    public int getDurationMs() {
        return mAudioController.getRelativeDurationMs();
    }

    @Override
    public int getLocationMs() {
        return mAudioController.getRelativeLocationMs();
    }

    @Override
    public int getLocationInFrames() {
        return mAudioController.getRelativeLocationInFrames();
    }

    @Override
    public int getDurationInFrames() {
        return mAudioController.getRelativeDurationInFrames();
    }

    @Override
    public void setOnCompleteListner(Runnable onComplete) {
        //mAudioController.setOnCompleteListener(onComplete);
    }

    @Override
    public int getStartMarkerFrame() {
        return mAudioController.mCutOp.absoluteLocToRelative(mAudioController.getLoopStart(), false);
    }

    @Override
    public int getEndMarkerFrame() {
        return mAudioController.mCutOp.absoluteLocToRelative(mAudioController.getLoopEnd(), false);
    }

    @Override
    public void onPlayerPaused() {
        mFragmentPlaybackTools.onPlayerPaused();
        mMarkerToolbarFragment.showPlayButton();
        onLocationUpdated();
    }

    @Override
    public void onSave() {
        save(null);
    }

    @Override
    public void onCut() {
        isSaved = false;
        mAudioController.cut();
        mFragmentPlaybackTools.onLocationUpdated(mAudioController.getAbsoluteLocationMs());
        mFragmentPlaybackTools.onDurationUpdated(mAudioController.getRelativeDurationMs());
        onClearMarkers();
        mFragmentTabbedWidget.invalidateMinimap();
        mFragmentTabbedWidget.onLocationChanged();
    }

    @Override
    public void onDropStartMarker() {
        mAudioController.dropStartMarker();
        int location = mAudioController.getLoopStart();
        mWaveformFragment.addStartMarker(mAudioController.mCutOp.absoluteLocToRelative(location, false));
        onLocationUpdated();
    }

    @Override
    public void onDropEndMarker() {
        mAudioController.dropEndMarker();
        int location = mAudioController.getLoopEnd();
        mWaveformFragment.addEndMarker(mAudioController.mCutOp.absoluteLocToRelative(location, false));
        onLocationUpdated();
    }

    @Override
    public void setStartMarkerAt(int frameRelative) {
        mAudioController.setStartMarker(frameRelative);
        mWaveformFragment.addStartMarker(frameRelative);
        onLocationUpdated();
    }

    @Override
    public void setEndMarkerAt(int frame) {
        mAudioController.setEndMarker(frame);
        mWaveformFragment.addEndMarker(frame);
        onLocationUpdated();
    }

    @Override
    public void onClearMarkers() {
        mMarkerMediator.onRemoveSectionMarkers();
    }

    @Override
    public boolean hasSetMarkers() {
        if (mMarkerMediator.hasSectionMarkers()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPlaying() {
        return mAudioController.isPlaying();
    }

    @Override
    public void onDropVerseMarker() {

    }

    @Override
    public void onUndo() {
        mAudioController.undo();
        if (!mAudioController.mCutOp.hasCut()) {
            isSaved = true;
        }
        mFragmentTabbedWidget.invalidateMinimap();
        onLocationUpdated();
    }

    @Override
    public boolean hasEdits() {
        return mAudioController.mCutOp.hasCut();
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        Logger.w(this.toString(), "rating set");
        mRating = dialog.getRating();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), mRating);
        db.close();
        mFragmentFileBar.onRatingChanged(mRating);
    }

    @Override
    public void onNegativeClick(RatingDialog dialog) {
        Logger.w(this.toString(), "rating canceled");
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Logger.w(this.toString(), "Back was pressed.");
        if (mode == MODE.VERSE_MARKER) {
            onDisableVerseMarkerMode();
        } else if (actionsToSave()) {
            Logger.i(this.toString(), "Asking if user wants to save before going back");
            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_AppCompat_Light_Dialog, true, isPlaying, mWavFile.getFile());
            exit.show();
        } else {
//            clearLoopPoints();
            super.onBackPressed();
        }
    }

    public boolean actionsToSave(){
        boolean cuts = mAudioController.mCutOp.hasCut();
        int markersOriginally = Math.max(mWavFile.getMetadata().getCuePoints().size(), 1);
        int markersNow = mMarkerMediator.numVerseMarkersPlaced();
        boolean markersPlaced = markersNow > markersOriginally;
        return cuts || markersPlaced;
    }

    public void onOpenRating(FourStepImageView v) {
        Logger.w(this.toString(), "Rating dialog opened");
        RatingDialog dialog = RatingDialog.newInstance(mWavFile.getFile().getName(), mRating);
        dialog.show(getFragmentManager(), "single_unit_rating");
    }

    public void onRerecord() {
        Intent intent = RecordingScreen.getRerecordIntent(this, mProject, mWavFile, mChapter, mUnit);
        save(intent);
    }

    private void writeMarkers(WavFile wav) {
        Collection<DraggableMarker> markers = mMarkerMediator.getMarkers();
        ArrayList<DraggableMarker> markersList = new ArrayList<>(markers);
        Collections.sort(markersList, new Comparator<DraggableMarker>() {
            @Override
            public int compare(DraggableMarker lhs, DraggableMarker rhs) {
                return Integer.compare(lhs.getFrame(), rhs.getFrame());
            }
        });
        int i = 0;
        for (DraggableMarker m : markersList) {
            wav.addMarker(String.valueOf(startVerse + i), m.getFrame());
            i++;
        }
        wav.commit();
    }

    private void save(Intent intent) {
        writeMarkers(mWavFile);
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

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Saving");
        pd.setMessage("Writing changes to file, please wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgressNumberFormat(null);
        pd.setCancelable(false);
        pd.show();
        Thread saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mAudioController.mCutOp.hasCut()) {
                    try {
                        File dir = Project.getProjectDirectory(mProject);
                        File toTemp = new File(dir, "temp.wav");
                        WavFile toTempWav = new WavFile(toTemp, from.getMetadata());
                        mAudioController.mCutOp.writeCut(toTempWav, wavFileLoader.getMappedAudioFile().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer(), pd);
                        writeMarkers(toTempWav);
                        to.delete();
                        toTemp.renameTo(to);
                        ProjectDatabaseHelper db = new ProjectDatabaseHelper(PlaybackActivity.this);
                        db.addTake(new FileNameExtractor(to), to.getName(), to.lastModified(), 0);
                        db.close();
                        String oldName = from.getFile().getName();
                        oldName = oldName.substring(0, oldName.lastIndexOf("."));
                        File visDir = new File(getExternalCacheDir(), "Visualization");
                        File toVis = new File(visDir, oldName + ".vis");
                        toVis.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isSaved = true;
                try {
                    pd.dismiss();
                } catch (IllegalArgumentException e) {
                    Logger.e("PlaybackActivity", "Tried to dismiss cut dialog", e);
                }
                if (intent == null) {
                    finish();
                } else {
                    WavFile result = new WavFile(to);
                    intent.putExtra(RecordingScreen.KEY_WAV_FILE, result);
                    startActivity(intent);
                    finish();
                }
            }
        });
        saveThread.start();
    }

    public void onInsert() {
        Intent insertIntent = RecordingScreen.getInsertIntent(this, mProject, mWavFile, mChapter, mUnit, mAudioController.getAbsoluteLocationMs());
        save(insertIntent);
    }

    private boolean allVersesMarked() {
        return mMarkerMediator.hasVersesRemaining();
    }

    private void getVerseRange() {
        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
        mTotalVerses = (fne.getEndVerse() - fne.getStartVerse() + 1);
        startVerse = fne.getStartVerse();
        endVerse = fne.getEndVerse();
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
    public void onViewCreated(Fragment ref) {
        if (ref instanceof WaveformFragment) {
            View view = mWaveformFragment.getView();
            ViewTreeObserver vto = view.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mWaveformInflated = true;
                    if (mWaveformInflated && mMinimapInflated) {
                        initializeRenderer();
                        initializeMarkers();
                        mWaveformFragment.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startDrawThread();
                    }
                }
            });
        } else if (ref instanceof FragmentTabbedWidget) {
            View view = mFragmentTabbedWidget.getView();
            ViewTreeObserver vto = view.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMinimapInflated = true;
                    if (mWaveformInflated && mMinimapInflated) {
                        initializeRenderer();
                        initializeMarkers();
                        mFragmentTabbedWidget.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startDrawThread();
                    }
                }
            });
        }
    }

    private void initializeRenderer() {
        ShortBuffer uncompressed = wavFileLoader.getMappedFile().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        ShortBuffer compressed = (wavFileLoader.getMappedCacheFile() != null) ? wavFileLoader.getMappedCacheFile().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer() : null;
        wavVis = new WavVisualizer(uncompressed, compressed, mWaveformFragment.getView().getWidth(), mWaveformFragment.getView().getHeight(), mFragmentTabbedWidget.getWidgetWidth(), mAudioController.getCutOp());
        mWaveformFragment.setWavRenderer(wavVis);
        mFragmentTabbedWidget.initializeTimecode(mAudioController.getRelativeDurationMs());
    }

    @Override
    public void delegateOnScroll(float distX) {
        mAudioController.scrollAudio(distX);
    }

    @Override
    public void onLocationUpdated() {
        int frame = mAudioController.getRelativeLocationInFrames();
        int absoluteMs = mAudioController.getAbsoluteLocationMs();

        mWaveformFragment.invalidateFrame(frame, absoluteMs);

//                //// TODO
//                mFragmentTabbedWidget.invalidateFrame(frame);
//                mFragmentPlaybackTools.invalidateMs(ms);
//                mMarkerToolbarFragment.invalidateMs(ms);

        mFragmentPlaybackTools.onLocationUpdated(mAudioController.getRelativeLocationMs());
        mFragmentTabbedWidget.onLocationChanged();
        mMarkerToolbarFragment.onLocationUpdated(mAudioController.getRelativeLocationMs());
    }

    @Override
    public void onVisualizationLoaded(final MappedByteBuffer mappedVisualizationFile) {
        if(wavVis == null) {
            //delay the call if the visualizer hasn't loaded yet
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onVisualizationLoaded(mappedVisualizationFile);
                }
            }, 1000);
        } else {
            wavVis.enableCompressedFileNextDraw(mappedVisualizationFile.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer());
        }
    }

    @Override
    public void onEnableVerseMarkerMode() {
        Logger.w(this.toString(), "onEnableVerseMarkerMode");
        onClearMarkers();
        //if (mMarkerMediator.hasVersesRemaining()) {
            mode = MODE.VERSE_MARKER;
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction()
                    .remove(mFragmentFileBar)
                    .add(R.id.file_bar_fragment_holder, mMarkerCounterFragment)
                    .remove(mFragmentPlaybackTools)
                    .add(R.id.playback_tools_fragment_holder, mMarkerToolbarFragment)
                    .commit();
        //}
        onLocationUpdated();
    }

    @Override
    public void onDisableVerseMarkerMode() {
        Logger.w(this.toString(), "onDisableVerseMarkerMode");
        mode = MODE.EDIT;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .remove(mMarkerCounterFragment)
                .add(R.id.file_bar_fragment_holder, mFragmentFileBar)
                .remove(mMarkerToolbarFragment)
                .add(R.id.playback_tools_fragment_holder, mFragmentPlaybackTools)
                .commit();
        onLocationUpdated();
    }

    @Override
    public void onMarkerPlaced() {
        if(mMarkerMediator.hasVersesRemaining()) {
            Logger.w(this.toString(), "Placed verse marker");
            int frame = mAudioController.getRelativeLocationInFrames();
            int markerNumber = (startVerse + 1) + (endVerse - startVerse - mMarkerMediator.numVersesRemaining());
            mAudioController.dropVerseMarker("Verse " + markerNumber, frame);
            mWaveformFragment.addVerseMarker(markerNumber, frame);
            mMarkerCounterFragment.decrementVersesRemaining();
            mWaveformFragment.invalidateFrame(mAudioController.getRelativeLocationInFrames(), mAudioController.getAbsoluteLocationMs());
        }
    }

    @Override
    public void onCueScroll(int id, float distX) {
        mMarkerMediator.onCueScroll(id, distX);
    }

    @Override
    public boolean onDelegateMinimapDraw(Canvas canvas, Paint paint) {
        if (wavVis != null) {
            canvas.drawLines(wavVis.getMinimap(canvas.getHeight(), canvas.getWidth(), mAudioController.getRelativeDurationMs()), paint);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDelegateMinimapMarkerDraw(Canvas canvas, Paint location, Paint section, Paint verse) {
        float x = (getLocationInFrames() / (float) getDurationInFrames()) * canvas.getWidth();
        canvas.drawLine(x, 0, x, canvas.getHeight(), location);
        float start = ((getStartMarkerFrame()) / (float) getDurationInFrames()) * canvas.getWidth();
        float end = ((getEndMarkerFrame()) / (float) getDurationInFrames()) * canvas.getWidth();
        Collection<DraggableMarker> markers = mMarkerMediator.getMarkers();
        for (DraggableMarker m : markers) {
            float markerPos = ((m.getFrame()) / (float) getDurationInFrames()) * canvas.getWidth();
            canvas.drawLine(markerPos, 0, markerPos, canvas.getHeight(), verse);
        }
        canvas.drawLine(start, 0, start, canvas.getHeight(), section);
        canvas.drawLine(end, 0, end, canvas.getHeight(), section);
    }

    @Override
    public boolean onMarkerMovementRequest(int markerId) {
        if(mode == MODE.EDIT && (markerId == MarkerHolder.END_MARKER_ID || markerId == MarkerHolder.START_MARKER_ID)){
            return true;
        } else if (mode == MODE.VERSE_MARKER && markerId != MarkerHolder.START_MARKER_ID && markerId != MarkerHolder.END_MARKER_ID) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInVerseMarkerMode(){
        return mode == MODE.VERSE_MARKER;
    }

    public boolean isInEditMode(){
        return mode == MODE.EDIT;
    }

    private class DrawThread implements Runnable {

        private volatile boolean finished = false;

        public DrawThread() {
        }

        public void finish() {
            finished = true;
        }

        @Override
        public void run() {

            while (!finished) {
                if(mAudioController != null && mAudioController.isPlaying()) {
                   onLocationUpdated();
                }
                try {
                    Thread.sleep(45);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}