package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import wycliffeassociates.recordingapp.AudioVisualization.CanvasView;
import wycliffeassociates.recordingapp.AudioVisualization.MinimapView;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;
import wycliffeassociates.recordingapp.AudioVisualization.WaveformView;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;

/**
 * Created by sarabiaj on 10/22/2015.
 */
public class MediaScreen extends Activity {
    //Constants for WAV format
    protected static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    protected static final String AUDIO_RECORDER_FOLDER = "TranslationRecorder";
    protected String recordedFilename = null;
    protected String outputName = null;
    protected WaveformView mainCanvas;
    protected MinimapView minimap;
    protected UIDataManager manager;
    protected TextView timerView;
    protected TextView filenameView;
    protected boolean backWasPressed = false;
    protected PreferencesManager pref;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        pref = new PreferencesManager(this);
//        //make sure the tablet does not go to sleep while on the recording screen
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        outputName = (String)pref.getPreferences("fileName")+"-" +pref.getPreferences("fileCounter").toString();
//
//        timerView = (TextView) findViewById(R.id.timerView);
//        timerView.invalidate();
//
//        filenameView = (TextView)findViewById(R.id.filenameView);
//        filenameView.setText(recordedFilename);
//
//        mainCanvas = (CanvasView) findViewById(R.id.main_canvas) instanceof WaveformView ? ((WaveformView) findViewById(R.id.main_canvas)) : null;
//        minimap = (CanvasView) findViewById(R.id.minimap) instanceof MinimapView ? ((MinimapView) findViewById(R.id.minimap)) : null;
//        manager = new UIDataManager(mainCanvas, minimap, this);


    }
}
