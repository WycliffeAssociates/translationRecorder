package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

import Reporting.Logger;
import wycliffeassociates.recordingapp.FilesPage.AudioFiles;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

public class MainMenu extends Activity{

    private ImageButton btnRecord;
    private ImageButton btnFiles;
    private ImageButton btnSettings;
    public static final String KEY_PREF_LOGGING_LEVEL = "logging_level";
    public static final String PREF_DEFAULT_LOGGING_LEVEL = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // configure logger
        int minLogLevel = Integer.parseInt(getUserPreferences().getString(KEY_PREF_LOGGING_LEVEL, PREF_DEFAULT_LOGGING_LEVEL));
        configureLogger(minLogLevel);
        Logger.i(MainMenu.class.toString(), "Initialized logger");

        System.out.println("internal files dir is " + getApplicationContext().getFilesDir());
        System.out.println("External files dir is " + Environment.getExternalStoragePublicDirectory("TranslationRecorder"));


        File visDir = new File(Environment.getExternalStoragePublicDirectory("TranslationRecorder"), "/Visualization");
        System.out.println("Result of making vis directory " + visDir.mkdir());



        AudioInfo.pathToVisFile = visDir.getAbsolutePath() + "/";
        btnRecord = (ImageButton) findViewById(R.id.new_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runtime.getRuntime().freeMemory();
//              TODO: clicking new_record button should trigger a new_project activity instead of the recording
                Intent intent = new Intent(v.getContext(), RecordingScreen.class);
                startActivityForResult(intent, 0);
            }
        });

        btnFiles = (ImageButton) findViewById(R.id.files);
        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AudioFiles.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Settings.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
            }
        });
    }

    public void configureLogger(int minLogLevel) {
        Logger.configure(new File(getExternalCacheDir(), "log.txt"), Logger.Level.getLevel(minLogLevel));
    }

    /**
     * Returns an instance of the user preferences.
     * This is just the default shared preferences
     * @return
     */
    public SharedPreferences getUserPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
