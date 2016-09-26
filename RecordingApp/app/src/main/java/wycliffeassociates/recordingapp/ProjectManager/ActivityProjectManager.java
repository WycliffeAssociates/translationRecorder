package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.FilesPage.Export.ExportTaskFragment;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.SplashScreen;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.project.ProjectWizardActivity;
import wycliffeassociates.recordingapp.utilities.TaskFragment;

/**
 * Created by sarabiaj on 6/23/2016.
 */
public class ActivityProjectManager extends AppCompatActivity implements ProjectInfoDialog.InfoDialogCallback,
                                ProjectInfoDialog.ExportDelegator, Export.ProgressUpdateCallback, DatabaseResyncTaskFragment.DatabaseResyncCallback,
                                ProjectInfoDialog.SourceAudioDelegator, SourceAudioTaskFragment.SourceAudioExportCallback{



    LinearLayout mProjectLayout;
    Button mNewProjectButton;
    ImageView mAddProject;
    ListView mProjectList;
    SharedPreferences pref;
    ListAdapter mAdapter;
    private int mNumProjects = 0;
    Activity mCtx;
    private ProgressDialog mPd;
    private ProgressDialog mDatabaseProgressDialog;
    private volatile int mProgress = 0;
    private volatile boolean mZipping = false;
    private volatile boolean mExporting = false;
    private ExportTaskFragment mExportTaskFragment;
    private DatabaseResyncTaskFragment mDatabaseResyncTaskFragment;
    private SourceAudioTaskFragment mSourceCompileTaskFragment;
    private TaskFragment mTaskFragment;

    private final String TAG_SOURCE_AUDIO_FRAGMENT = "source_audio_task_fragment";
    private final String TAG_EXPORT_TASK_FRAGMENT = "export_task_fragment";
    private final String TAG_DATABASE_RESYNC_FRAGMENT = "database_resync_task_fragment";
    private final String TAG_TASK_FRAGMENT = "task_fragment";
    private final String STATE_EXPORTING = "was_exporting";
    private final String STATE_ZIPPING = "was_zipping";
    private final String STATE_RESYNC = "db_resync";
    private final String STATE_PROGRESS = "upload_progress";

    private final String STATE_SOURCE_COMPILING = "source_compiling";
    public static final int PROJECT_WIZARD_REQUEST = RESULT_FIRST_USER;
    private volatile int mDbProgress = 0;
    private boolean mDbResyncing = false;
    private boolean mSourceCompiling = false;
    private ProgressDialog mSourceCompileProgressDialog;
    private File mSourceAudioFile;

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
        mCtx = this;

        if(savedInstanceState != null) {
            mZipping = savedInstanceState.getBoolean(STATE_ZIPPING, false);
            mExporting = savedInstanceState.getBoolean(STATE_EXPORTING, false);
            mProgress = savedInstanceState.getInt(STATE_PROGRESS, 0);
            mDbResyncing = savedInstanceState.getBoolean(STATE_RESYNC, false);
            mSourceCompiling = savedInstanceState.getBoolean(STATE_SOURCE_COMPILING, false);
        }
    }

    //This code exists here rather than onResume due to the potential for onResume() -> onResume()
    //This scenario occurs when the user begins to create a new project and backs out. Calling onResume()
    //twice will result in two background processes trying to sync the database, and only one reference
    //will be kept in the activity- thus leaking the reference to the first dialog causing in it never closing
    @Override
    protected void onStart(){
        super.onStart();
        //Moved this section to onResume so that these dialogs pop up above the dialog info fragment
        //check if fragment was retained from a screen rotation
        FragmentManager fm = getFragmentManager();
        mExportTaskFragment = (ExportTaskFragment) fm.findFragmentByTag(TAG_EXPORT_TASK_FRAGMENT);
        mDatabaseResyncTaskFragment = (DatabaseResyncTaskFragment) fm.findFragmentByTag(TAG_DATABASE_RESYNC_FRAGMENT);
        mSourceCompileTaskFragment = (SourceAudioTaskFragment) fm.findFragmentByTag(TAG_SOURCE_AUDIO_FRAGMENT);
        if(mExportTaskFragment == null){
            mExportTaskFragment = new ExportTaskFragment();
            fm.beginTransaction().add(mExportTaskFragment, TAG_EXPORT_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        } else {
            if(mZipping){
                zipProgress(mProgress);
            } else if(mExporting){
                exportProgress(mProgress);
            }
        }
        if(mDatabaseResyncTaskFragment == null){
            mDatabaseResyncTaskFragment = new DatabaseResyncTaskFragment();
            fm.beginTransaction().add(mDatabaseResyncTaskFragment, TAG_DATABASE_RESYNC_FRAGMENT).commit();
            fm.executePendingTransactions();
        } else if(mDbResyncing){
            dbProgress();
        }
//        if(mSourceCompileTaskFragment == null){
//            mSourceCompileTaskFragment = new SourceAudioTaskFragment();
//            fm.beginTransaction().add(mSourceCompileTaskFragment, TAG_SOURCE_AUDIO_FRAGMENT).commit();
//            fm.executePendingTransactions();
//        } else if (mSourceCompiling) {
//            sourceCompileProgress();
//        }
        if(mTaskFragment == null){
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        if(!mDbResyncing) {
            dbProgress();
            mDatabaseResyncTaskFragment.resyncDatabase();
        }
    }

    private void sourceCompileProgress() {
//        mSourceCompiling = true;
//        mSourceCompileProgressDialog = new ProgressDialog(this);
//        mSourceCompileProgressDialog.setTitle("Compiling Source Audio");
//        mSourceCompileProgressDialog.setMessage("Please Wait...");
//        mSourceCompileProgressDialog.setIndeterminate(true);
//        mSourceCompileProgressDialog.setCancelable(false);
//        mSourceCompileProgressDialog.show();
    }

    public void dbProgress(){
        mDbResyncing = true;
        mDatabaseProgressDialog = new ProgressDialog(this);
        mDatabaseProgressDialog.setTitle("Resyncing Database");
        mDatabaseProgressDialog.setMessage("Please Wait...");
        mDatabaseProgressDialog.setIndeterminate(true);
        mDatabaseProgressDialog.setCancelable(false);
        mDatabaseProgressDialog.show();
    }

    public void onDatabaseResynced(){
        if(mDatabaseProgressDialog != null) {
            mDatabaseProgressDialog.dismiss();
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            mNumProjects = db.getNumProjects();
            mDbResyncing = false;
            initializeViews();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        if(mPd != null) {
            savedInstanceState.putInt(STATE_PROGRESS, mPd.getProgress());
        }
        savedInstanceState.putBoolean(STATE_EXPORTING, mExporting);
        savedInstanceState.putBoolean(STATE_ZIPPING, mZipping);
        savedInstanceState.putBoolean(STATE_RESYNC, mDbResyncing);
        savedInstanceState.putBoolean(STATE_SOURCE_COMPILING, mSourceCompiling);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews(){
        mProjectLayout = (LinearLayout) findViewById(R.id.project_list_layout);
        mNewProjectButton = (Button) findViewById(R.id.new_project_button);
        mAddProject = (ImageView) findViewById(R.id.new_project_fab);
        mProjectList = (ListView) findViewById(R.id.project_list);

        mAddProject.setOnClickListener(btnClick);
        mNewProjectButton.setOnClickListener(btnClick);

        hideProjectsIfEmpty(mNumProjects);
        if(mNumProjects > 0){
            initializeRecentProject();
            if(mNumProjects > 1) {
                populateProjectList();
            }
        } else {
            mProjectList.setVisibility(View.GONE);
        }
    }

    public void initializeRecentProject(){
        Project project = null;
        if(pref.getString(Settings.KEY_PREF_LANG, "").compareTo("") != 0) {
            project = Project.getProjectFromPreferences(this);
        } else {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            List<Project> projects = db.getAllProjects();
            if(projects.size() > 0){
                project = projects.get(0);
            }
        }
        if(project != null) {
            ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
            ProjectAdapter.initializeProjectCard(this, project, db, findViewById(R.id.recent_project));
        } else {
            findViewById(R.id.recent_project).setVisibility(View.GONE);
        }
    }

    public void hideProjectsIfEmpty(int numProjects){
        if(numProjects > 0){
            mNewProjectButton.setVisibility(View.GONE);
        } else {
            mProjectLayout.setVisibility(View.GONE);
            mNewProjectButton.setVisibility(View.VISIBLE);
        }
    }

    private void removeProjectFromPreferences(Project project){
        Map<String, ?> vals = pref.getAll();
        String prefLang = (String)vals.get(Settings.KEY_PREF_LANG);
        String prefSource = (String)vals.get(Settings.KEY_PREF_SOURCE);
        String prefProject = (String)vals.get(Settings.KEY_PREF_PROJECT);
        String prefBook = (String)vals.get(Settings.KEY_PREF_BOOK);

        if (prefLang != null && prefLang.equals(project.getTargetLanguage())) {
            pref.edit().putString(Settings.KEY_PREF_LANG, "").commit();
        }
        if (prefSource != null && prefSource.equals(project.getSource())) {
            pref.edit().putString(Settings.KEY_PREF_SOURCE, "").commit();
        }
        if (prefProject != null && prefProject.equals(project.getProject())){
            pref.edit().putString(Settings.KEY_PREF_PROJECT, "").commit();
        }
        if (prefBook != null && prefBook.equals(project.getSlug())) {
            pref.edit().putString(Settings.KEY_PREF_BOOK, "").commit();
        }
    }

    private void populateProjectList(){
        final ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        final List<Project> projects = db.getAllProjects();
        mAdapter = new ProjectAdapter(this, projects);

        mProjectList.setAdapter(mAdapter);
    }

    //sets the profile in the preferences to "" then returns to the splash screen
    private void logout(){
        pref.edit().putString(Settings.KEY_PROFILE, "").commit();
        finishAffinity();
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    private void createNewProject(){
        startActivityForResult(new Intent(getBaseContext(), ProjectWizardActivity.class), PROJECT_WIZARD_REQUEST);
    }

    private void loadProject(Project project){
        pref.edit().putString("resume", "resume").commit();

        pref.edit().putString(Settings.KEY_PREF_BOOK, project.getSlug()).commit();
        pref.edit().putString(Settings.KEY_PREF_BOOK_NUM, project.getBookNumber()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG, project.getTargetLanguage()).commit();
        pref.edit().putString(Settings.KEY_PREF_SOURCE, project.getSource()).commit();
        pref.edit().putString(Settings.KEY_PREF_PROJECT, project.getProject()).commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK_VERSE, project.getMode()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG_SRC, project.getSourceLanguage()).commit();

        //FIXME: find the last place worked on?
        pref.edit().putString(Settings.KEY_PREF_CHAPTER, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_START_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_END_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK, "1").commit();
    }

    private void addProjectToDatabase(Project project){
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.addProject(project);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PROJECT_WIZARD_REQUEST:{
                if(resultCode == RESULT_OK){
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    addProjectToDatabase(project);
                    loadProject(project);
                    finish();
                    //TODO: should find place left off at?
                    Intent intent = RecordingScreen.getNewRecordingIntent(this, project, 1, 1);
                    startActivity(intent);
                } else {
                    onResume();
                }
            }
            case 42:{
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    try {
                        ParcelFileDescriptor destFilename = getContentResolver().openFileDescriptor(uri, "w");
                        InputStream sourceStream = new FileInputStream(mSourceAudioFile);
                        OutputStream outStream = new FileOutputStream(destFilename.getFileDescriptor());

                        byte[] buffer = new byte[1024];
                        int length;

                        while((length = sourceStream.read(buffer)) > 0)
                        {
                            outStream.write(buffer, 0, length);
                        }

                        outStream.flush();
                        sourceStream.close();
                        outStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
                        Project.deleteProject(mCtx, project);
                        populateProjectList();
                        hideProjectsIfEmpty(mAdapter.getCount());
                        removeProjectFromPreferences(project);
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

    public void exportProgress(int progress){
        mPd = new ProgressDialog(this);
        mPd.setTitle("Uploading...");
        mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPd.setProgress(progress);
        mPd.setCancelable(false);
        mPd.show();
    }

    public void zipProgress(int progress){
        mPd = new ProgressDialog(this);
        mPd.setTitle("Packaging files to export.");
        mPd.setMessage("Please wait...");
        mPd.setProgress(progress);
        mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPd.setCancelable(false);
        mPd.show();
    }

    public void dismissProgress(){
        mPd.dismiss();
    }

    public void incrementProgress(int progress){
        mPd.incrementProgressBy(progress);
    }

    public void setUploadProgress(int progress){
        mPd.setProgress(progress);
    }

    public void showProgress(boolean mode){
        if(mode == true){
            zipProgress(0);
        } else {
            exportProgress(0);
        }
    }

    @Override
    public void setZipping(boolean zipping){
        mZipping = zipping;
    }

    @Override
    public void setExporting(boolean exporting){
        mExporting = exporting;
    }

    @Override
    public void setCurrentFile(String currentFile) {
        mPd.setMessage(currentFile);
    }

    @Override
    public void onPause(){
        super.onPause();
        dismissExportProgressDialog();
        dismissDatabaseResyncProgressDialog();
        dismissSourceCompileProgressDialog();
    }

    private void dismissExportProgressDialog(){
        if(mPd != null && mPd.isShowing()){
            mPd.dismiss();
            mPd = null;
        }
    }

    private void dismissDatabaseResyncProgressDialog(){
        if(mDatabaseProgressDialog != null && mDatabaseProgressDialog.isShowing()){
            mDatabaseProgressDialog.dismiss();
            mDatabaseProgressDialog = null;
        }
    }

    private void dismissSourceCompileProgressDialog(){
        if(mSourceCompileProgressDialog != null && mSourceCompileProgressDialog.isShowing()){
            mSourceCompileProgressDialog.dismiss();
            mSourceCompileProgressDialog = null;
        }
    }

    @Override
    public void delegateExport(Export exp) {
        exp.setFragmentContext(mExportTaskFragment);
        mExportTaskFragment.delegateExport(exp);
    }

    @Override
    public void delegateSourceAudio(Project project) {
        mSourceAudioFile = new File(getFilesDir(), project.getTargetLanguage() + Utils.capitalizeFirstLetter(project.getSlug()) + ".tr");
        ExportSourceAudioTask task = new ExportSourceAudioTask(project, project.getProjectDirectory(project), getFilesDir(), mSourceAudioFile);
        task.setActivity(this);
        mTaskFragment.executeRunnable(task, "Exporting Source Audio in Task Fragment", "Please wait...");
        //mSourceAudioTaskFragment.createSourceAudio(project, project.getProjectDirectory(project), getFilesDir());
        //sourceCompileProgress();
    }

    @Override
    public void onSourceAudioExported() {
        //dismissSourceCompileProgressDialog();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, mSourceAudioFile.getName());
        startActivityForResult(intent, 42);
    }
}
