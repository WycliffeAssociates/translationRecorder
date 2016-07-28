package wycliffeassociates.recordingapp.ProjectManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.project.Chunks;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class ActivityVerseList extends AppCompatActivity {

    String mChapter;
    Project mProject;
    ConstantsDatabaseHelper mDb;
    ListView mVerseList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verse_list);

        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        mChapter = getIntent().getStringExtra(Settings.KEY_PREF_CHAPTER);
        mDb = new ConstantsDatabaseHelper(this);
        mVerseList = (ListView) findViewById(R.id.chapter_list);

        mVerseList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mVerseList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position,
                                                  long id, boolean checked) {
                System.out.println("ITEM CHECKED STATE CHANGED " + position + " " + id + " " + checked);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                System.out.println("CREATE ACTION MODE " + mode + " " + menu);
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.unit_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                System.out.println("PREPARE ACTION MODE " + mode + " " + menu);
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                System.out.println("ACTION ITEM CLICKED " + mode + " " + menuItem);
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                System.out.println("DESTROY ACTION MODE " + mode);
            }
        });

        String languageCode = mProject.getTargetLanguage();
        String language = mDb.getLanguageName(languageCode);
        String bookCode = mProject.getSlug();
        String book = mDb.getBookName(bookCode);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.chapter_list_toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(language + " - " + book + " - Chapter " + mChapter);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        this.setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Chunks chunks = new Chunks(this, mProject.getSlug());
            mVerseList.setAdapter(new VerseAdapter(this, mProject, chunks, Integer.parseInt(mChapter)));
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

    private void setListeners() {
        mVerseList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(mVerseList.getCheckedItemPositions());
                System.out.println("LONG CLICK");
//                mVerseList.clearChoices();
                mVerseList.setItemChecked(i, true);
//                if (activeMode == null) {
//                    activeMode = startActionMode(this);
//                }
                System.out.println(mVerseList.getCheckedItemPositions());
                return true;
            }
        });
    }

}
