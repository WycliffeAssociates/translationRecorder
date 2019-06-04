package org.wycliffeassociates.translationrecorder.SettingsPage;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.ScrollableListFragment;
import org.wycliffeassociates.translationrecorder.project.adapters.TargetLanguageAdapter;
import org.wycliffeassociates.translationrecorder.utilities.TaskFragment;

/**
 *
 * The settings page -- for all persistent options/information.
 *
 */
public class Settings extends AppCompatActivity implements TaskFragment.OnTaskComplete, ScrollableListFragment.OnItemClickListener, SettingsFragment.LanguageSelector {

    public static final String KEY_RECENT_PROJECT_ID = "pref_recent_project_id";

    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    public static final String KEY_PREF_TAKE = "pref_take";
    public static final String KEY_PREF_CHUNK_VERSE = "pref_chunk_verse";
    public static final String KEY_PREF_START_VERSE = "pref_start_verse";
    public static final String KEY_PREF_END_VERSE = "pref_end_verse";
    public static final String KEY_PREF_SRC_LOC = "pref_src_loc";
    public static final String KEY_SDK_LEVEL = "pref_sdk_level";
    public static final String KEY_PROFILE = "pref_profile";
    public static final String KEY_USER = "pref_profile";

    public static final String KEY_PREF_GLOBAL_SOURCE_LOC = "pref_global_src_loc";
    public static final String KEY_PREF_GLOBAL_LANG_SRC = "pref_global_lang_src";
    public static final String KEY_PREF_ADD_LANGUAGE = "pref_add_temp_language";
    public static final String KEY_PREF_UPDATE_LANGUAGES = "pref_update_languages";


    private String mSearchText;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    public static boolean displayingList = false;
    private boolean mShowSearch = false;

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

    @Override
    public void onBackPressed() {
        if(displayingList){
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(fm.findFragmentById(R.id.fragment_scroll_list)).commit();
            displayingList = false;
            hideSearchMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Utils.closeKeyboard(this);
                onBackPressed();
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
        super.onPrepareOptionsMenu(menu);
        if(mShowSearch) {
            //if(mFragment instanceof LanguageListFragment) {
            menu.findItem(R.id.action_update).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(true);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
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
                    Fragment fragment = (mFragmentManager.findFragmentById(R.id.fragment_scroll_list));
                    //Seems to sometimes pull SettingsFragment instead and thus cannot cast?
                    if(fragment instanceof ScrollableListFragment) {
                        ((ScrollableListFragment) fragment).onSearchQuery(s);
                    }
                    return true;
                }
            });
            searchViewAction.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            if (mSearchText != null) {
                searchViewAction.setQuery(mSearchText, true);
            }
        } else {
            menu.findItem(R.id.action_update).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        }
        return true;
    }

    @Override
    public void onItemClick(Object result) {
        Utils.closeKeyboard(this);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString(KEY_PREF_GLOBAL_LANG_SRC, ((Language)result).getSlug()).commit();
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentById(R.id.fragment_scroll_list)).commit();
        displayingList = false;
        hideSearchMenu();
    }

    private void displaySearchMenu(){
        mShowSearch = true;
        invalidateOptionsMenu();
        mSearchText = "";
    }

    private void hideSearchMenu(){
        mShowSearch = false;
        invalidateOptionsMenu();
        mSearchText = "";
    }

    @Override
    public void sourceLanguageSelected() {
        displaySearchMenu();
        Settings.displayingList = true;
        mFragment = new ScrollableListFragment
                .Builder(new TargetLanguageAdapter(Language.getLanguages(this), this))
                .setSearchHint("Choose Source Language:")
                .build();
        mFragmentManager.beginTransaction().add(R.id.fragment_scroll_list, mFragment).commit();
    }

    @Override
    public void onTaskComplete(int taskTag, int resultCode) {

    }
}