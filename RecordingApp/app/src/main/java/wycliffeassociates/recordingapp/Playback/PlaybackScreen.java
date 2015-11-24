package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.ExitDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;

/**
 * Created by sarabiaj on 11/10/2015.
 */
public class PlaybackScreen extends Activity {

    //Constants for WAV format
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";


    private final Context context = this;
    private TextView filenameView;
    private WaveformView mainCanvas;
    private MinimapView minimap;
    private UIDataManager manager;
    private PreferencesManager pref;
    private String recordedFilename = null;
    private String suggestedFilename = null;
    private boolean isSaved = false;
    private boolean isPlaying = false;
    private boolean isALoadedFile = false;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);
        suggestedFilename = pref.getPreferences("fileName") + "-" + pref.getPreferences("fileCounter").toString();
        recordedFilename = getIntent().getStringExtra("recordedFilename");
        isALoadedFile = getIntent().getBooleanExtra("loadFile", false);
        System.out.println("Loaded file name is " + recordedFilename);

        //make sure the tablet does not go to sleep while on the recording screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.playback_screen);

        setButtonHandlers();
        enableButtons();

        mainCanvas = ((WaveformView) findViewById(R.id.main_canvas));
        minimap = ((MinimapView) findViewById(R.id.minimap));

        final Activity ctx = this;
        ViewTreeObserver vto = mainCanvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                manager = new UIDataManager(mainCanvas, minimap, ctx, UIDataManager.PLAYBACK_MODE, isALoadedFile);
                System.out.println("and I'm sending in " + recordedFilename);
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
        WavPlayer.pause();
    }

    private void skipForward() {
        WavPlayer.seekTo(WavPlayer.getDuration());
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
    }

    private void placeEndMarker(){
        mainCanvas.placeEndMarker(WavPlayer.getLocation());
    }

    @Override
    public void onBackPressed() {
        if (!isSaved) {
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
        isSaved = true;
        recordedFilename = saveFile(suggestedFilename);
        filenameView.setText(suggestedFilename);
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
    public String saveFile(String name) {
        File dir = new File(pref.getPreferences("fileDirectory").toString());
        // System.out.println(recordedFilename);
        File from = new File(recordedFilename);
        File to = new File(dir, name + AUDIO_RECORDER_FILE_EXT_WAV);
        Boolean out = from.renameTo(to);
        System.out.println("result of saving file " + out);
        File fromVis = new File(AudioInfo.pathToVisFile, "visualization.vis");
        File toVis = new File(AudioInfo.pathToVisFile, name + ".vis");
        try {
            toVis.createNewFile();
            toVis.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out = fromVis.renameTo(toVis);
        System.out.println("result of saving vis file " + out + toVis.getAbsolutePath() + " path to vis file is " + AudioInfo.pathToVisFile);
        recordedFilename = to.getAbsolutePath();
        pref.setPreferences("fileCounter", ((int) pref.getPreferences("fileCounter") + 1));
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
            System.out.println("filename is " + recordedFilename);
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
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons() {
        enableButton(R.id.btnPlay, true);
        enableButton(R.id.btnSave, true);
        enableButton(R.id.btnPause, true);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            System.out.println("Pressed something");
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
            }
        }
    };
}
