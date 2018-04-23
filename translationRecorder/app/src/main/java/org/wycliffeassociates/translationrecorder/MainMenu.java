package org.wycliffeassociates.translationrecorder;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.door43.tools.reporting.GithubReporter;
import com.door43.tools.reporting.GlobalExceptionHandler;
import com.door43.tools.reporting.Logger;

import org.apache.commons.io.FileUtils;
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityProjectManager;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.Reporting.BugReportDialog;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.data.model.Project;
import org.wycliffeassociates.translationrecorder.data.model.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.data.model.ProjectSlugs;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.persistence.repository.RoomDb;
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.BookDao;
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.LanguageDao;
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.ProjectDao;
import org.wycliffeassociates.translationrecorder.project.ProjectWizardActivity;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Activity {

    private RelativeLayout btnRecord;
    private ImageButton btnFiles;
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

    private ProjectDatabaseHelper mDb;
    private ProjectDao projectDb;
    private BookDao bookDb;
    private LanguageDao languageDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDb = new ProjectDatabaseHelper(this);
        RoomDb rdb = RoomDb.Companion.getInstance(getApplicationContext());
        if(rdb != null) {
            projectDb = rdb.projectDao();
            bookDb = rdb.bookDao();
            languageDb = rdb.languageDao();
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        AudioInfo.SCREEN_WIDTH = metrics.widthPixels;

        System.out.println("internal files dir is " + this.getCacheDir());
        System.out.println("External files dir is " + Environment.getExternalStorageDirectory());

        initApp();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNumProjects = projectDb.getProjects().size();

        btnRecord = (RelativeLayout) findViewById(R.id.new_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNumProjects <= 0 || emptyPreferences()) {
                    setupNewProject();
                } else {
                    startRecordingScreen();
                }
            }
        });

        btnFiles = (ImageButton) findViewById(R.id.files);
        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ActivityProjectManager.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
            }
        });

        initViews();
    }

    private boolean emptyPreferences() {
        if (pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1) == -1) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PROJECT_WIZARD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    if (addProjectToDatabase(project)) {
                        loadProject(project);
                        Intent intent = RecordingActivity.getNewRecordingIntent(
                                this,
                                project,
                                ChunkPlugin.DEFAULT_CHAPTER,
                                ChunkPlugin.DEFAULT_UNIT
                        );
                        startActivity(intent);
                    } else {
                        onResume();
                    }
                } else {
                    onResume();
                }
            }
            default:
        }
    }

    private void setupNewProject() {
        startActivityForResult(new Intent(getBaseContext(), ProjectWizardActivity.class), PROJECT_WIZARD_REQUEST);
    }

    private void startRecordingScreen() {
        Project project = Project.getProjectFromPreferences(this);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int chapter = pref.getInt(Settings.KEY_PREF_CHAPTER, ChunkPlugin.DEFAULT_CHAPTER);
        int unit = pref.getInt(Settings.KEY_PREF_CHUNK, ChunkPlugin.DEFAULT_UNIT);
        Intent intent = RecordingActivity.getNewRecordingIntent(
                this,
                project,
                chapter,
                unit
        );
        startActivity(intent);
    }

    private boolean addProjectToDatabase(Project project) {
        if (projectDb.getById(project.)) {
            ProjectWizardActivity.displayProjectExists(this);
            return false;
        } else {
            mDb.addProject(project);
            return true;
        }
    }


    private void loadProject(Project project) {
        pref.edit().putString("resume", "resume").commit();

        if (mDb.projectExists(project)) {
            pref.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, mDb.getProjectId(project)).commit();
        } else {
            Logger.e(this.toString(), "Project " + project + " doesn't exist in the database");
        }
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

    private void reportCrash(String message) {
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

    public void archiveStackTraces() {
        File dir = new File(getExternalCacheDir(), STACKTRACE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File archive = new File(dir, "Archive");
        if (!archive.exists()) {
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

    private void initViews() {
        int projectId = pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1);
        TextView languageView = (TextView) findViewById(R.id.language_view);
        TextView bookView = (TextView) findViewById(R.id.book_view);
        if (projectId != -1) {
            Project project = projectDb.getProject(projectId);
            String language = project.getLanguage().getName();
            languageView.setText(language);

            String book = project.getBookName();
            bookView.setText(book);
        } else {
            languageView.setText("");
            bookView.setText("");
        }
    }

    private void initApp() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString("version", BuildConfig.VERSION_NAME).commit();

        //set up Visualization folder
        Utils.VISUALIZATION_DIR = new File(getExternalCacheDir(), "Visualization");
        Utils.VISUALIZATION_DIR.mkdirs();

        //if the current directory is already set, then don't overwrite it
        if (pref.getString("current_directory", null) == null) {
            pref.edit().putString("current_directory",
                    Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit();
        }
        pref.edit().putString("root_directory", Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit();

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

        removeUnusedVisualizationFiles();
    }

    private void removeUnusedVisualizationFiles() {
        File visFilesLocation = Utils.VISUALIZATION_DIR;
        File[] visFiles = visFilesLocation.listFiles();
        if (visFiles == null) {
            return;
        }
        String rootPath = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder").getAbsolutePath();
        List<Project> projects = projectDb.getAllProjects();
        List<ProjectPatternMatcher> patterns = new ArrayList<>();
        for(Project project : projects) {
            patterns.add(project.getPatternMatcher());
        }
        for (File v : visFiles) {
            boolean matched = false;
            TakeInfo takeInfo = null;
            //no idea what project the vis file is, so try all known anthology regexes until one works
            for (ProjectPatternMatcher ppm : patterns) {
                if (ppm.match(v)) {
                    matched = true;
                    takeInfo = ppm.getTakeInfo();
                    break;
                }
            }
            if (!matched) {
                v.delete();
                continue;
            }
            boolean found = false;
            ProjectSlugs slugs = takeInfo.getProjectSlugs();
            String path = rootPath + "/" + slugs.getLanguage() + "/" + slugs.getVersion() + "/" + slugs.getBook() + "/" + String.format("%02d", takeInfo.getChapter());
            String visFileWithoutExtension = v.getName().split(".vis$")[0];
            String name = visFileWithoutExtension + "_t" + String.format("%02d", takeInfo.getTake()) + ".wav";
            File searchName = new File(path, name);
            if (searchName != null && searchName.exists()) {
                //check if the names match up; exclude the path to get to them or the file extention
                if (extractFilename(searchName).equals(extractFilename(v))) {
                    continue;
                }
            }
            if (!found) {
                System.out.println("Removing " + v.getName());
                v.delete();
            }
        }
    }

    private String extractFilename(File a) {
        if (a.isDirectory()) {
            return "";
        }
        String nameWithExtention = a.getName();
        if (nameWithExtention.lastIndexOf('.') < 0 || nameWithExtention.lastIndexOf('.') > nameWithExtention.length()) {
            return "";
        }
        String filename = nameWithExtention.substring(0, nameWithExtention.lastIndexOf('.'));
        return filename;
    }


    public void configureLogger(int minLogLevel, File logDir) {
        File logFile = new File(logDir, "log.txt");
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.configure(logFile, Logger.Level.getLevel(minLogLevel));
        if (logFile.exists()) {
            Logger.w(this.toString(), "SUCCESS: Log file initialized.");
        } else {
            Logger.e(this.toString(), "ERROR: could not initialize log file.");
        }
    }
}
