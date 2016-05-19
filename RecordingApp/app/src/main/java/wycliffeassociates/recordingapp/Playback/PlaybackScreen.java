package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.SectionMarkers;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.RerecordDialog;
import wycliffeassociates.recordingapp.SettingsPage.InternsPreferencesManager;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 11/10/2015.
 */
public class PlaybackScreen extends Activity{

    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";

    private final Context context = this;
    private TextView filenameView;
    private WaveformView mMainCanvas;
    private MinimapView minimap;
    private View mSrcAudioPlayback;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private UIDataManager mManager;
    private SharedPreferences pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private volatile boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isALoadedFile = false;
    private ProgressDialog mProgress;
    private volatile boolean mChangedName = false;
    private ImageButton mSwitchToMinimap;
    private ImageButton mSwitchToPlayback;
    private FileNameExtractor mFileNameExtractor;
    private TextView mLangView;
    private TextView mSourceView;
    private TextView mBookView;
    private TextView mChapterView;
    private TextView mChunkView;
    private SourceAudio mSrcPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        suggestedFilename = pref.getString(Settings.KEY_PREF_FILENAME, "en_udb_gen_01-01_01");
        recordedFilename = getIntent().getStringExtra("recordedFilename");
        isALoadedFile = getIntent().getBooleanExtra("loadFile", false);
        if(isALoadedFile){
            suggestedFilename = FileNameExtractor.getNameWithoutTake(recordedFilename);
            //suggestedFilename = recordedFilename.substring(recordedFilename.lastIndexOf('/')+1, recordedFilename.lastIndexOf('.'));
        }
        mFileNameExtractor = new FileNameExtractor(suggestedFilename);
        isSaved = true;
        Logger.w(this.toString(), "Loading Playback screen. Recorded Filename is " + recordedFilename + " Suggested Filename is " + suggestedFilename + " Came from loading a file is:" + isALoadedFile);

        // Make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.playback_screen);

        mMainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        mSrcAudioPlayback = (View) findViewById(R.id.srcAudioPlayer);
        mStartMarker = ((MarkerView) findViewById(R.id.startmarker));
        mEndMarker = ((MarkerView) findViewById(R.id.endmarker));
        mSwitchToMinimap = (ImageButton) findViewById(R.id.switch_minimap);
        mSwitchToPlayback = (ImageButton) findViewById(R.id.switch_source_playback);

        mLangView = (TextView) findViewById(R.id.file_language);
        mSourceView = (TextView) findViewById(R.id.file_project);
        mBookView = (TextView) findViewById(R.id.file_book);
        mChapterView = (TextView) findViewById(R.id.file_chapter);
        mChunkView = (TextView) findViewById(R.id.file_unit);
        mLangView.setText(mFileNameExtractor.getLang().toUpperCase());
        mSourceView.setText(mFileNameExtractor.getSource().toUpperCase());
        mBookView.setText(mFileNameExtractor.getBook().toUpperCase());
        mChapterView.setText(String.format("%d", mFileNameExtractor.getChapter()));
        mChunkView.setText(String.format("%d", mFileNameExtractor.getStartVerse()));

        if(pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "chunk").compareTo("chunk") == 0) {
            ((TextView) findViewById(R.id.file_unit_label)).setText("Chunk");
        } else {
            ((TextView) findViewById(R.id.file_unit_label)).setText("Verse");
        }

        setButtonHandlers();
        enableButtons();

        // By default, select the minimap view over the source playback
        mSwitchToMinimap.setSelected(true);

        mMainCanvas.enableGestures();
        mMainCanvas.setDb(0);

        mStartMarker.setOrientation(MarkerView.LEFT);
        mEndMarker.setOrientation(MarkerView.RIGHT);

        mSrcPlayer = (SourceAudio) findViewById(R.id.srcAudioPlayer);
        mSrcPlayer.initSrcAudio();

        final Activity ctx = this;
        ViewTreeObserver vto = mMainCanvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Logger.i(this.toString(), "Initializing UIDataManager in VTO callback");
                mManager = new UIDataManager(mMainCanvas, minimap, mStartMarker, mEndMarker, ctx, UIDataManager.PLAYBACK_MODE, isALoadedFile);
                mManager.loadWavFromFile(recordedFilename);
                mManager.updateUI();
                mMainCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();
        mSrcPlayer.pauseSource();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mManager.release();
        mSrcPlayer.cleanup();
        SectionMarkers.clearMarkers(mManager);
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
        int toShow[] = {R.id.btnPlay};
        int toHide[] = {R.id.btnPause};
        mManager.swapViews(toShow, toHide);
        mManager.pause(true);
    }

    private void skipForward() {
        mManager.seekToEnd();
        mManager.updateUI();
    }

    private void skipBack() {
        mManager.seekToStart();
        mManager.updateUI();
    }

    private void placeStartMarker(){
        mMainCanvas.placeStartMarker(mManager.getLocation());
        int toShow[] = {R.id.btnEndMark, R.id.btnClear};
        int toHide[] = {R.id.btnStartMark};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
    }

    private void placeEndMarker(){
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
        if(!mManager.hasCut()) {
            toHide = new int[1];
            toHide[0] = R.id.btnUndo;
        }
        else {
            toHide = new int[0];
        }
        mManager.swapViews(toShow, toHide);
    }

    private void clearMarkers(){
        SectionMarkers.clearMarkers(mManager);
        int toShow[] = {R.id.btnStartMark};
        int toHide[] = {R.id.btnClear, R.id.btnEndMark, R.id.btnCut};
        mManager.swapViews(toShow, toHide);
        mManager.updateUI();
    }

    private void rerecord(){
        File file = new File(recordedFilename);
        FileNameExtractor fne = new FileNameExtractor(file);
        if(fne.matched()) {
            Settings.updateFilename(this, fne.getLang(), fne.getSource(), fne.getBook(),
                    fne.getChapter(), fne.getChunk());
        }
        Intent intent = new Intent(this, RecordingScreen.class);
        save(intent);
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
        if (!isSaved && !isALoadedFile || isALoadedFile && mManager.hasCut()) {
            Logger.i(this.toString(), "Asking if user wants to save before going back");
            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_UserDialog, true, isPlaying, recordedFilename);
            exit.show();
        } else {
//            clearMarkers();
            mManager.release();
            super.onBackPressed();
        }
    }

    private void mChangedName() {
        mChangedName = true;
    }

    private void save(Intent intent) {
        //no changes were made, so just exit
        if(isSaved){
            if(intent == null) {
                this.finish();
                return;
            } else {
                startActivity(intent);
                this.finish();
                return;
            }
        }

        File dir = FileNameExtractor.getDirectoryFromFile(pref, new File(suggestedFilename));
        File from = new File(recordedFilename);

//        if(isALoadedFile) {
//            suggestedFilename = suggestedFilename.substring(0, suggestedFilename.lastIndexOf("_"));
//        }
        int takeInt = FileNameExtractor.getLargestTake(dir, new File(suggestedFilename))+1;
        String take = String.format("%02d", takeInt);
        File to = new File(dir, suggestedFilename + "_" + take + AUDIO_RECORDER_FILE_EXT_WAV);
        writeCutToFile(to, from.getName().substring(0, from.getName().lastIndexOf(".")), intent);
    }

    public String getName() {
        return suggestedFilename;
    }

    /**
     * Names the currently recorded .wav file.
     *
     * @param name a string with the desired output filename. Should not include the .wav extension.
     * @return the absolute path of the file created
     */
    public void writeCutToFile(final File to, final String name, final Intent intent) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Saving");
        pd.setMessage("Writing changes to file, please wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgressNumberFormat(null);
        pd.show();
        Thread saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(mManager.hasCut()){
                    try {
                        File dir = new File(pref.getString("current_directory", "").toString());
                        File toTemp = new File(dir, "temp.wav");
                        mManager.writeCut(toTemp, pd);
                        to.delete();
                        toTemp.renameTo(to);
                        File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
                        toVis.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isSaved = true;
                pd.dismiss();
                if(intent == null) {
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

    public void insert(){
        Intent insertIntent = new Intent(this, RecordingScreen.class);
        insertIntent.putExtra("insert_location", mManager.getAdjustedLocation());
        insertIntent.putExtra("old_name", recordedFilename);
        insertIntent.putExtra("insert_mode", true);
        save(insertIntent);
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
        findViewById(R.id.btnPause).setOnClickListener(btnClick);
        findViewById(R.id.btnSkipBack).setOnClickListener(btnClick);
        findViewById(R.id.btnSkipForward).setOnClickListener(btnClick);
        findViewById(R.id.btnStartMark).setOnClickListener(btnClick);
        findViewById(R.id.btnEndMark).setOnClickListener(btnClick);
        findViewById(R.id.btnCut).setOnClickListener(btnClick);
        findViewById(R.id.btnClear).setOnClickListener(btnClick);
        findViewById(R.id.btnUndo).setOnClickListener(btnClick);
        findViewById(R.id.btnRerecord).setOnClickListener(btnClick);
        findViewById(R.id.btnInsertRecord).setOnClickListener(btnClick);
        mSwitchToMinimap.setOnClickListener(btnClick);
        mSwitchToPlayback.setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
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
                case R.id.btnUndo: {
                    undo();
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
