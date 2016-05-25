package wycliffeassociates.recordingapp.SettingsPage;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;


import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;


/**
 * Created by sarabiaj on 2/23/2016.
 */
public class LanguageActivity extends AppCompatActivity implements LanguageListFragment.OnItemClickListener {

    LanguageListFragment mLanguageListFragment;
    public final String TAG_LANGUAGE_LIST = "language_list_tag";
    private Searchable mFragment;
    private String mSearchText = null;
    private String mSourceOrTarget;
    private final String target = "target";
    private final String source = "source";
    private Project mProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selector);
        if(getIntent().hasExtra("lang_type")) {
            mSourceOrTarget = getIntent().getStringExtra("lang_type");
        } else {
            mSourceOrTarget = "target";
        }
        Intent i = getIntent();
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        mFragment = (Searchable)getFragmentManager().findFragmentByTag(TAG_LANGUAGE_LIST);
        if(savedInstanceState != null){
            mSearchText = savedInstanceState.getString("search_text", null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        saveInstanceState.putString("search_text", mSearchText);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(mFragment instanceof LanguageListFragment) {
            menu.findItem(R.id.action_update).setVisible(false);
        } else {
            menu.findItem(R.id.action_update).setVisible(false);
        }
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchText = s;
                mFragment.onSearchQuery(s);
                return true;
            }
        });
        searchViewAction.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        if(mSearchText != null){
            System.out.println("should be setting the queury");
            searchViewAction.setQuery(mSearchText, true);
        }
        return true;
    }

    @Override
    public void onItemClick(Language targetLanguage) {
        if(mSourceOrTarget.compareTo("source") == 0){
            mProject.setSourceLanguage(targetLanguage.getCode());
        } else {
            mProject.setTargetLanguage(targetLanguage.getCode());
        }
        Intent intent = new Intent();
        intent.putExtra(Project.PROJECT_EXTRA, mProject);
        setResult(RESULT_OK, intent);
        this.finish();
    }
}
