package org.wycliffeassociates.translationrecorder.ProjectManager.activities;

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

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CompileDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.CompileChapterTask;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ChapterResyncTask;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.utilities.TaskFragment;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.util.ArrayList;
import java.util.List;

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
    private ChunkPlugin mChunks;
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

    public static Intent getActivityUnitListIntent(Context ctx, Project p) {
        Intent intent = new Intent(ctx, ActivityUnitList.class);
        intent.putExtra(Project.PROJECT_EXTRA, p);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        FragmentManager fm = getFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
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
        String language = mDb.getLanguageName(mProject.getTargetLanguageSlug());
        String book = mDb.getBookName(mProject.getBookSlug());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chapter_list_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(language + " - " + book);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        try {
            mChunks = mProject.getChunkPlugin(new ChunkPluginLoader(this));
        } catch (Exception e) {
            Logger.e(this.toString(), "Error parsing chunks", e);
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

        //if the database is still resyncing from a previous orientation change, don't start a new one
        if (!mDbResyncing) {
            mDbResyncing = true;
            ChapterResyncTask task = new ChapterResyncTask(
                    DATABASE_RESYNC_TASK,
                    getBaseContext(),
                    getFragmentManager(),
                    mProject
            );
            mTaskFragment.executeRunnable(task, "Resyncing Database", "Please wait...", true);
        }
    }

    public void refreshChapterCards() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int numChapters = mChunks.numChapters();
        int[] unitsStarted = db.getNumStartedUnitsInProject(mProject, numChapters);
        for (int i = 0; i < mChapterCardList.size(); i++) {
            ChapterCard cc = mChapterCardList.get(i);
            cc.setNumOfUnitStarted(unitsStarted[i]);
            cc.refreshProgress();
            cc.refreshIsEmpty();
            cc.refreshCanCompile();
            cc.refreshChapterCompiled(cc.getChapterNumber());
            if (cc.isCompiled()) {
                cc.setCheckingLevel(db.getChapterCheckingLevel(mProject, cc.getChapterNumber()));
            }
        }
        db.close();
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
        int level = dialog.getCheckingLevel();
        int[] chapterIndicies = dialog.getChapterIndicies();
        for (int i = 0; i < chapterIndicies.length; i++) {
            int position = chapterIndicies[i];
            ChapterCard cc = mChapterCardList.get(position);
            cc.setCheckingLevel(level);
            db.setCheckingLevel(dialog.getProject(), cc.getChapterNumber(), level);
            mAdapter.notifyItemChanged(position);
        }
        db.close();
    }

    @Override
    public void onPositiveClick(CompileDialog dialog) {
        List<ChapterCard> toCompile = new ArrayList<>();
        for (int i : dialog.getChapterIndicies()) {
            toCompile.add(mChapterCardList.get(i));
            mChapterCardList.get(i).destroyAudioPlayer();
        }
        mChaptersCompiled = dialog.getChapterIndicies();
        CompileChapterTask task = new CompileChapterTask(COMPILE_CHAPTER_TASK, toCompile);
        mTaskFragment.executeRunnable(task, "Compiling Chapter", "Please wait...", false);
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
        List<Chapter> chapters = mChunks.getChapters();
        for(Chapter chapter : chapters) {
            int unitCount = chapter.getChunks().size();
            mChapterCardList.add(new ChapterCard(this, mProject, chapter.getNumber(), unitCount));
        }
    }

    @Override
    public void onTaskComplete(int taskTag, int resultCode) {
        if (resultCode == TaskFragment.STATUS_OK) {
            if (taskTag == DATABASE_RESYNC_TASK) {
                mDbResyncing = false;
                refreshChapterCards();
            } else if (taskTag == COMPILE_CHAPTER_TASK) {
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(ActivityChapterList.this);
                for (int i : mChaptersCompiled) {
                    db.setCheckingLevel(mProject, i + 1, 0);
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }
}
