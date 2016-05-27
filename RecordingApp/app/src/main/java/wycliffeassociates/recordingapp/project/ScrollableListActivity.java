package wycliffeassociates.recordingapp.project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Searchable;

/**
 * Created by sarabiaj on 5/27/2016.
 */
public abstract class ScrollableListActivity extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener {
    private Searchable mFragment;
    private String mSearchText = null;
    protected final String SEARCH_TEXT_KEY = "search_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollable_list);
        if(savedInstanceState != null){
            mSearchText = savedInstanceState.getString(SEARCH_TEXT_KEY, null);
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
        saveInstanceState.putString(SEARCH_TEXT_KEY, mSearchText);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);

//        if(mFragment instanceof LanguageListFragment) {
//            menu.findItem(R.id.action_update).setVisible(false);
//        } else {
//            menu.findItem(R.id.action_update).setVisible(false);
//        }

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
    public abstract void onItemClick(Object result);

    protected void initializeScrollableListFragment(Searchable fragment){
        this.mFragment = fragment;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, (Fragment) fragment).commit();
    }

    protected void buildFragmentFromAdapter(ArrayAdapter adapter){
        ScrollableListFragment fragment = new ScrollableListFragment.Builder(adapter).build();
        initializeScrollableListFragment(fragment);
    }

    protected void buildFragmentFromAdapter(ArrayAdapter adapter, String searchHint){
        ScrollableListFragment fragment = new ScrollableListFragment.Builder(adapter).setSearchHint(searchHint).build();
        initializeScrollableListFragment(fragment);
    }
}
