package wycliffeassociates.recordingapp.ProjectManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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


    Project mProject;
    ConstantsDatabaseHelper mDb;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        String chapter = getIntent().getStringExtra(Settings.KEY_PREF_CHAPTER);
        mDb = new ConstantsDatabaseHelper(this);

        String languageCode = mProject.getTargetLanguage();
        String language = mDb.getLanguageName(languageCode);
        String bookCode = mProject.getSlug();
        String book = mDb.getBookName(bookCode);

        setContentView(R.layout.activity_verse_list);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chapter_list_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(language + " - " + book + " - Chapter " + chapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            Chunks chunks = new Chunks(this, mProject.getSlug());
            ListView chapterList = (ListView)findViewById(R.id.chapter_list);
            chapterList.setAdapter(new VerseAdapter(this, mProject, chunks, Integer.parseInt(chapter)));
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
