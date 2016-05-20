package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import wycliffeassociates.recordingapp.ProjectManager.DatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Reporting.BugReportDialog;
import wycliffeassociates.recordingapp.Reporting.GithubReporter;
import wycliffeassociates.recordingapp.Reporting.GlobalExceptionHandler;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.FilesPage.AudioFiles;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.BookActivity;
import wycliffeassociates.recordingapp.SettingsPage.LanguageActivity;
import wycliffeassociates.recordingapp.SettingsPage.ModeActivity;
import wycliffeassociates.recordingapp.SettingsPage.ProjectActivity;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.SettingsPage.SourceTextActivity;


public class MainMenu extends Activity{

    private ImageButton btnRecord;
    private ImageButton btnFiles;
    private ImageButton btnSettings;
    private SharedPreferences pref;

    private int mNumProjects = 0;

    public static final String KEY_PREF_LOGGING_LEVEL = "logging_level";
    public static final String PREF_DEFAULT_LOGGING_LEVEL = "1";
    public static final String STACKTRACE_DIR = "stacktrace";

    public static final int FIRST_REQUEST = 1;
    public static final int LANGUAGE_REQUEST = FIRST_REQUEST;
    public static final int PROJECT_REQUEST = FIRST_REQUEST + 1;
    public static final int BOOK_REQUEST = FIRST_REQUEST + 2;
    public static final int MODE_REQUEST = FIRST_REQUEST + 3;
    public static final int SOURCE_TEXT_REQUEST = FIRST_REQUEST + 4;
    public static final int SOURCE_REQUEST = FIRST_REQUEST + 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        AudioInfo.SCREEN_WIDTH = metrics.widthPixels;

        System.out.println("internal files dir is " + this.getCacheDir());
        System.out.println("External files dir is " + Environment.getExternalStorageDirectory());

        initApp();

        DatabaseHelper db = new DatabaseHelper(this);
        mNumProjects = db.getNumProjects();

        btnRecord = (ImageButton) findViewById(R.id.new_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              TODO: clicking new_record button should trigger a new_project activity instead of the recording
                if(mNumProjects < 0) {
                    Intent intent = new Intent(v.getContext(), LanguageActivity.class);
                    intent.putExtra(Project.PROJECT_EXTRA, new Project());
                    startActivityForResult(intent, LANGUAGE_REQUEST);
                } else {
                    Intent intent = new Intent(v.getContext(), RecordingScreen.class);
                    startActivity(intent);
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case LANGUAGE_REQUEST: {
                if(resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    Intent intent = new Intent(this, ProjectActivity.class);
                    intent.putExtra(Project.PROJECT_EXTRA, project);
                    startActivityForResult(intent, PROJECT_REQUEST);
                } else if (resultCode == RESULT_CANCELED){
                    onResume();
                }
                break;
            }
            case PROJECT_REQUEST: {
                if(resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    //FIXME: Replace with moving to Source Activity
                    if(project.getProject().compareTo("obs") == 0){
                        addProjectToDatabase(project);
                        loadProject(project);
                        Intent intent = new Intent(this, RecordingScreen.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, BookActivity.class);
                        intent.putExtra(Project.PROJECT_EXTRA, project);
                        startActivityForResult(intent, BOOK_REQUEST);
                    }
                } else if (resultCode == RESULT_CANCELED){
                    onResume();
                }
                break;
            }
            case BOOK_REQUEST: {
                if(resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    //FIXME: this is a hack rather than using a source text activity
                    Intent intent = new Intent(this, SourceTextActivity.class);
                    intent.putExtra(Project.PROJECT_EXTRA, project);
                    startActivityForResult(intent, SOURCE_TEXT_REQUEST);
                } else if (resultCode == RESULT_CANCELED){
                    onResume();
                }
                break;
            }
            case SOURCE_TEXT_REQUEST: {
                if(resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    Intent intent = new Intent(this, ModeActivity.class);
                    intent.putExtra(Project.PROJECT_EXTRA, project);
                    startActivityForResult(intent, MODE_REQUEST);
                } else if (resultCode == RESULT_CANCELED){
                    onResume();
                }
                break;
            }
            case MODE_REQUEST: {
//                if(resultCode == RESULT_OK) {
//                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
//                    Intent intent = new Intent(this, SourceLanguageActivity.class);
//                    intent.putExtra(Project.PROJECT_EXTRA, project);
//                    startActivityForResult(intent, PROJECT_REQUEST);
//                } else if (resultCode == RESULT_CANCELED){
//                    onResume();
//                }
//                break;
                //FALLTHROUGH
                //FIXME: REPLACE WITH COMMENTED CODE ABOVE
            }
            case SOURCE_REQUEST: {
                if(resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    addProjectToDatabase(project);
                    loadProject(project);
                    Intent intent = new Intent(this, RecordingScreen.class);
                    startActivity(intent);
                } else if (resultCode == RESULT_CANCELED){
                    onResume();
                }
                break;
            }
            default:
        }
    }

    private void addProjectToDatabase(Project project){
        DatabaseHelper db = new DatabaseHelper(this);
        db.addProject(project);
    }

    private void loadProject(Project project){
        pref.edit().putString(Settings.KEY_PREF_BOOK, project.getSlug()).commit();
        pref.edit().putString(Settings.KEY_PREF_BOOK_NUM, project.getBookNumber()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG, project.getTargetLang()).commit();
        pref.edit().putString(Settings.KEY_PREF_SOURCE, project.getSource()).commit();
        pref.edit().putString(Settings.KEY_PREF_PROJECT, project.getProject()).commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK_VERSE, project.getMode()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG_SRC, project.getSrcLang()).commit();

        //FIXME: find the last place worked on?
        pref.edit().putString(Settings.KEY_PREF_CHAPTER, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_START_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_END_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK, "1").commit();

        Settings.updateFilename(this);
    }

    public void report(final String message) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                reportCrash(message);
            }
        });
        t.start();
    }

    private void reportCrash(String message){
        File dir = new File(getExternalCacheDir(), STACKTRACE_DIR);
        String[] stacktraces = GlobalExceptionHandler.getStacktraces(dir);
        String githubTokenIdentifier = getResources().getString(R.string.github_token);
        String githubUrl = getResources().getString(R.string.github_bug_report_repo);

        // TRICKY: make sure the github_oauth2 token has been set
        if (githubTokenIdentifier != null) {
            GithubReporter reporter = new GithubReporter(this, githubUrl, githubTokenIdentifier);
            if (stacktraces.length > 0) {
                // upload most recent stacktrace
                reporter.reportCrash(message, new File(stacktraces[0]), Logger.getLogFile());
                // empty the log
                try {
                    FileUtils.write(Logger.getLogFile(), "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                archiveStackTraces();
            }
        }
    }

    public void archiveStackTraces(){
        File dir = new File(getExternalCacheDir(), STACKTRACE_DIR);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File archive = new File(dir, "Archive");
        if(!archive.exists()){
            archive.mkdirs();
        }
        String[] stacktraces = GlobalExceptionHandler.getStacktraces(dir);
        // delete stacktraces
        for (String filePath : stacktraces) {
            File traceFile = new File(filePath);
            if (traceFile.exists()) {
                File move = new File(archive, traceFile.getName());
                traceFile.renameTo(move);
            }
        }
    }

    private void initApp(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString("version", BuildConfig.VERSION_NAME).commit();

        //set up Visualization folder
        File visDir = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/Visualization");
        System.out.println("Result of making vis directory " + visDir.mkdirs());
        if(visDir.exists()){
            Logger.w(this.toString(), "SUCCESS: Visualization folder exists.");
        } else {
            Logger.e(this.toString(), "ERROR: Visualization folder does not exist.");
        }

        //set up directory paths
        pref.edit().putString("vis_folder_path", visDir.getAbsolutePath() + "/").commit();
        //if the current directory is already set, then don't overwrite it
        if(pref.getString("current_directory", null) == null){
            pref.edit().putString("current_directory",
                    Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit();
        }
        pref.edit().putString("root_directory", Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit();
        AudioInfo.pathToVisFile = visDir.getAbsolutePath() + "/";
        AudioInfo.fileDir = Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString();

        //configure logger
        File dir = new File(getExternalCacheDir(), STACKTRACE_DIR);
        dir.mkdirs();

        GlobalExceptionHandler.register(dir);
        int minLogLevel = Integer.parseInt(pref.getString(KEY_PREF_LOGGING_LEVEL, PREF_DEFAULT_LOGGING_LEVEL));
        configureLogger(minLogLevel, dir);

        //check if we crashed
        String[] stacktraces = GlobalExceptionHandler.getStacktraces(dir);
        if (stacktraces.length > 0) {
            FragmentManager fm = getFragmentManager();
            BugReportDialog brd = new BugReportDialog();
            brd.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            brd.show(fm, "Bug Report Dialog");
        }
    }

    public void configureLogger(int minLogLevel, File logDir) {
        File logFile = new File(logDir, "log.txt");
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.configure(logFile, Logger.Level.getLevel(minLogLevel));
        if(logFile.exists()){
            Logger.w(this.toString(), "SUCCESS: Log file initialized.");
        } else {
            Logger.e(this.toString(),"ERROR: could not initialize log file.");
        }
    }
}
