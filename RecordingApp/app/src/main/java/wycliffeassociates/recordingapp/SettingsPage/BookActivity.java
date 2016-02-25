package wycliffeassociates.recordingapp.SettingsPage;

import android.app.SearchManager;
import android.content.Context;
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
 * Created by sarabiaj on 2/25/2016.
 */
public class BookActivity extends AppCompatActivity implements BookListFragment.OnItemClickListener {

    public final String TAG_BOOK_LIST = "book_list_tag";
    private Searchable mFragment;
    private String mSearchText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_selector);

        mFragment = (Searchable)getFragmentManager().findFragmentByTag(TAG_BOOK_LIST);
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
        if(mFragment instanceof BookListFragment) {
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
    public void onItemClick(Book targetBook) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString(Settings.KEY_PREF_BOOK, targetBook.getSlug()).commit();
        Settings.updateFilename(this);
        this.finish();
    }
}


