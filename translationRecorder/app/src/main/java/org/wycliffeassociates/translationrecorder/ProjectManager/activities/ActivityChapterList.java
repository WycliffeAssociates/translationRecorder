package org.wycliffeassociates.translationrecorder.ProjectManager.activities;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.ChapterCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CompileDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.CompileChapterTask;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ChapterResyncTask;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.utilities.TaskFragment;
import org.wycliffeassociates.translationrecorder.widgets.ChapterCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<Integer, Integer> unitsStarted = db.getNumStartedUnitsInProject(mProject);
        for (int i = 0; i < mChapterCardList.size(); i++) {
            ChapterCard cc = mChapterCardList.get(i);
            int numUnits = (unitsStarted.containsKey(cc.getChapterNumber())) ? unitsStarted.get(cc.getChapterNumber()) : 0;
            cc.setNumOfUnitStarted(numUnits);
            cc.refreshProgress(this);
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
    protected void onPause() {
        super.onPause();
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
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);

        Map<ChapterCard, List<String>> chaptersToCompile = new HashMap<>();
        for (ChapterCard cc : toCompile) {
            chaptersToCompile.put(cc, db.getTakesForChapterCompilation(mProject, cc.getChapterNumber()));
        }

        CompileChapterTask task = new CompileChapterTask(COMPILE_CHAPTER_TASK, chaptersToCompile, mProject);
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
        for (Chapter chapter : chapters) {
            int unitCount = chapter.getChunks().size();
            int chapterNumber = chapter.getNumber();
            mChapterCardList.add(
                    new ChapterCard(
                            mProject,
                            Utils.capitalizeFirstLetter(mChunks.getChapterLabel()) + " " + mChunks.getChapterName(chapterNumber),
                            chapterNumber,
                            unitCount
                    )
            );
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
                    int chapter = mChapterCardList.get(i).getChapterNumber();
                    db.setCheckingLevel(mProject, chapter, 0);
                    mChapterCardList.get(i).compile();
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }
}
