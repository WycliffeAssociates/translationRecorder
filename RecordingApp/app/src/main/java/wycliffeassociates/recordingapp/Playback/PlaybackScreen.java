package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.SectionMarkers;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.FilesPage.ExitDialog;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.RatingDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Recording.WavFile;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by sarabiaj on 11/10/2015.
 */
public class PlaybackScreen extends Activity implements RatingDialog.DialogListener {

    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_WAV_FILE = "wavfile";
    private static final String KEY_CHAPTER = "key_chapter";
    private static final String KEY_UNIT = "key_unit";

    private UIDataManager mManager;

    private volatile boolean isSaved = true;
    private boolean isPlaying = false;
    private boolean isInVMarkerMode = false;

    private WaveformView mMainCanvas;
    private RelativeLayout mToolbar;
    private MinimapView minimap;
    private View mSrcAudioPlayback;
    private MarkerView mStartMarker, mEndMarker;
    private TextView mLangView, mSourceView, mBookView, mChapterView, mChapterLabel, mUnitView,
            mUnitLabel;
    private ImageButton mSwitchToMinimap, mSwitchToPlayback, mEnterVMarkerMode, mExitVMarkerMode,
            mRerecordBtn, mInsertBtn, mPlayBtn, mPauseBtn, mSkipBackBtn, mSkipForwardBtn,
            mDropStartMarkBtn, mDropEndMarkBtn, mUndoBtn, mCutBtn, mClearBtn, mSaveBtn;
    private FourStepImageView mRateBtn;

    private SourceAudio mSrcPlayer;
    private WavFile mWavFile;
    private Project mProject;
    private int mChapter, mUnit, mRating;

    public static Intent getPlaybackIntent(Context ctx, WavFile file, Project project, int chapter, int unit) {
        Intent intent = new Intent(ctx, PlaybackScreen.class);
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
        setContentView(R.layout.playback_screen);
        initialize(getIntent());
    }

    private void initialize(Intent intent) {
        isSaved = true;
        parseIntent(intent);
        findViews();
        initializeViews();
        setButtonHandlers();
        enableButtons();
        mSrcPlayer.initSrcAudio(mProject, FileNameExtractor.getNameWithoutTake(mWavFile.getFile().getName()), mChapter);
        initializeController();
    }

    private void parseIntent(Intent intent) {
        mWavFile = intent.getParcelableExtra(KEY_WAV_FILE);
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mUnit = intent.getIntExtra(KEY_UNIT, 1);
        mChapter = intent.getIntExtra(KEY_CHAPTER, 1);
    }

    private void findViews() {
        mMainCanvas = (WaveformView) findViewById(R.id.main_canvas);
        mToolbar = (RelativeLayout) findViewById(R.id.toolbar);
        minimap = (MinimapView) findViewById(R.id.minimap);
        mSrcAudioPlayback = (View) findViewById(R.id.srcAudioPlayer);
        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mSwitchToMinimap = (ImageButton) findViewById(R.id.switch_minimap);
        mSwitchToPlayback = (ImageButton) findViewById(R.id.switch_source_playback);
        mLangView = (TextView) findViewById(R.id.file_language);
        mSourceView = (TextView) findViewById(R.id.file_project);
        mBookView = (TextView) findViewById(R.id.file_book);
        mChapterView = (TextView) findViewById(R.id.file_chapter);
        mChapterLabel = (TextView) findViewById(R.id.file_chapter_label);
        mUnitView = (TextView) findViewById(R.id.file_unit);
        mUnitLabel = (TextView) findViewById(R.id.file_unit_label);
        // NOTE: Look at Android Studio's warning. Why is the same view converted and captured as
        //    two different things? (Refering to this and mSrcAudioPlayback)
        mSrcPlayer = (SourceAudio) findViewById(R.id.srcAudioPlayer);
        mEnterVMarkerMode = (ImageButton) findViewById(R.id.btnEnterVMarkerMode);
        mExitVMarkerMode = (ImageButton) findViewById(R.id.btnExitVMarkerMode);
        mRateBtn = (FourStepImageView) findViewById(R.id.btnRate);
        mRerecordBtn = (ImageButton) findViewById(R.id.btnRerecord);
        mInsertBtn = (ImageButton) findViewById(R.id.btnInsertRecord);
        mPlayBtn = (ImageButton) findViewById(R.id.btnPlay);
        mPauseBtn = (ImageButton) findViewById(R.id.btnPause);
        mSkipBackBtn = (ImageButton) findViewById(R.id.btnSkipBack);
        mSkipForwardBtn = (ImageButton) findViewById(R.id.btnSkipForward);
        mDropStartMarkBtn = (ImageButton) findViewById(R.id.btnStartMark);
        mDropEndMarkBtn = (ImageButton) findViewById(R.id.btnEndMark);
        mUndoBtn = (ImageButton) findViewById(R.id.btnUndo);
        mCutBtn = (ImageButton) findViewById(R.id.btnCut);
        mClearBtn = (ImageButton) findViewById(R.id.btnClear);
        mSaveBtn = (ImageButton) findViewById(R.id.btnSave);
    }

    private void initializeViews() {
        mLangView.setText(mProject.getTargetLanguage().toUpperCase());
        if (!mProject.isOBS()) {
            mSourceView.setText(mProject.getSource().toUpperCase());
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            mBookView.setText(db.getBookName(mProject.getSlug()));
        } else {
            mSourceView.setText("");
            mBookView.setText("Open Bible Stories");
        }
        mChapterView.setText(String.format("%d", mChapter));
        mUnitView.setText(String.format("%d", mUnit));

        if (mProject.getMode().compareTo("chunk") == 0) {
            mUnitLabel.setText("Chunk");
        } else {
            mUnitLabel.setText("Verse");
            mEnterVMarkerMode.setVisibility(View.GONE);
        }
        // By default, select the minimap view over the source playback
        mSwitchToMinimap.setSelected(true);

        mMainCanvas.enableGestures();
        mMainCanvas.setDb(0);

        mStartMarker.setOrientation(MarkerView.LEFT);
        mEndMarker.setOrientation(MarkerView.RIGHT);

        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
        mRating = db.getTakeRating(fne);
        mRateBtn.setStep(mRating);
        mRateBtn.invalidate();
        db.close();
    }

    private void initializeController() {
        final Activity ctx = this;
        ViewTreeObserver vto = mMainCanvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Logger.i(this.toString(), "Initializing UIDataManager in VTO callback");
                mManager = new UIDataManager(mMainCanvas, minimap, mStartMarker, mEndMarker, ctx, UIDataManager.PLAYBACK_MODE);
                mManager.loadWavFile(mWavFile);
                mManager.updateUI();
                mMainCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mSrcPlayer.pauseSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManager.release();
        mSrcPlayer.cleanup();
        SectionMarkers.clearMarkers(mManager);
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        mRating = dialog.getRating();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), mRating);
        db.close();
        mRateBtn.setStep(mRating);
    }

    @Override
    public void onNegativeClick(RatingDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
        if (!isSaved && mManager.hasCut()) {
            Logger.i(this.toString(), "Asking if user wants to save before going back");
            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_AppCompat_Light_Dialog, true, isPlaying, mWavFile.getFile());
            exit.show();
        } else {
//            clearMarkers();
            mManager.release();
            super.onBackPressed();
        }
    }


    private void playRecording() {
        isPlaying = true;
        mManager.play();
        int toShow[] = {R.id.btnPause};
        int toHide[] = {R.id.btnPlay};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
    }

    private void pausePlayback() {
        // NOTE: Shouldn't we set isPlaying = false here?
        mManager.pause(true);
        int toShow[] = {R.id.btnPlay};
        int toHide[] = {R.id.btnPause};
        mManager.swapViews(toShow, toHide);
    }

    private void skipForward() {
        mManager.seekToEnd();
        mManager.updateUI();
    }

    private void skipBack() {
        mManager.seekToStart();
        mManager.updateUI();
    }

    private void placeStartMarker() {
        mMainCanvas.placeStartMarker(mManager.getLocation());
        int toShow[] = {R.id.btnEndMark, R.id.btnClear};
        int toHide[] = {R.id.btnStartMark};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
    }

    private void placeEndMarker() {
        mMainCanvas.placeEndMarker(mManager.getLocation());
        int toShow[] = {R.id.btnCut};
        int toHide[] = {R.id.btnEndMark};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
    }

    private void cut() {
        isSaved = false;
        int toShow[] = {R.id.btnStartMark, R.id.btnUndo};
        int toHide[] = {R.id.btnCut, R.id.btnClear};
        mManager.swapViews(toShow, toHide);
        mManager.cutAndUpdate();
    }

    private void undo() {
        // TODO: Check mManager.hasCut() before hiding the undo button when cut is allowed more than one time.
        mManager.undoCut();
        int toShow[] = {};
        int toHide[];
        if (!mManager.hasCut()) {
            toHide = new int[1];
            toHide[0] = R.id.btnUndo;
        } else {
            toHide = new int[0];
        }
        mManager.swapViews(toShow, toHide);
    }

    private void clearMarkers() {
        SectionMarkers.clearMarkers(mManager);
        int toShow[] = {R.id.btnStartMark};
        int toHide[] = {R.id.btnClear, R.id.btnEndMark, R.id.btnCut};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
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

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Saving");
        pd.setMessage("Writing changes to file, please wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgressNumberFormat(null);
        pd.show();
        Thread saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mManager.hasCut()) {
                    try {
                        File dir = Project.getProjectDirectory(mProject);
                        File toTemp = new File(dir, "temp.wav");
                        mManager.writeCut(toTemp, to, from, pd);
                        to.delete();
                        toTemp.renameTo(to);
                        ProjectDatabaseHelper db = new ProjectDatabaseHelper(PlaybackScreen.this);
                        db.addTake(new FileNameExtractor(to), to.getName(), to.lastModified(), 0);
                        db.close();
                        String oldName = from.getFile().getName();
                        oldName = oldName.substring(0, oldName.lastIndexOf("."));
                        File toVis = new File(AudioInfo.pathToVisFile, oldName + ".vis");
                        toVis.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isSaved = true;
                pd.dismiss();
                if (intent == null) {
                    finish();
                } else {
                    intent.putExtra("old_name", to.getAbsolutePath());
                    startActivity(intent);
                    finish();
                }
            }
        });
        saveThread.start();
    }

    public void insert() {
        Intent insertIntent = RecordingScreen.getInsertIntent(this, mProject, mWavFile, mChapter, mUnit, mManager.getAdjustedLocation());
        save(insertIntent);
    }

    private void setButtonHandlers() {
        // NOTE: Why are we assigning the same OnClickListener and then putting it through the
        // switch case later while we already know what's being clicked right here? Should we break
        // up the OnClickListener to specific ones?
        mPlayBtn.setOnClickListener(btnClick);
        mSaveBtn.setOnClickListener(btnClick);
        mPauseBtn.setOnClickListener(btnClick);
        mSkipBackBtn.setOnClickListener(btnClick);
        mSkipForwardBtn.setOnClickListener(btnClick);
        mDropStartMarkBtn.setOnClickListener(btnClick);
        mDropEndMarkBtn.setOnClickListener(btnClick);
        mCutBtn.setOnClickListener(btnClick);
        mClearBtn.setOnClickListener(btnClick);
        mRateBtn.setOnClickListener(btnClick);
        mUndoBtn.setOnClickListener(btnClick);
        mRerecordBtn.setOnClickListener(btnClick);
        mInsertBtn.setOnClickListener(btnClick);
        mSwitchToMinimap.setOnClickListener(btnClick);
        mSwitchToPlayback.setOnClickListener(btnClick);
        mEnterVMarkerMode.setOnClickListener(btnClick);
        mExitVMarkerMode.setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        // NOTE: Why do we need to enable these buttons?
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
    }

    private View[] getViewsToHideInMarkerMode() {
        return new View[]{mLangView, mSourceView, mBookView, mChapterView, mChapterLabel,
                mUnitView, mUnitLabel, mEnterVMarkerMode, mRateBtn, mRerecordBtn, mInsertBtn,
                mDropStartMarkBtn, mSaveBtn};
    }

    private View[] getViewsToHideInNormalMode() {
        return new View[]{mExitVMarkerMode};
    }

    private void enterVerseMarkerMode() {
        isInVMarkerMode = true;
        Utils.hideView(getViewsToHideInMarkerMode());
        Utils.showView(getViewsToHideInNormalMode());
        mToolbar.setBackgroundColor(getResources().getColor(R.color.tertiary));
    }

    private void exitVerseMarkerMode() {
        isInVMarkerMode = false;
        Utils.showView(getViewsToHideInMarkerMode());
        Utils.hideView(getViewsToHideInNormalMode());
        mToolbar.setBackgroundColor(getResources().getColor(R.color.primary));
    }


    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay: {
                    playRecording();
                    break;
                }
                case R.id.btnSave: {
                    save(null);
                    break;
                }
                case R.id.btnPause: {
                    pausePlayback();
                    break;
                }
                case R.id.btnSkipForward: {
                    skipForward();
                    break;
                }
                case R.id.btnSkipBack: {
                    skipBack();
                    break;
                }
                case R.id.btnStartMark: {
                    placeStartMarker();
                    break;
                }
                case R.id.btnEndMark: {
                    placeEndMarker();
                    break;
                }
                case R.id.btnCut: {
                    cut();
                    break;
                }
                case R.id.btnClear: {
                    clearMarkers();
                    break;
                }
                case R.id.btnRate: {
                    // NOTE: Probably don't need to pass in the view once we implement it the right
                    // way
                    openRating((FourStepImageView) v);
                    break;
                }
                case R.id.btnUndo: {
                    undo();
                    break;
                }
                case R.id.btnMarkerMode: {
                    enterVerseMarkerMode();
                    break;
                }
                case R.id.btnExitMarkerMode: {
                    exitVerseMarkerMode();
                    break;
                }
                case R.id.btnRerecord: {
                    rerecord();
                    break;
                }
                case R.id.btnInsertRecord: {
                    insert();
                    break;
                }
                case R.id.switch_minimap: {
                    // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                    v.setSelected(true);
                    v.setBackgroundColor(Color.parseColor("#00000000"));
                    minimap.setVisibility(View.VISIBLE);
                    mSrcAudioPlayback.setVisibility(View.INVISIBLE);
                    mSwitchToPlayback.setSelected(false);
                    mSwitchToPlayback.setBackgroundColor(getResources().getColor(R.color.mostly_black));
                    break;
                }
                case R.id.switch_source_playback: {
                    // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                    v.setSelected(true);
                    v.setBackgroundColor(Color.parseColor("#00000000"));
                    mSrcAudioPlayback.setVisibility(View.VISIBLE);
                    minimap.setVisibility(View.INVISIBLE);
                    mSwitchToMinimap.setSelected(false);
                    mSwitchToMinimap.setBackgroundColor(getResources().getColor(R.color.mostly_black));
                    break;
                }
            }
        }
    };
}
