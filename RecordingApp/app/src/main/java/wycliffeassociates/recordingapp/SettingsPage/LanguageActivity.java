package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import wycliffeassociates.recordingapp.R;


/**
 * Created by sarabiaj on 2/23/2016.
 */
public class LanguageActivity extends AppCompatActivity implements LanguageListFragment.OnItemClickListener {

    LanguageListFragment mLanguageListFragment;
    public final String TAG_LANGUAGE_LIST = "language_list_tag";
    private Searchable mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selector);

            if(savedInstanceState != null) {
                mFragment = (Searchable)getFragmentManager().findFragmentByTag(TAG_LANGUAGE_LIST);
            } else {
                mFragment = new LanguageListFragment();
                ((LanguageListFragment) mFragment).setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add((LanguageListFragment) mFragment, TAG_LANGUAGE_LIST).commit();
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
        if(mFragment instanceof LanguageListFragment) {
            menu.findItem(R.id.action_update).setVisible(true);
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
                mFragment.onSearchQuery(s);
                return true;
            }
        });
        searchViewAction.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public void onItemClick(Language targetLanguage) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("Language set to " + pref.getString(Settings.KEY_PREF_LANG, targetLanguage.getCode()));
        System.out.println("Setting language to " + targetLanguage.getCode());
        pref.edit().putString(Settings.KEY_PREF_LANG, targetLanguage.getCode()).commit();
        System.out.println("Language now " + pref.getString(Settings.KEY_PREF_LANG, targetLanguage.getCode()));
        Settings.updateFilename(this);
        this.finish();
    }
}
