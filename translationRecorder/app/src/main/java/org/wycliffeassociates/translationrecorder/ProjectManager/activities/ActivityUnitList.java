package org.wycliffeassociates.translationrecorder.ProjectManager.activities;

import android.app.FragmentManager;
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

import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.adapters.UnitCardAdapter;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.CheckingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RatingDialog;
import org.wycliffeassociates.translationrecorder.ProjectManager.tasks.ResyncProjectTask;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Chunks;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.utilities.TaskFragment;
import org.wycliffeassociates.translationrecorder.widgets.UnitCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class ActivityUnitList extends AppCompatActivity implements CheckingDialog.DialogListener,
        RatingDialog.DialogListener, TaskFragment.OnTaskComplete {

    public static String PROJECT_KEY = "project_key";
    public static String CHAPTER_KEY = "chapter_key";
    private final String TAG_TASK_FRAGMENT = "task_fragment";

    private final String STATE_RESYNC = "db_resync";
    private static final int DATABASE_RESYNC_TASK = Task.FIRST_TASK;

    public static Intent getActivityUnitListIntent(Context ctx, Project p, int chapter) {
        Intent intent = new Intent(ctx, ActivityUnitList.class);
        intent.putExtra(PROJECT_KEY, p);
        intent.putExtra(CHAPTER_KEY, chapter);
        return intent;
    }

    private int mChapterNum;
    private Project mProject;
    private List<UnitCard> mUnitCardList;
    private UnitCardAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mUnitList;
    private boolean mDbResyncing;
    private TaskFragment mTaskFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_list);

        mProject = getIntent().getParcelableExtra(PROJECT_KEY);
        mChapterNum = getIntent().getIntExtra(CHAPTER_KEY, 1);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
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
        String language = db.getLanguageName(mProject.getTargetLanguage());
        String book = db.getBookName(mProject.getSlug());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.unit_list_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(language + " - " + book + " - Chapter " + mChapterNum);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Find the recycler view
        mUnitList = (RecyclerView) findViewById(R.id.unit_list);
        mUnitList.setHasFixedSize(false);

        // Set its layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUnitList.setLayoutManager(mLayoutManager);

        // Set its adapter
        mUnitCardList = new ArrayList<>();
        mAdapter = new UnitCardAdapter(this, mProject, mChapterNum, mUnitCardList);
        mUnitList.setAdapter(mAdapter);

        // Set its animator
        mUnitList.setItemAnimator(new DefaultItemAnimator());
        prepareUnitCardData();

        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mDbResyncing) {
            mDbResyncing = true;
            ResyncProjectTask task = new ResyncProjectTask(DATABASE_RESYNC_TASK, getBaseContext(), getFragmentManager(), mProject);
            mTaskFragment.executeRunnable(task, "Resyncing Database", "Please wait...", true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putBoolean(STATE_RESYNC, mDbResyncing);
        super.onSaveInstanceState(saveInstanceState);
    }

    public void refreshUnitCards() {
        for (int i = 0; i < mUnitCardList.size(); i++) {
            mUnitCardList.get(i).refreshUnitStarted(mProject, mChapterNum, mUnitCardList.get(i).getStartVerse());
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.exitCleanUp();
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

    @Override
    public void onPositiveClick(CheckingDialog dialog) {
        // NOTE: Deprecated
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), dialog.getRating());
        db.close();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNegativeClick(CheckingDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onNegativeClick(RatingDialog dialog) {
        dialog.dismiss();
    }


    private void prepareUnitCardData() {
        try {
            Chunks chunks = new Chunks(this, mProject.getSlug());
            List<Map<String, String>> map = chunks.getChunks(mProject, mChapterNum);
            for (Map<String, String> unit : map) {
                mUnitCardList.add(new UnitCard(this, mProject, mChapterNum, Integer.parseInt(unit.get(Chunks.FIRST_VERSE)), Integer.parseInt(unit.get(Chunks.LAST_VERSE))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskComplete(int taskTag, int resultCode) {
        if (resultCode == TaskFragment.STATUS_OK) {
            if (taskTag == DATABASE_RESYNC_TASK) {
                mDbResyncing = false;
                refreshUnitCards();
            }
        }
    }
}
