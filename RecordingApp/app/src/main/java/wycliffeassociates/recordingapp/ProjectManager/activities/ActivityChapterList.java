package wycliffeassociates.recordingapp.ProjectManager.activities;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.adapters.ChapterCardAdapter;
import wycliffeassociates.recordingapp.ProjectManager.dialogs.CheckingDialog;
import wycliffeassociates.recordingapp.ProjectManager.dialogs.CompileDialog;
import wycliffeassociates.recordingapp.ProjectManager.tasks.CompileChapterTask;
import wycliffeassociates.recordingapp.ProjectManager.tasks.DatabaseResyncTask;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.utilities.Task;
import wycliffeassociates.recordingapp.utilities.TaskFragment;
import wycliffeassociates.recordingapp.widgets.ChapterCard;

/**
 * Created by sarabiaj on 6/28/2016.
 */
public class ActivityChapterList extends AppCompatActivity implements
        CheckingDialog.DialogListener, CompileDialog.DialogListener, TaskFragment.OnTaskComplete {
    public static final String STATE_COMPILING = "compiling";
    private static final String STATE_PROGRESS = "progress";
    public static String PROJECT_KEY = "project_key";
    private final String STATE_RESYNC = "db_resync";
    private final String TAG_TASK_FRAGMENT = "task_fragment";
    private Chunks mChunks;
    private ProgressDialog mPd;
    private volatile boolean mIsCompiling = false;
    private Project mProject;
    private List<ChapterCard> mChapterCardList;
    private ChapterCardAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mChapterList;
    private volatile int mProgress = 0;
    private boolean mDbResyncing;
    private TaskFragment mTaskFragment;
    private static final int DATABASE_RESYNC_TASK = Task.FIRST_TASK;
    private static final int COMPILE_CHAPTER_TASK = Task.FIRST_TASK + 1;
    private int[] mChaptersCompiled;

        Intent intent = new Intent(ctx, ActivityUnitList.class);
        intent.putExtra(PROJECT_KEY, p);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        FragmentManager fm = getFragmentManager();
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }

        if (savedInstanceState != null) {
            mDbResyncing = savedInstanceState.getBoolean(STATE_RESYNC);
        }

        // Setup toolbar
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        ProjectDatabaseHelper mDb = new ProjectDatabaseHelper(this);
        String language = mDb.getLanguageName(mProject.getTargetLanguage());
        String book = mDb.getBookName(mProject.getSlug());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chapter_list_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(language + " - " + book);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        try {
            mChunks = new Chunks(this, mProject.getSlug());
        } catch (Exception e) {

        }

        // Find the recycler view
        mChapterList = (RecyclerView) findViewById(R.id.chapter_list);
        mChapterList.setHasFixedSize(false);

        // Set its layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mChapterList.setLayoutManager(mLayoutManager);

        // Set its adapter
        mChapterCardList = new ArrayList<>();
        mAdapter = new ChapterCardAdapter(this, mProject, mChapterCardList);
        mChapterList.setAdapter(mAdapter);

        // Set its animator
        mChapterList.setItemAnimator(new DefaultItemAnimator());

        prepareChapterCardData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mDbResyncing) {
            mDbResyncing = true;
            DatabaseResyncTask task = new DatabaseResyncTask(DATABASE_RESYNC_TASK, getBaseContext());
            mTaskFragment.executeRunnable(task, "Resyncing Database", "Please wait...");
        }
    }

    public void refreshChapterCards() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int numChapters = mChunks.getNumChapters();
        int[] numStarted = db.getNumStartedUnitsInProject(mProject, numChapters);
        for (int i = 0; i < mChapterCardList.size(); i++) {
            mChapterCardList.get(i).refreshChapterStarted(mProject, i + 1);
            mChapterCardList.get(i).setCanCompile(numStarted[i] == mChunks.getNumChunks(mProject, i + 1));
            mChapterCardList.get(i).refreshChapterCompiled(mProject, i + 1);
            if (mChapterCardList.get(i).isCompiled()) {
                mChapterCardList.get(i).setCheckingLevel(db.getChapterCheckingLevel(mProject, i + 1));
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.exitCleanUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPd != null && mPd.isShowing()) {
            mPd.dismiss();
            mPd = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putBoolean(STATE_COMPILING, mIsCompiling);
        saveInstanceState.putInt(STATE_PROGRESS, mProgress);
        saveInstanceState.putBoolean(STATE_RESYNC, mDbResyncing);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public void onPositiveClick(CheckingDialog dialog) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int[] chapterIndicies = dialog.getChapterIndicies();
        for (int i = 0; i < chapterIndicies.length; i++) {
            int position = chapterIndicies[i];
            db.setCheckingLevel(dialog.getProject(), position + 1, dialog.getCheckingLevel());
            mAdapter.notifyItemChanged(position);
        }
        db.close();
    }

    @Override
    public void onPositiveClick(CompileDialog dialog) {
        List<ChapterCard> toCompile = new ArrayList<>();
        for (int i : dialog.getChapterInicies()) {
            toCompile.add(mChapterCardList.get(i));
            mChapterCardList.get(i).destroyAudioPlayer();
        }
        mChaptersCompiled = dialog.getChapterInicies();
        CompileChapterTask task = new CompileChapterTask(COMPILE_CHAPTER_TASK, toCompile);
        mTaskFragment.executeRunnable(task, "Compiling Chapter", "Please wait...");
    }

    @Override
    public void onNegativeClick(CheckingDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onNegativeClick(CompileDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareChapterCardData() {
        for (int i = 0; i < mChunks.getNumChapters(); i++) {
            mChapterCardList.add(new ChapterCard(this, mProject, i + 1));
        }
    }

    @Override
    public void onTaskComplete(int taskTag, int resultCode) {
        if(resultCode == TaskFragment.STATUS_OK){
            if(taskTag == DATABASE_RESYNC_TASK){
                mDbResyncing = false;
                refreshChapterCards();
            } else if (taskTag == COMPILE_CHAPTER_TASK){
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(ActivityChapterList.this);
                for (int i : mChaptersCompiled) {
                    mAdapter.notifyItemChanged(i);
                    db.setCheckingLevel(mProject, i + 1, 0);
                }
            }
        }
    }
}
