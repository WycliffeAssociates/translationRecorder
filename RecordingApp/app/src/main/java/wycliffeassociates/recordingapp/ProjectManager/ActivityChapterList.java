package wycliffeassociates.recordingapp.ProjectManager;

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

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.widgets.ChapterCard;

/**
 * Created by sarabiaj on 6/28/2016.
 */
public class ActivityChapterList extends AppCompatActivity implements
        CheckingDialog.DialogListener, CompileDialog.DialogListener, UpdateProgressCallback, DatabaseResyncTaskFragment.DatabaseResyncCallback {

    public static final String STATE_COMPILING = "compiling";
    private static final String TAG_COMPILE_CHAPTER_TASK_FRAGMENT = "tag_compile_chapter";
    private static final String STATE_PROGRESS = "progress";
    public static String PROJECT_KEY = "project_key";
    private final String STATE_RESYNC = "db_resync";
    private final String TAG_DATABASE_RESYNC_FRAGMENT = "database_resync_task_fragment";
    private Chunks mChunks;
    private ProgressDialog mPd;
    private volatile boolean mIsCompiling = false;
    private Project mProject;
    private List<ChapterCard> mChapterCardList;
    private ChapterCardAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mChapterList;
    private CompileChapterTaskFragment mCompileChapterTaskFragment;
    private volatile int mProgress = 0;
    private DatabaseResyncTaskFragment mDatabaseResyncTaskFragment;
    private boolean mDbResyncing;
    private ProgressDialog mDatabaseProgressDialog;

    public static Intent getActivityVerseListIntent(Context ctx, Project p) {
        Intent intent = new Intent(ctx, ActivityUnitList.class);
        intent.putExtra(PROJECT_KEY, p);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);


        FragmentManager fm = getFragmentManager();
        mCompileChapterTaskFragment = (CompileChapterTaskFragment) fm.findFragmentByTag(TAG_COMPILE_CHAPTER_TASK_FRAGMENT);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(STATE_COMPILING)) {
                mProgress = savedInstanceState.getInt(STATE_PROGRESS);
                mIsCompiling = true;
                compileProgress(mProgress);
            }
            mDbResyncing = savedInstanceState.getBoolean(STATE_RESYNC);
        }

        //check if fragment was retained from a screen rotation
        if (mCompileChapterTaskFragment == null) {
            mCompileChapterTaskFragment = new CompileChapterTaskFragment();
            fm.beginTransaction().add(mCompileChapterTaskFragment, TAG_COMPILE_CHAPTER_TASK_FRAGMENT).commit();
            fm.executePendingTransactions();
        }
        if (mDatabaseResyncTaskFragment == null) {
            mDatabaseResyncTaskFragment = new DatabaseResyncTaskFragment();
            fm.beginTransaction().add(mDatabaseResyncTaskFragment, TAG_DATABASE_RESYNC_FRAGMENT).commit();
            fm.executePendingTransactions();
        } else if (mDbResyncing) {
            dbProgress();
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
            dbProgress();
            mDatabaseResyncTaskFragment.resyncDatabase();
        }
    }

    public void dbProgress() {
        mDbResyncing = true;
        mDatabaseProgressDialog = new ProgressDialog(this);
        mDatabaseProgressDialog.setTitle("Resyncing Database");
        mDatabaseProgressDialog.setMessage("Please Wait...");
        mDatabaseProgressDialog.setIndeterminate(true);
        mDatabaseProgressDialog.setCancelable(false);
        mDatabaseProgressDialog.show();
    }

    public void onDatabaseResynced() {
        mDatabaseProgressDialog.dismiss();
        mDbResyncing = false;
        refreshChapterCards();
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

    public void compileProgress(int progress) {
        mPd = new ProgressDialog(this);
        mPd.setTitle("Compiling Chapter");
        mPd.setMessage("Please Wait...");
        mPd.setProgress(progress);
        mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPd.setCancelable(false);
        mPd.setMax(100);
        mPd.show();
    }

    @Override
    public void setIsCompiling() {
        mIsCompiling = true;
        compileProgress(0);
    }

    public void setCompilingProgress(int progress) {
        mProgress = progress;
        mPd.setProgress(progress);
    }

    public void onCompileCompleted(final int[] chaptersModified) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPd.dismiss();
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(ActivityChapterList.this);
                for (int i : chaptersModified) {
                    mAdapter.notifyItemChanged(i);
                    db.setCheckingLevel(mProject, i + 1, 0);
                }
                mIsCompiling = false;
            }
        });
    }

    @Override
    public void onPositiveClick(CheckingDialog dialog) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int level = dialog.getCheckingLevel();
        int[] chapterIndicies = dialog.getChapterIndicies();
        for (int i = 0; i < chapterIndicies.length; i++) {
            int position = chapterIndicies[i];
            mChapterCardList.get(position).setCheckingLevel(level);
            db.setCheckingLevel(dialog.getProject(), position + 1, level);
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
        mCompileChapterTaskFragment.compile(toCompile, dialog.getChapterInicies());
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

}
