package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import wycliffeassociates.recordingapp.FilesPage.ExportFiles;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.MainMenu;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.connectivity.LanguageNamesRequest;
import wycliffeassociates.recordingapp.project.Language;
import wycliffeassociates.recordingapp.project.ScrollableListFragment;

/**
 *
 * The settings page -- for all persistent options/information.
 *
 */
public class Settings extends Activity implements ScrollableListFragment.OnItemClickListener {
    public static final String KEY_PREF_PROJECT = "pref_project";
    public static final String KEY_PREF_SOURCE = "pref_source";
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

    private String mSearchText;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    public static boolean displayingList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayingList = false;
        mFragmentManager = getFragmentManager();
        setContentView(R.layout.settings);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    }

    public void onBackPressed(View v) {
        if(displayingList){
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(fm.findFragmentById(R.id.fragment_scroll_list)).commit();
            displayingList = false;
        } else {
            finish();
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
        //if(mFragment instanceof LanguageListFragment) {
        menu.findItem(R.id.action_update).setVisible(false);
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
                ((ScrollableListFragment)(mFragmentManager.findFragmentById(R.layout.fragment_scroll_list))).onSearchQuery(s);
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
        pref.edit().putString(KEY_PREF_LANG_SRC, ((Language)result).getCode()).commit();
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentById(R.id.fragment_scroll_list)).commit();
        displayingList = false;
    }

}