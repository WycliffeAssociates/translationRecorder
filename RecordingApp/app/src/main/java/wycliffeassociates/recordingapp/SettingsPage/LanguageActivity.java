package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;


import org.json.JSONException;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.project.ScrollableListFragment;


/**
 * Created by sarabiaj on 2/23/2016.
 */
public class LanguageActivity extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener {

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
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        TargetLanguageAdapter tla = new TargetLanguageAdapter(getLanguages(), this);
        mFragment = new ScrollableListFragment.Builder(tla).setSearchHint("IT'S HAPPENING!!!").build();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, (Fragment) mFragment).commit();

        if(savedInstanceState != null){
            mSearchText = savedInstanceState.getString("search_text", null);
        }
    }

    private Language[] getLanguages(){
        ParseJSON parse = new ParseJSON(this);
        Language[] languages= null;
        try {
            languages = parse.pullLangNames();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return languages;
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
            searchViewAction.setQuery(mSearchText, true);
        }
        return true;
    }

    @Override
    public void onItemClick(Object targetLanguage) {
//        if(mSourceOrTarget.compareTo("source") == 0){
//            mProject.setSourceLanguage(((Language)targetLanguage).getCode());
//        } else {
//            mProject.setTargetLanguage(((Language)targetLanguage).getCode());
//        }
//        Intent intent = new Intent();
//        intent.putExtra(Project.PROJECT_EXTRA, mProject);
//        setResult(RESULT_OK, intent);
//        this.finish();
    }
}
