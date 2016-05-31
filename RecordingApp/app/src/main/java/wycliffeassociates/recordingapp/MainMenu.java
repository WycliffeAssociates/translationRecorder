package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.DatabaseHelper;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Reporting.BugReportDialog;
import wycliffeassociates.recordingapp.Reporting.GithubReporter;
import wycliffeassociates.recordingapp.Reporting.GlobalExceptionHandler;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.FilesPage.AudioFiles;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.project.ProjectWizardActivity;


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
    public static final int PROJECT_WIZARD_REQUEST = FIRST_REQUEST + 6;


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
                startActivityForResult(new Intent(getBaseContext(), ProjectWizardActivity.class), PROJECT_WIZARD_REQUEST);
//                if( pref.getString("resume", "").compareTo("") == 0 ) {
//                    if (mNumProjects <= 0) {
//                        setupNewProject();
//                    } else {
//                        promptProjectList();
//                    }
//                } else {
//                    startRecordingScreen();
//                }
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
            case PROJECT_WIZARD_REQUEST:{
                if(resultCode == RESULT_OK){
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    addProjectToDatabase(project);
                    loadProject(project);
                    Intent intent = new Intent(this, RecordingScreen.class);
                    startActivity(intent);
                } else {
                    onResume();
                }
            }
            default:
        }
    }

    private void setupNewProject(){
//        Intent intent = new Intent(this, LanguageActivity.class);
//        intent.putExtra(Project.PROJECT_EXTRA, new Project());
//        startActivityForResult(intent, LANGUAGE_REQUEST);
    }

    private void promptProjectList(){
        final DatabaseHelper db = new DatabaseHelper(this);
        final List<Project> projects = db.getAllProjects();
        final CharSequence[] items = getProjectList(projects);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Continue an existing Project?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("New Project")) {
                    setupNewProject();
                } else {
                    promptDeleteProject(projects.get(item-1));
//                    loadProject(projects.get(item-1));
//                    startRecordingScreen();
                }
            }
        });
        builder.show();
    }

    private void promptDeleteProject(final Project p){
        final DatabaseHelper db = new DatabaseHelper(this);
        final CharSequence[] items = new CharSequence[]{"Continue Project", "Delete Project"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Continue " + p.getTargetLang() + " " + p.getSlug() + "?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Continue Project")) {
                    loadProject(p);
                    startRecordingScreen();
                } else {
                    db.deleteProject(p);
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void startRecordingScreen(){
        Intent intent = new Intent(this, RecordingScreen.class);
        startActivity(intent);
    }

    private CharSequence[] getProjectList(List<Project> projects){
        CharSequence[] list = new CharSequence[projects.size()+1];
        list[0] = "New Project";
        for(int i = 1; i <= projects.size(); i++){
            Project p = projects.get(i-1);
            CharSequence c = (p.getProject().compareTo("obs") != 0)? p.getTargetLang() + ": " + p.getSlug() : p.getTargetLang() + ": " + "Open Bible Stories";
            list[i] = c;
        }
        for(CharSequence c : list){
            System.out.println(c);
        }
        return list;
    }

    private void addProjectToDatabase(Project project){
        DatabaseHelper db = new DatabaseHelper(this);
        db.addProject(project);
    }

    private void loadProject(Project project){
        pref.edit().putString("resume", "resume").commit();

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
