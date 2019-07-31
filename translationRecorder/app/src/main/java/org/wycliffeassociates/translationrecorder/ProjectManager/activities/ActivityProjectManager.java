package org.wycliffeassociates.translationrecorder.ProjectManager.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.DocumentationActivity;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.Export;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.ExportTaskFragment;
import org.wycliffeassociates.translationrecorder.FilesPage.FeedbackDialog;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ProjectAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.ProjectInfoDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.ExportSourceAudioTask;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ProjectListResyncTask;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.SplashScreen;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectWizardActivity;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.utilities.TaskFragment;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 6/23/2016.
 */
public class ActivityProjectManager extends AppCompatActivity implements ProjectInfoDialog.InfoDialogCallback,
        ProjectInfoDialog.ExportDelegator, Export.ProgressUpdateCallback,
        ProjectInfoDialog.SourceAudioDelegator, TaskFragment.OnTaskComplete {

    LinearLayout mProjectLayout;
    Button mNewProjectButton;
    ImageView mAddProject;
    ListView mProjectList;
    SharedPreferences pref;
    ListAdapter mAdapter;
    private int mNumProjects = 0;
    private ProgressDialog mPd;
    private volatile int mProgress = 0;
    private volatile String mProgressTitle = null;
    private volatile boolean mZipping = false;
    private volatile boolean mExporting = false;
    private ExportTaskFragment mExportTaskFragment;
    private TaskFragment mTaskFragment;

    public static final int SOURCE_AUDIO_TASK = Task.FIRST_TASK;
    private static final int DATABASE_RESYNC_TASK = Task.FIRST_TASK + 1;
    public static final int EXPORT_TASK = Task.FIRST_TASK + 2;

    private final String TAG_EXPORT_TASK_FRAGMENT = "export_task_fragment";
    private final String TAG_TASK_FRAGMENT = "task_fragment";
    private final String STATE_EXPORTING = "was_exporting";
    private final String STATE_ZIPPING = "was_zipping";
    private final String STATE_RESYNC = "db_resync";

    private final String STATE_PROGRESS = "upload_progress";
    private final String STATE_PROGRESS_TITLE = "upload_progress_title";
    public static final int PROJECT_WIZARD_REQUEST = RESULT_FIRST_USER;
    public static final int SAVE_SOURCE_AUDIO_REQUEST = RESULT_FIRST_USER + 1;
    private boolean mDbResyncing = false;
    private File mSourceAudioFile;
    private Project mProjectToExport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_management);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.project_management_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Project Management");
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            mZipping = savedInstanceState.getBoolean(STATE_ZIPPING, false);
            mExporting = savedInstanceState.getBoolean(STATE_EXPORTING, false);
            mProgress = savedInstanceState.getInt(STATE_PROGRESS, 0);
            mProgressTitle = savedInstanceState.getString(STATE_PROGRESS_TITLE, null);
            mDbResyncing = savedInstanceState.getBoolean(STATE_RESYNC, false);
        }
    }

    //This code exists here rather than onResume due to the potential for onResume() -> onResume()
    //This scenario occurs when the user begins to create a new project and backs out. Calling onResume()
    //twice will result in two background processes trying to sync the database, and only one reference
    //will be kept in the activity- thus leaking the reference to the first dialog causing in it never closing
    @Override
    protected void onStart() {
        super.onStart();
        //Moved this section to onResume so that these dialogs pop up above the dialog info fragment
        //check if fragment was retained from a screen rotation
        FragmentManager fm = getFragmentManager();
        mExportTaskFragment = (ExportTaskFragment) fm.findFragmentByTag(TAG_EXPORT_TASK_FRAGMENT);
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        //TODO: refactor export to fit the new taskfragment
        if (mExportTaskFragment == null) {
            mExportTaskFragment = new ExportTaskFragment();
            fm.beginTransaction().add(mExportTaskFragment, TAG_EXPORT_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        } else {
            if (mZipping) {
                zipProgress(mProgress, mProgressTitle);
            } else if (mExporting) {
                exportProgress(mProgress, mProgressTitle);
            }
        }
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        //still need to track whether a db resync was issued so as to not issue them in the middle of another
        if (!mDbResyncing) {
            mDbResyncing = true;
            ProjectListResyncTask task = new ProjectListResyncTask(DATABASE_RESYNC_TASK, getBaseContext(), getFragmentManager());
            mTaskFragment.executeRunnable(task, "Resyncing Database", "Please wait...", true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mPd != null) {
            savedInstanceState.putInt(STATE_PROGRESS, mPd.getProgress());
        }
        savedInstanceState.putString(STATE_PROGRESS_TITLE, mProgressTitle);
        savedInstanceState.putBoolean(STATE_EXPORTING, mExporting);
        savedInstanceState.putBoolean(STATE_ZIPPING, mZipping);
        savedInstanceState.putBoolean(STATE_RESYNC, mDbResyncing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                Intent help = new Intent(this, DocumentationActivity.class);
                startActivity(help);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews() {
        mProjectLayout = (LinearLayout) findViewById(R.id.project_list_layout);
        mNewProjectButton = (Button) findViewById(R.id.new_project_button);
        mAddProject = (ImageView) findViewById(R.id.new_project_fab);
        mProjectList = (ListView) findViewById(R.id.project_list);

        mAddProject.setOnClickListener(btnClick);
        mNewProjectButton.setOnClickListener(btnClick);

        hideProjectsIfEmpty(mNumProjects);
        if (mNumProjects > 0) {
            Project recent = initializeRecentProject();
            populateProjectList(recent);
        }
    }


    //Returns the project that was initialized
    public Project initializeRecentProject() {
        Project project = null;
        int projectId = pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1);
        if (projectId != -1) {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            project = db.getProject(projectId);
            Logger.w(this.toString(), "Recent Project: language " + project.getTargetLanguageSlug()
                    + " book " + project.getBookSlug() + " version "
                    + project.getVersionSlug() + " mode " + project.getModeSlug());
        } else {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            List<Project> projects = db.getAllProjects();
            if (projects.size() > 0) {
                project = projects.get(0);
            }
        }
        if (project != null) {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            ProjectAdapter.initializeProjectCard(this, project, db, findViewById(R.id.recent_project));
            return project;
        } else {
            findViewById(R.id.recent_project).setVisibility(View.GONE);
            return null;
        }
    }

    public void hideProjectsIfEmpty(int numProjects) {
        if (numProjects > 0) {
            mNewProjectButton.setVisibility(View.GONE);
        } else {
            mProjectLayout.setVisibility(View.GONE);
            mNewProjectButton.setVisibility(View.VISIBLE);
        }
    }

    private void removeProjectFromPreferences() {
        pref.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, -1).commit();
    }

    private void populateProjectList(Project recent) {
        final ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        List<Project> projects = db.getAllProjects();
        if (recent != null) {
            for (int i = 0; i < projects.size(); i++) {
                if (recent.equals(projects.get(i))) {
                    projects.remove(i);
                    break;
                }
            }
        }
        for (Project p : projects) {
            Logger.w(this.toString(), "Project: language " + p.getTargetLanguageSlug() + " book " + p.getBookSlug() + " version " + p.getVersionSlug() + " mode " + p.getModeSlug());
        }
        mAdapter = new ProjectAdapter(this, projects);
        mProjectList.setAdapter(mAdapter);
    }

    //sets the profile in the preferences to "" then returns to the splash screen
    private void logout() {
        pref.edit().remove(Settings.KEY_PROFILE).commit();
        finishAffinity();
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    private void createNewProject() {
        startActivityForResult(new Intent(getBaseContext(), ProjectWizardActivity.class), PROJECT_WIZARD_REQUEST);
    }

    private void loadProject(Project project) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        if (!db.projectExists(project)) {
            Logger.e(this.toString(), "Project " + project + " does not exist");
        }
        int projectId = db.getProjectId(project);
        pref.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, projectId).commit();
    }

    private boolean addProjectToDatabase(Project project) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        if (db.projectExists(project)) {
            ProjectWizardActivity.displayProjectExists(this);
            return false;
        } else {
            db.addProject(project);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PROJECT_WIZARD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    if (addProjectToDatabase(project)) {
                        loadProject(project);
                        finish();
                        //TODO: should find place left off at?
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
                break;
            }
            case SAVE_SOURCE_AUDIO_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    OutputStream fos = null;
                    BufferedOutputStream bos = null;
                    try {
                        fos = getContentResolver().openOutputStream(uri, "w");
                        bos = new BufferedOutputStream(fos);
                        //sending output streams to the task to run in a thread means they cannot be closed in a finally block here
                        ExportSourceAudioTask task = new ExportSourceAudioTask(SOURCE_AUDIO_TASK, mProjectToExport, ProjectFileUtils.getProjectDirectory(mProjectToExport), getFilesDir(), bos);
                        mTaskFragment.executeRunnable(task, "Exporting Source Audio", "Please wait...", false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            default:
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.new_project_button:
                case R.id.new_project_fab:
                    createNewProject();
                    break;
            }
        }
    };

    @Override
    public void onDelete(final Project project) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Delete Project")
                .setMessage("Deleting this project will remove all associated verse and chunk " +
                        "recordings.\n\nAre you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == dialog.BUTTON_POSITIVE) {
                            Logger.w(this.toString(), "Delete Project: language " + project.getTargetLanguageSlug()
                                    + " book " + project.getBookSlug() + " version "
                                    + project.getVersionSlug() + " mode " + project.getModeSlug());
                            if (project.equals(Project.getProjectFromPreferences(ActivityProjectManager.this))) {
                                removeProjectFromPreferences();
                            }
                            ProjectFileUtils.deleteProject(ActivityProjectManager.this, project);
                            hideProjectsIfEmpty(mAdapter.getCount());
                            mNumProjects--;
                            initializeViews();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void exportProgress(int progress, String title) {
        mPd = new ProgressDialog(this);
        mPd.setTitle(title != null ? title : "Uploading...");
        mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPd.setProgress(progress);
        mPd.setCancelable(false);
        mPd.show();
    }

    public void zipProgress(int progress, String title) {
        mPd = new ProgressDialog(this);
        mPd.setTitle(title != null ? title : "Packaging files to export.");
        mPd.setMessage("Please wait...");
        mPd.setProgress(progress);
        mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPd.setCancelable(false);
        mPd.show();
    }

    public void dismissProgress() {
        if(mPd != null) {
            mPd.dismiss();
        }
    }

    public void incrementProgress(int progress) {
        if(mPd != null) {
            mPd.incrementProgressBy(progress);
        }
    }

    public void setUploadProgress(int progress) {
        if(mPd != null) {
            mPd.setProgress(progress);
        }
    }

    public void showProgress(boolean mode) {
        if (mode == true) {
            zipProgress(0, mProgressTitle);
        } else {
            exportProgress(0, mProgressTitle);
        }
    }

    @Override
    public void setZipping(boolean zipping) {
        mZipping = zipping;
    }

    @Override
    public void setExporting(boolean exporting) {
        mExporting = exporting;
    }

    @Override
    public void setCurrentFile(String currentFile) {
        if(mPd != null) {
            mPd.setMessage(currentFile);
        }
    }

    @Override
    public void setProgressTitle(String title) {
        if (mPd != null) {
            mProgressTitle = title;
            mPd.setTitle(mProgressTitle);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissExportProgressDialog();
    }

    private void dismissExportProgressDialog() {
        if (mPd != null && mPd.isShowing()) {
            mPd.dismiss();
            mPd = null;
        }
    }

    @Override
    public void delegateExport(Export exp) {
        exp.setFragmentContext(mExportTaskFragment);
        mExportTaskFragment.delegateExport(exp);
    }

    @Override
    public void delegateSourceAudio(Project project) {
        mProjectToExport = project;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        mSourceAudioFile = new File(getFilesDir(), project.getTargetLanguageSlug() + "_" + project.getVersionSlug() + "_" + project.getBookSlug() + ".tr");
        intent.putExtra(Intent.EXTRA_TITLE, mSourceAudioFile.getName());
        startActivityForResult(intent, SAVE_SOURCE_AUDIO_REQUEST);
    }

    @Override
    public void onTaskComplete(int taskTag, int resultCode) {
        if (resultCode == TaskFragment.STATUS_OK) {
            if (taskTag == DATABASE_RESYNC_TASK) {
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
                mNumProjects = db.getNumProjects();
                mDbResyncing = false;
                initializeViews();
            } else if(taskTag == SOURCE_AUDIO_TASK) {
                FeedbackDialog fd = FeedbackDialog.newInstance("Source Audio", "Source Audio generation complete.");
                fd.show(getFragmentManager(), "SOURCE_AUDIO");
            }
        } else if(resultCode == TaskFragment.STATUS_ERROR) {
            if(taskTag == SOURCE_AUDIO_TASK) {
                FeedbackDialog fd = FeedbackDialog.newInstance("Source Audio", "Source Audio generation failed.");
                fd.show(getFragmentManager(), "SOURCE_AUDIO");
            }
        }
    }
}
