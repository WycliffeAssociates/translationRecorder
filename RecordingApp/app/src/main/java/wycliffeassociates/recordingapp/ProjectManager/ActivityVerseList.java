package wycliffeassociates.recordingapp.ProjectManager;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bignerdranch.android.multiselector.MultiSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.InternsPreferencesManager;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.widgets.UnitCard;
import wycliffeassociates.recordingapp.widgets.VerseCard;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class ActivityVerseList extends AppCompatActivity {

    public static String PROJECT_KEY = "project_key";
    public static String CHAPTER_KEY = "chapter_key";

    public static Intent getActivityVerseListIntent(Context ctx, Project p, int chapter){
        Intent intent = new Intent(ctx, ActivityVerseList.class);
        intent.putExtra(PROJECT_KEY, p);
        intent.putExtra(CHAPTER_KEY, chapter);
        return intent;
    }

    private int mChapterNum;
    private Project mProject;
    private ConstantsDatabaseHelper mDb;
    private List<UnitCard> mUnitCardList;
    private UnitCardAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mUnitList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verse_list);

        mProject = getIntent().getParcelableExtra(PROJECT_KEY);
        mChapterNum = getIntent().getIntExtra(CHAPTER_KEY, 1);
        mDb = new ConstantsDatabaseHelper(this);

        // Setup toolbar
        String language = mDb.getLanguageName(mProject.getTargetLanguage());
        String book = mDb.getBookName(mProject.getSlug());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chapter_list_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(language + " - " + book + " - Chapter " + mChapterNum);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Find the recycler view
        mUnitList = (RecyclerView) findViewById(R.id.chapter_list);
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

        // Fill the data list for the recycler view
        prepareUnitCardData();
    }

    private void prepareUnitCardData() {
//        for (int i = 0; i < 100; i++) {
//            mUnitCardList.add(new UnitCard("Wat", Integer.toString(i)));
//        }

        try {
            Chunks chunks = new Chunks(this, mProject.getSlug());
            List<Map<String, String>> map = chunks.getChunks(mProject, mChapterNum);
            String mode = Utils.capitalizeFirstLetter(mProject.getMode());
            for (Map<String, String> unit : map) {
                mUnitCardList.add(new UnitCard(mode, unit.get(Chunks.FIRST_VERSE)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
