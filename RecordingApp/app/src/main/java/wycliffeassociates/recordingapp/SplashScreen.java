package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.Reporting.GithubReporter;
import wycliffeassociates.recordingapp.Reporting.GlobalExceptionHandler;
import wycliffeassociates.recordingapp.Reporting.Logger;

public class SplashScreen extends Activity{

    public static final String KEY_PREF_LOGGING_LEVEL = "logging_level";
    public static final String PREF_DEFAULT_LOGGING_LEVEL = "1";
    public static final String STACKTRACE_DIR = "stacktrace";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                initApp();
            }
        });
        t.start();
        Intent intent = new Intent(this, MainMenu.class);
        startActivityForResult(intent, 0);
    }

    private void uploadToGithub(String[] stacktraces) {
        String githubTokenIdentifier = getResources().getString(R.string.github_token);
        String githubUrl = getResources().getString(R.string.github_bug_report_repo);

        // TRICKY: make sure the github_oauth2 token has been set
        if (githubTokenIdentifier != null) {
            GithubReporter reporter = new GithubReporter(this, githubUrl, githubTokenIdentifier);
            if (stacktraces.length > 0) {
                // upload most recent stacktrace
                reporter.reportCrash("crash", new File(stacktraces[0]), Logger.getLogFile());
                // empty the log
                try {
                    FileUtils.write(Logger.getLogFile(), "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // delete stacktraces
                for (String filePath : stacktraces) {
                    File traceFile = new File(filePath);
                    if (traceFile.exists()) {
                        traceFile.delete();
                    }
                }
            }
        }
    }

    private void initApp(){

        // configure logger
        File dir = new File(getExternalCacheDir(), STACKTRACE_DIR);
        GlobalExceptionHandler.register(dir);
        int minLogLevel = Integer.parseInt(getUserPreferences().getString(KEY_PREF_LOGGING_LEVEL, PREF_DEFAULT_LOGGING_LEVEL));
        configureLogger(minLogLevel);

        // check if we crashed
        String[] files = GlobalExceptionHandler.getStacktraces(dir);
        if (files.length > 0) {
            uploadToGithub(files);
        }

        // set up Visualization folder
        File visDir = new File(Environment.getExternalStoragePublicDirectory("TranslationRecorder"), "/Visualization");
        System.out.println("Result of making vis directory " + visDir.mkdir());
        AudioInfo.pathToVisFile = visDir.getAbsolutePath() + "/";
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
