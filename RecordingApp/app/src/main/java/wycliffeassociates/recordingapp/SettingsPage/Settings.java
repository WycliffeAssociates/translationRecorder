package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Fragment;
import android.app.FragmentManager;
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
import android.view.View;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.project.Language;
import wycliffeassociates.recordingapp.project.ParseJSON;
import wycliffeassociates.recordingapp.project.ScrollableListFragment;
import wycliffeassociates.recordingapp.project.adapters.TargetLanguageAdapter;

/**
 *
 * The settings page -- for all persistent options/information.
 *
 */
public class Settings extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener, SettingsFragment.LanguageSelector {
    public static final String KEY_PREF_ANTHOLOGY = "pref_anthology";
    public static final String KEY_PREF_VERSION = "pref_version";
    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_LANG_SRC = "pref_lang_src";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_BOOK_NUM = "pref_book_num";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    public static final String KEY_PREF_TAKE = "pref_take";
    public static final String KEY_PREF_CHUNK_VERSE = "pref_chunk_verse";
    public static final String KEY_PREF_START_VERSE = "pref_start_verse";
    public static final String KEY_PREF_END_VERSE = "pref_end_verse";
    public static final String KEY_PREF_SRC_LOC = "pref_src_loc";
    public static final String KEY_SDK_LEVEL = "pref_sdk_level";
    public static final String KEY_PROFILE = "pref_profile";

    public static final String KEY_PREF_GLOBAL_SOURCE_LOC = "pref_global_src_loc";
    public static final String KEY_PREF_GLOBAL_LANG_SRC = "pref_global_lang_src";


    private String mSearchText;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    public static boolean displayingList = false;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayingList = false;
        mFragmentManager = getFragmentManager();
        setContentView(R.layout.settings);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void onBackPressed(View v) {
        backPressed();
    }

    private void backPressed(){
        if(displayingList){
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(fm.findFragmentById(R.id.fragment_scroll_list)).commit();
            displayingList = false;
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        mMenu = menu;
        super.onPrepareOptionsMenu(menu);
        //if(mFragment instanceof LanguageListFragment) {
        menu.findItem(R.id.action_update).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

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
                ((ScrollableListFragment)(mFragmentManager.findFragmentById(R.id.fragment_scroll_list))).onSearchQuery(s);
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
    public void onItemClick(Object result) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString(KEY_PREF_GLOBAL_LANG_SRC, ((Language)result).getCode()).commit();
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentById(R.id.fragment_scroll_list)).commit();
        displayingList = false;
        mMenu.findItem(R.id.action_search).setVisible(false);
    }

    @Override
    public void sourceLanguageSelected() {
        mMenu.findItem(R.id.action_search).setVisible(true);
        Settings.displayingList = true;
        mFragment = new ScrollableListFragment
                .Builder(new TargetLanguageAdapter(ParseJSON.getLanguages(this), this))
                .setSearchHint("Choose Source Language:")
                .build();
        mFragmentManager.beginTransaction().add(R.id.fragment_scroll_list, mFragment).commit();
    }
}