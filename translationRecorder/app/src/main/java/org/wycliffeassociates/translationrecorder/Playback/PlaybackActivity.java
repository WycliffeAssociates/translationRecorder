package org.wycliffeassociates.translationrecorder.Playback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.AudioVisualization.WavVisualizer;
import org.wycliffeassociates.translationrecorder.FilesPage.ExitDialog;
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
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RatingDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.WavFileLoader;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.project.components.User;
import org.wycliffeassociates.translationrecorder.wav.WavCue;
import org.wycliffeassociates.translationrecorder.wav.WavFile;
import org.wycliffeassociates.translationrecorder.widgets.FourStepImageView;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableImageView;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarkerView;

import java.io.File;
import java.io.IOException;
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

public class PlaybackActivity extends Activity implements
        RatingDialog.DialogListener,
        MediaController,
        AudioStateCallback,
        AudioEditDelegator,
        EditStateInformer,
        ViewCreatedCallback,
        WaveformFragment.OnScrollDelegator,
        VerseMarkerModeToggler,
        MarkerToolbarFragment.OnMarkerPlacedListener,
        MinimapLayer.MinimapDrawDelegator,
        FragmentTabbedWidget.DelegateMinimapMarkerDraw,
        FragmentFileBar.RerecordCallback,
        FragmentFileBar.RatingCallback,
        FragmentFileBar.InsertCallback,
        DraggableImageView.OnMarkerMovementRequest,
        ExitDialog.DeleteFileCallback
{

    public enum MODE {
        EDIT,
        VERSE_MARKER
    }

    public static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static final String KEY_PROJECT = "key_project";
    public static final String KEY_WAV_FILE = "wavfile";
    public static final String KEY_CHAPTER = "key_chapter";
    public static final String KEY_UNIT = "key_unit";

    private volatile boolean isSaved = true;
    private boolean isPlaying = false;
    private boolean shouldResume = false;
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
    private User mUser;
    private AudioTrack audioTrack;
    private int trackBufferSize = 0;

    public static Intent getPlaybackIntent(
            Context ctx,
            WavFile file,
            Project project,
            int chapter,
            int unit
    ) {
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
        audioTrack = ((TranslationRecorderApp)getApplication()).getAudioTrack();
        trackBufferSize = ((TranslationRecorderApp)getApplication()).getTrackBufferSize();
        try {
            initialize(getIntent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Logger.w(this.toString(), "onCreate");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int userId = pref.getInt(Settings.KEY_USER, 1);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        mUser = db.getUser(userId);
    }

    private void initialize(Intent intent) throws IOException {
        isSaved = true;
        parseIntent(intent);
        getVerseRange();
        try {
            mAudioController = new AudioVisualController(audioTrack, trackBufferSize, this, mWavFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMarkerMediator = new MarkerHolder(
                mAudioController,
                this,
                mFragmentPlaybackTools,
                mTotalVerses
        );
        initializeFragments();
        wavFileLoader = mAudioController.getWavLoader();
        mMarkerMediator.setMarkerButtons(mFragmentPlaybackTools);
        mode = MODE.EDIT;
    }

    public void startDrawThread() {
        if (mDrawLoop != null) {
            mDrawLoop.finish();
        }
        mDrawLoop = new DrawThread();
        Thread draw = new Thread(mDrawLoop);
        draw.start();
    }

    private void initializeMarkers() {
        List<WavCue> cues = mWavFile.getMetadata().getCuePoints();
        for (WavCue cue : cues) {
            mWaveformFragment.addVerseMarker(
                    Integer.valueOf(cue.getLabel()),
                    cue.getLocation()
            );
        }
        if (cues.size() == 0) {
            mWaveformFragment.addVerseMarker(0, 0);
        }
    }

    private void parseIntent(Intent intent) {
        mWavFile = intent.getParcelableExtra(KEY_WAV_FILE);
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mChapter = intent.getIntExtra(KEY_CHAPTER, ChunkPlugin.DEFAULT_CHAPTER);
        mUnit = intent.getIntExtra(KEY_UNIT, ChunkPlugin.DEFAULT_UNIT);
    }

    private void initializeFragments() throws IOException {
        ChunkPlugin plugin = mProject.getChunkPlugin(new ChunkPluginLoader(this));
        plugin.initialize(mChapter, mUnit);

        mFragmentContainerMapping = new HashMap<>();

        mFragmentPlaybackTools = FragmentPlaybackTools.newInstance();
        mFragmentContainerMapping.put(
                R.id.playback_tools_fragment_holder,
                mFragmentPlaybackTools
        );

        mFragmentTabbedWidget = FragmentTabbedWidget.newInstance(
                mMarkerMediator,
                mProject,
                ProjectFileUtils.getNameWithoutTake(mWavFile.getFile().getName()),
                mChapter
        );
        mFragmentContainerMapping.put(
                R.id.tabbed_widget_fragment_holder,
                mFragmentTabbedWidget
        );

        mFragmentFileBar = FragmentFileBar.newInstance(
                mProject.getTargetLanguageSlug(),
                mProject.getVersionSlug(),
                mProject.getBookSlug(),
                Utils.capitalizeFirstLetter(plugin.getChapterLabel()),
                plugin.getChapterName(mChapter),
                mProject.getModeName(),
                //getUnitLabel(),
                plugin.getChunkName(),
                mProject.getModeType()
        );

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
        if(mDrawLoop != null) {
            mDrawLoop.finish();
        }
        if (mAudioController.isPlaying()) {
            mAudioController.pause();
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
        try {
            mAudioController.play();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    public void requestUserToRestart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Could not initialize the audio player");
        builder.setMessage("If this issue continues, try restarting your device.");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onSeekForward() {
        try {
            mAudioController.seekNext();
            //mWaveformFragment.invalidateFrame(mAudioController.getAbsoluteLocationInFrames(), mAudioController.getRelativeLocationInFrames(), mAudioController.getAbsoluteLocationMs());
            onLocationUpdated();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    @Override
    public void onSeekBackward() throws IllegalStateException {
        try {
            mAudioController.seekPrevious();
            //mWaveformFragment.invalidateFrame(mAudioController.getAbsoluteLocationInFrames(), mAudioController.getRelativeLocationInFrames(), mAudioController.getAbsoluteLocationMs());
            onLocationUpdated();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    @Override
    public void onSeekTo(float x) {
        try {
            mAudioController.seekTo(
                    mAudioController.mCutOp.relativeLocToAbsolute(
                            (int) (x * mAudioController.getRelativeDurationInFrames()),
                            false
                    )
            );
            onLocationUpdated();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    @Override
    public int getDurationMs() {
        return mAudioController.getRelativeDurationMs();
    }

    @Override
    public int getLocationMs() throws IllegalStateException {
        try {
            return mAudioController.getRelativeLocationMs();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
        return 0;
    }

    @Override
    public int getLocationInFrames() {
        try {
            return mAudioController.getRelativeLocationInFrames();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
        return 0;
    }

    @Override
    public int getDurationInFrames() {
        try {
            return mAudioController.getRelativeDurationInFrames();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
        return 0;
    }

    @Override
    public void setOnCompleteListner(Runnable onComplete) {
        //mAudioController.setOnCompleteListener(onComplete);
    }

    @Override
    public int getStartMarkerFrame() {
        return mAudioController.mCutOp.absoluteLocToRelative(
                mAudioController.getLoopStart(),
                false
        );
    }

    @Override
    public int getEndMarkerFrame() {
        return mAudioController.mCutOp.absoluteLocToRelative(
                mAudioController.getLoopEnd(),
                false
        );
    }

    @Override
    public void onPlayerPaused() {
        mFragmentPlaybackTools.onPlayerPaused();
        mMarkerToolbarFragment.showPlayButton();
        onLocationUpdated();
    }

    @Override
    public void onDeleteRecording() {
        super.onBackPressed();
    }

    @Override
    public void onSave() {
        save(null);
    }

    @Override
    public synchronized void onCut() {
        isSaved = false;
        Collection<DraggableMarker> markers = mMarkerMediator.getMarkers();
        List<DraggableMarker> markerList = new ArrayList<>(markers);
        long relativeLoopStart = mAudioController.mCutOp.absoluteLocToRelative(
                mAudioController.getLoopStart(),
                false
        );
        long relativeLoopEnd = mAudioController.mCutOp.absoluteLocToRelative(
                mAudioController.getLoopEnd(),
                false
        );

        for (int i = 0; i < markerList.size(); i++) {
            DraggableMarker marker = markerList.get(i);
            if (marker.getFrame() <= relativeLoopEnd && marker.getFrame() > relativeLoopStart) {
                if (marker instanceof VerseMarker) {
                    //iter.remove();
                    mMarkerMediator.onRemoveVerseMarker(
                            ((VerseMarkerView) marker.getView()).getMarkerId()
                    );
                }
            } else {
                marker.updateFrame(
                        mAudioController.mCutOp.relativeLocToAbsolute(
                                marker.getFrame(),
                                false
                        )
                );
            }
        }
        mAudioController.cut();
        for (DraggableMarker marker : markers) {
            marker.updateFrame(
                    mAudioController.mCutOp.absoluteLocToRelative(
                            marker.getFrame(),
                            false
                    )
            );
        }
        try {
            mFragmentPlaybackTools.onLocationUpdated(mAudioController.getAbsoluteLocationMs());
            mFragmentPlaybackTools.onDurationUpdated(mAudioController.getRelativeDurationMs());
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
        onClearMarkers();
        mFragmentTabbedWidget.invalidateMinimap();
        mFragmentTabbedWidget.onLocationChanged();
    }

    @Override
    public void onDropStartMarker() {
        try {
            mAudioController.dropStartMarker();
            int location = mAudioController.getLoopStart();
            mWaveformFragment.addStartMarker(
                    mAudioController.mCutOp.absoluteLocToRelative(
                            location,
                            false
                    )
            );
            onLocationUpdated();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    @Override
    public void onDropEndMarker() {
        try {
            mAudioController.dropEndMarker();
            int location = mAudioController.getLoopEnd();
            mWaveformFragment.addEndMarker(
                    mAudioController.mCutOp.absoluteLocToRelative(
                            location,
                            false
                    )
            );
            onLocationUpdated();
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
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
        Collection<DraggableMarker> markers = mMarkerMediator.getMarkers();
        //map markers back to absolute before
        for (DraggableMarker marker : markers) {
            marker.updateFrame(
                    mAudioController.mCutOp.relativeLocToAbsolute(
                            marker.getFrame(),
                            false
                    )
            );
        }
        mAudioController.undo();
        for (DraggableMarker marker : markers) {
            marker.updateFrame(
                    mAudioController.mCutOp.absoluteLocToRelative(
                            marker.getFrame(),
                            false
                    )
            );
        }
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

        db.setTakeRating(dialog.getTakeInfo(), mRating);
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
            //keep file needs to be false so the callback will go through and the super.onBackPressed is called
            ExitDialog exit = ExitDialog.Build(
                    this,
                    R.style.Theme_AppCompat_Light_Dialog,
                    false,
                    isPlaying,
                    mWavFile.getFile()
            );
            exit.show();
        } else {
            super.onBackPressed();
        }
    }


    public boolean actionsToSave() {
        boolean cuts = mAudioController.mCutOp.hasCut();
        int markersOriginally = Math.max(mWavFile.getMetadata().getCuePoints().size(), 1);
        int markersNow = mMarkerMediator.numVerseMarkersPlaced();
        boolean markersPlaced = markersNow > markersOriginally;
        return cuts || markersPlaced;
    }

    public void onOpenRating(FourStepImageView v) {
        Logger.w(this.toString(), "Rating dialog opened");
        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
        ppm.match(mWavFile.getFile());
        RatingDialog dialog = RatingDialog.newInstance(ppm.getTakeInfo(), mRating);
        dialog.show(getFragmentManager(), "single_unit_rating");
    }

    public void onRerecord() {
        Intent intent = RecordingActivity.getRerecordIntent(
                this,
                mProject,
                mWavFile,
                mChapter,
                mUnit
        );
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
            if(m instanceof VerseMarker) {
                wav.addMarker(String.valueOf(startVerse + i), m.getFrame());
            }
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

        File dir = new File(
                ProjectFileUtils.getProjectDirectory(mProject),
                ProjectFileUtils.chapterIntToString(mProject, mChapter)
        );
        File from = mWavFile.getFile();
        int takeInt = ProjectFileUtils.getLargestTake(mProject, dir, from) + 1;
        String take = String.format("%02d", takeInt);
        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
        ppm.match(from);
        TakeInfo takeInfo = ppm.getTakeInfo();
        File to = new File(
                dir,
                ProjectFileUtils.getNameWithoutTake(from)
                        + "_t"
                        + take
                        + AUDIO_RECORDER_FILE_EXT_WAV
        );
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
                        File dir = ProjectFileUtils.getProjectDirectory(mProject);
                        File toTemp = new File(dir, "temp.wav");
                        WavFile toTempWav = new WavFile(toTemp, from.getMetadata());
                        mAudioController.mCutOp.writeCut(
                                toTempWav,
                                wavFileLoader.mapAndGetAudioBuffer(),
                                pd
                        );
                        writeMarkers(toTempWav);
                        to.delete();
                        toTemp.renameTo(to);
                        ProjectDatabaseHelper db = new ProjectDatabaseHelper(PlaybackActivity.this);
                        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
                        ppm.match(to);
                        db.addTake(
                                ppm.getTakeInfo(),
                                to.getName(),
                                from.getMetadata().getModeSlug(),
                                to.lastModified(),
                                0,
                                mUser.getId()
                        );
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
                    intent.putExtra(RecordingActivity.KEY_WAV_FILE, result);
                    startActivity(intent);
                    finish();
                }
            }
        });
        saveThread.start();
    }

    public void onInsert() {
        Intent insertIntent = RecordingActivity.getInsertIntent(
                this,
                mProject,
                mWavFile,
                mChapter,
                mUnit,
                mAudioController.getRelativeLocationInFrames()
        );
        save(insertIntent);
    }

    private boolean allVersesMarked() {
        return mMarkerMediator.hasVersesRemaining();
    }

    private void getVerseRange() {
        ProjectPatternMatcher ppm = mProject.getPatternMatcher();
        ppm.match(mWavFile.getFile());
        TakeInfo takeInfo = ppm.getTakeInfo();
        mTotalVerses = (takeInfo.getEndVerse() - takeInfo.getStartVerse() + 1);
        startVerse = takeInfo.getStartVerse();
        endVerse = takeInfo.getEndVerse();
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
                        mWaveformFragment.getView()
                                .getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
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
                        mFragmentTabbedWidget.getView()
                                .getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        startDrawThread();
                    }
                }
            });
        }
    }

    private void initializeRenderer() {
        try {
            int numThreads = 4;
            ShortBuffer uncompressed = wavFileLoader.mapAndGetAudioBuffer();
            ShortBuffer compressed = wavFileLoader.mapAndGetVisualizationBuffer();
            wavVis = new WavVisualizer(
                    uncompressed,
                    compressed,
                    numThreads,
                    mWaveformFragment.getView().getWidth(),
                    mWaveformFragment.getView().getHeight(),
                    mFragmentTabbedWidget.getWidgetWidth(),
                    mAudioController.getCutOp()
            );
            mWaveformFragment.setWavRenderer(wavVis);
            mFragmentTabbedWidget.initializeTimecode(mAudioController.getRelativeDurationMs());
        } catch (IOException e) {

        }
    }

    @Override
    public void delegateOnScroll(float distX) {
        if(mAudioController.isPlaying()) {
            shouldResume = true;
            mAudioController.pause();
        }
        mAudioController.scrollAudio(distX);
    }

    @Override
    public void delegateOnScrollComplete() {
        if(shouldResume) {
            shouldResume = false;
            mAudioController.play();
        }
    }

    @Override
    public void onLocationUpdated() {
        try {
            int absoluteFrame = mAudioController.getAbsoluteLocationInFrames();
            int relativeFrame = mAudioController.getRelativeLocationInFrames();
            int absoluteMs = mAudioController.getAbsoluteLocationMs();

            mWaveformFragment.invalidateFrame(absoluteFrame, relativeFrame, absoluteMs);

//                //// TODO
//                mFragmentTabbedWidget.invalidateFrame(frame);
//                mFragmentPlaybackTools.invalidateMs(ms);
//                mMarkerToolbarFragment.invalidateMs(ms);

            mFragmentPlaybackTools.onLocationUpdated(mAudioController.getRelativeLocationMs());
            mFragmentTabbedWidget.onLocationChanged();
            mMarkerToolbarFragment.onLocationUpdated(mAudioController.getRelativeLocationMs());
        } catch (IllegalStateException e) {
            requestUserToRestart();
        }
    }

    @Override
    public void onVisualizationLoaded(final ShortBuffer mappedVisualizationFile) {
        Handler handler = new Handler(Looper.getMainLooper());
        if (wavVis == null) {
            //delay the call if the visualizer hasn't loaded yet
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onVisualizationLoaded(mappedVisualizationFile);
                }
            }, 1000);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    wavVis.enableCompressedFileNextDraw(mappedVisualizationFile);
                    mFragmentTabbedWidget.invalidateMinimap();
                }
            });
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
        if (mMarkerMediator.hasVersesRemaining()) {
            Logger.w(this.toString(), "Placed verse marker");
            int frame = mAudioController.getRelativeLocationInFrames();
            int markerNumber = (startVerse + 1) + (endVerse - startVerse - mMarkerMediator.numVersesRemaining());
            mAudioController.dropVerseMarker(String.valueOf(markerNumber), frame);
            mWaveformFragment.addVerseMarker(markerNumber, frame);
            mMarkerCounterFragment.decrementVersesRemaining();
            try {
                mWaveformFragment.invalidateFrame(
                        mAudioController.getAbsoluteLocationInFrames(),
                        mAudioController.getRelativeLocationInFrames(),
                        mAudioController.getAbsoluteLocationMs()
                );
            } catch (IllegalStateException e) {
                requestUserToRestart();
            }
        }
    }

    @Override
    public void onCueScroll(int id, float distX) {
        mMarkerMediator.onCueScroll(id, distX);
    }

    @Override
    public boolean onDelegateMinimapDraw(Canvas canvas, Paint paint) {
        if (wavVis != null) {
            canvas.drawLines(
                    wavVis.getMinimap(
                            canvas.getHeight(),
                            canvas.getWidth(),
                            mAudioController.getRelativeDurationInFrames()
                    ),
                    paint
            );
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDelegateMinimapMarkerDraw(
            Canvas canvas,
            Paint location,
            Paint section,
            Paint verse
    ) {
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
        if (mode == MODE.EDIT && (markerId == MarkerHolder.END_MARKER_ID || markerId == MarkerHolder.START_MARKER_ID)) {
            return true;
        } else if (mode == MODE.VERSE_MARKER && markerId != MarkerHolder.START_MARKER_ID && markerId != MarkerHolder.END_MARKER_ID) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInVerseMarkerMode() {
        return mode == MODE.VERSE_MARKER;
    }

    public boolean isInEditMode() {
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
                if (mAudioController != null && mAudioController.isPlaying()) {
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