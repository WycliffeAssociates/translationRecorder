package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.SectionMarkers;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.RerecordDialog;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
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
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private UIDataManager mManager;
    private PreferencesManager pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isALoadedFile = false;
    private ProgressDialog mProgress;
    private volatile boolean mChangedName = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);

        suggestedFilename = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PREF_FILENAME, "en_mat_1-1_1");
        recordedFilename = getIntent().getStringExtra("recordedFilename");
        isALoadedFile = getIntent().getBooleanExtra("loadFile", false);
        if(isALoadedFile){
            suggestedFilename = recordedFilename.substring(recordedFilename.lastIndexOf('/')+1, recordedFilename.lastIndexOf('.'));
        }
        Logger.w(this.toString(), "Loading Playback screen. Recorded Filename is " + recordedFilename + " Suggested Filename is " + suggestedFilename + " Came from loading a file is:" + isALoadedFile);

        // Make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.playback_screen);

        setButtonHandlers();
        enableButtons();

        mMainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));

        mMainCanvas.enableGestures();
        mMainCanvas.setDb(0);

        mStartMarker = ((MarkerView) findViewById(R.id.startmarker));
        mStartMarker.setOrientation(MarkerView.LEFT);
        mEndMarker = ((MarkerView) findViewById(R.id.endmarker));
        mEndMarker.setOrientation(MarkerView.RIGHT);

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

        filenameView = (TextView) findViewById(R.id.filenameView);
        filenameView.setText(suggestedFilename);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mManager.release();
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
        RerecordDialog exit = RerecordDialog.Build(this, R.style.Theme_UserDialog, isALoadedFile, isPlaying, recordedFilename);
        if(exit != null) {
            exit.show();
        }
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
        if (!isSaved && !isALoadedFile || isALoadedFile && mManager.hasCut()) {
            Logger.i(this.toString(), "Asking if user wants to save before going back");
            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_UserDialog, isALoadedFile, isPlaying, recordedFilename);
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

    private void save() {
        getSaveName(this);
    }

    private void getSaveName(Context c) {
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mChangedName();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        final EditText toSave = new EditText(c);
        toSave.addTextChangedListener(tw);
        toSave.setInputType(InputType.TYPE_CLASS_TEXT);

        //pref.getPreferences("fileName");
        toSave.setText(suggestedFilename, TextView.BufferType.EDITABLE);
        mChangedName = false;

        //prepare the dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Save as");
        builder.setView(toSave);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                setName(toSave.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setName(String newName) {
        suggestedFilename = newName;
        File dir = new File(pref.getPreferences("fileDirectory").toString());
        File from = new File(recordedFilename);

        if(isALoadedFile && suggestedFilename.contains(".wav")) {
            suggestedFilename = suggestedFilename.substring(0, suggestedFilename.lastIndexOf(".wav"));
        }
        File to = new File(dir, suggestedFilename + AUDIO_RECORDER_FILE_EXT_WAV);

        if(to.exists()){
            final File finalFrom = from;
            final File finalTo = to;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Would you like to overwrite the existing file?").setTitle("Warning");
            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    isSaved = true;
                    saveFile(finalFrom, finalTo, suggestedFilename, true, false);
                }
            });
            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    getSaveName(context);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            isSaved = true;
            if(isALoadedFile) {
                saveFile(from, to, suggestedFilename, false, false);
            } else {
                saveFile(from, to, suggestedFilename, false, true);
            }
        }
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
    public void saveFile(final File from, final File to, final String name, final boolean overwrite, final boolean deleteUUID) {

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
                        File dir = new File(pref.getPreferences("fileDirectory").toString());
                        File toTemp = new File(dir, "temp.wav");
                        mManager.writeCut(toTemp, pd);
                        if(overwrite || !isALoadedFile) {
                            from.delete();
                            to.delete();
                            toTemp.renameTo(to);
                        } else {
                            to.delete();
                            toTemp.renameTo(to);
                        }
                        File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
                        toVis.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //if overwrite, just rename the file to the new name since there are no cuts
                    if(overwrite){
                        try {
                            if(!FileUtils.contentEquals(from, to)) {
                                to.delete();
                                from.renameTo(to);
                                File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
                                toVis.delete();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            FileUtils.copyFile(from, to);
                            if(!isALoadedFile) {
                                File fromVis = new File(AudioInfo.pathToVisFile, "visualization.vis");
                                File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
                                FileUtils.copyFile(fromVis, toVis);
                            }
                            recordedFilename = to.getAbsolutePath();
                            if(deleteUUID){
                                from.delete();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(!isALoadedFile && !mChangedName) {
                    int setTo = FileNameExtractor.getLargestTake(new File(AudioInfo.fileDir), to)+1;
                    Settings.incrementTake(context, setTo);
                }
                pd.dismiss();
                finish();
            }
        });
        saveThread.start();
    }


    /**
     * Retrieves the filename of the recorded audio file.
     * If the AudioRecorder folder does not exist, it is created.
     *
     * @return the absolute filepath to the recorded .wav file
     */
    public String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        if (recordedFilename != null) {
            return (file.getAbsolutePath() + "/" + recordedFilename);
        }
        else {
            recordedFilename = (file.getAbsolutePath() + "/" + UUID.randomUUID().toString() + AUDIO_RECORDER_FILE_EXT_WAV);
            return recordedFilename;
        }
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
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
//        enableButton(R.id.btnPause, true);
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
                    save();
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
            }
        }
    };
}
