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
import android.text.InputType;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.SectionMarkers;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Reporting.Logger;
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
    private WaveformView mainCanvas;
    private MinimapView minimap;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private UIDataManager manager;
    private PreferencesManager pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isALoadedFile = false;
    private ProgressDialog mProgress;

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

        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));
        mStartMarker = ((MarkerView) findViewById(R.id.startmarker));
        mStartMarker.setOrientation(MarkerView.LEFT);
        mEndMarker = ((MarkerView) findViewById(R.id.endmarker));
        mEndMarker.setOrientation(MarkerView.RIGHT);

        final Activity ctx = this;
        ViewTreeObserver vto = mainCanvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Logger.i(this.toString(), "Initializing UIDataManager in VTO callback");
                manager = new UIDataManager(mainCanvas, minimap, mStartMarker, mEndMarker, ctx, UIDataManager.PLAYBACK_MODE, isALoadedFile);
                manager.loadWavFromFile(recordedFilename);
                manager.updateUI();
                mainCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        filenameView = (TextView) findViewById(R.id.filenameView);
        filenameView.setText(suggestedFilename);
    }

    private void pausePlayback() {
        manager.swapPauseAndPlay();
        WavPlayer.pause(true);
    }

    private void skipForward() {
        WavPlayer.seekToEnd();
        manager.updateUI();
    }

    private void skipBack() {
        WavPlayer.seekToStart();
        manager.updateUI();
    }

    private void playRecording() {
        manager.swapPauseAndPlay();
        isPlaying = true;
        WavPlayer.play();
        manager.updateUI();
    }

    private void placeStartMarker(){
        mainCanvas.placeStartMarker(WavPlayer.getLocation());
        manager.updateUI();
    }

    private void placeEndMarker(){
        mainCanvas.placeEndMarker(WavPlayer.getLocation());
        manager.updateUI();
    }

    private void cut() {
        manager.cutAndUpdate();
    }

    private void undo() {
        manager.undoCut();
    }

    private void clearMarkers(){
        SectionMarkers.clearMarkers();
        manager.updateUI();
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
        if (!isSaved && !isALoadedFile || isALoadedFile && manager.hasCut()) {
            Logger.i(this.toString(), "Asking if user wants to save before going back");
            ExitDialog dialog = new ExitDialog(this, R.style.Theme_UserDialog);
            dialog.setFilename(recordedFilename);
            dialog.setLoadedFile(isALoadedFile);
            if (isPlaying) {
                dialog.setIsPlaying(true);
                isPlaying = false;
            }
            dialog.show();
        } else {
            WavPlayer.release();
            super.onBackPressed();
        }
    }

    private boolean getSaveName(Context c) {
        final EditText toSave = new EditText(c);
        toSave.setInputType(InputType.TYPE_CLASS_TEXT);

        //pref.getPreferences("fileName");
        toSave.setText(suggestedFilename, TextView.BufferType.EDITABLE);

        //prepare the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Save as");
        builder.setView(toSave);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setName(toSave.getText().toString());
                //SAVE FILE HERE
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
        return true;
    }

    private void setName(String newName) {
        suggestedFilename = newName;

        File dir = new File(pref.getPreferences("fileDirectory").toString());
        File from = new File(recordedFilename);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM:dd:yyyy:hh:mm:ss");
        String format = simpleDateFormat.format(new Date());
        //File to = new File(dir, suggestedFilename + "d_" + format + AUDIO_RECORDER_FILE_EXT_WAV);
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
                    isSaved = true;
                    recordedFilename = saveFile(finalFrom, finalTo, suggestedFilename);
                    filenameView.setText(suggestedFilename);
                }
            });
            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            isSaved = true;
            recordedFilename = saveFile(from, to, suggestedFilename);
            filenameView.setText(suggestedFilename);
        }
    }

    public String getName() {
        return suggestedFilename;
    }

    private void saveRecording() {
        try {
            getSaveName(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Names the currently recorded .wav file.
     *
     * @param name a string with the desired output filename. Should not include the .wav extension.
     * @return the absolute path of the file created
     */
    public String saveFile(File from, File to, String name) {

        if(manager.hasCut()){
            try {
                manager.writeCut(to);
                from.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Boolean out = from.renameTo(to);
            Logger.i(this.toString(), "result of saving file " + out);
            File fromVis = new File(AudioInfo.pathToVisFile, "visualization.vis");
            File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
            try {
                toVis.createNewFile();
                toVis.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = fromVis.renameTo(toVis);
            Logger.i(this.toString(), "result of saving vis file " + out + toVis.getAbsolutePath() + " path to vis file is " + AudioInfo.pathToVisFile);
            recordedFilename = to.getAbsolutePath();
        }
        if(!isALoadedFile) {
            Settings.incrementTake(this);
        }
        this.finish();
        return to.getAbsolutePath();
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
        if (recordedFilename != null)
            return (file.getAbsolutePath() + "/" + recordedFilename);
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
                    saveRecording();
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
            }
        }
    };
}
