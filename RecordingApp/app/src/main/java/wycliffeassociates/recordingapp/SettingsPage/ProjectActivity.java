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


import wycliffeassociates.recordingapp.R;


/**
 * Created by sarabiaj on 2/23/2016.
 */
public class ProjectActivity extends AppCompatActivity implements ProjectListFragment.OnItemClickListener {

    ProjectListFragment mProjectListFragment;
    public final String TAG_LANGUAGE_LIST = "language_list_tag";
    public final String TAG_PROJECT_LIST = "project_list_tag";
    private Searchable mFragment;
    private String mSearchText = null;
    private String mSourceOrTarget;
    private final String target = "target";

    private final String source = "source";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        mSourceOrTarget = getIntent().getStringExtra("lang_type");
        Intent i = getIntent();
        mFragment = (Searchable)getFragmentManager().findFragmentByTag(TAG_PROJECT_LIST);
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
        if(mFragment instanceof ProjectListFragment) {
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
            System.out.println("should be setting the query");
            searchViewAction.setQuery(mSearchText, true);
        }
        return true;
    }

    @Override
    public void onItemClick(String projectId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String project = "nt";
        if(projectId.compareTo("Bible: OT") == 0){
            project = "ot";
        } else if(projectId.compareTo("Bible: NT") == 0){
            project = "nt";
        } else if(projectId.compareTo("Open Bible Stories") == 0){
            project = "obs";
        }
        pref.edit().putString(Settings.KEY_PREF_PROJECT, project).commit();
        if(project.compareTo("obs") != 0){
            Intent intent = new Intent(this, BookActivity.class);
            intent.putExtra("project", project);
            startActivity(intent);
        }
        finish();
    }
}
