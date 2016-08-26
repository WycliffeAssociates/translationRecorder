package wycliffeassociates.recordingapp.ProjectManager;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.WavFile;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.widgets.ChapterCard;

/**
 * Created by sarabiaj on 6/28/2016.
 */
public class ActivityChapterList extends AppCompatActivity implements
        CheckingDialog.DialogListener, CompileDialog.DialogListener {

    public static String PROJECT_KEY = "project_key";
    private Chunks mChunks;

    public static Intent getActivityVerseListIntent(Context ctx, Project p){
        Intent intent = new Intent(ctx, ActivityUnitList.class);
        intent.putExtra(PROJECT_KEY, p);
        return intent;
    }

    private Project mProject;
    private List<ChapterCard> mChapterCardList;
    private ChapterCardAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mChapterList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        ProjectDatabaseHelper mDb = new ProjectDatabaseHelper(this);

        // Setup toolbar
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
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int numChapters = mChunks.getNumChapters();
        int[] numStarted = db.getNumStartedUnitsInProject(mProject, numChapters);
        for(int i = 0; i < mChapterCardList.size(); i++){
            mChapterCardList.get(i).refreshChapterStarted(mProject, i+1);
            mChapterCardList.get(i).setCanCompile(numStarted[i] == mChunks.getNumChunks(mProject, i + 1));
            mChapterCardList.get(i).refreshChapterCompiled(mProject, i+1);
            if(mChapterCardList.get(i).isCompiled()) {
                mChapterCardList.get(i).setCheckingLevel(db.getChapterCheckingLevel(mProject, i+1));
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPositiveClick(CheckingDialog dialog) {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        int[] chapters = dialog.getChapters();
        for(int i =0; i < chapters.length; i++) {
            db.setCheckingLevel(dialog.getProject(), chapters[i], dialog.getCheckingLevel());
        }
        db.close();
        dialog.dismiss();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPositiveClick(CompileDialog dialog) {
        int[] chapters = dialog.getChapters();
        for(int i = 0; i < chapters.length; i++){
            mChapterCardList.get(chapters[i]-1).compile();
            mAdapter.notifyItemChanged(chapters[i]-1);
        }
        if (mAdapter.isInActionMode()) {
            mAdapter.getActionMode().finish();
        }
        dialog.dismiss();
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
                mChapterCardList.add(new ChapterCard(this, mProject, i+1));
            }

//        try {
//            Chunks chunks = new Chunks(this, mProject.getSlug());
//            ListView chapterList = (ListView)findViewById(R.id.chapter_list);
//            chapterList.setAdapter(new ChapterAdapter(this, mProject, chunks));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
