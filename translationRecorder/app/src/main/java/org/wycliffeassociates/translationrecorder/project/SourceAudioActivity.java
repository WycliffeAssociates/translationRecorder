package org.wycliffeassociates.translationrecorder.project;

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
import android.view.View;
import android.widget.Button;

import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.project.adapters.TargetLanguageAdapter;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.project.components.Language;

/**
 * Created by sarabiaj on 5/25/2016.
 */
public class SourceAudioActivity extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener {
    private static final String mSetLanguageKey = "set_language_key";
    private static final String mSetLocationKey = "set_location_key";
    private static final String mProjectKey = "project_key";
    private static final String mUserSearchingLanguageKey = "searching_language_key";
    private static final String mSearchTextKey = "search_text_key";
    private Project mProject;
    private Button btnSourceLanguage;
    private Button btnSourceLocation;
    private Button btnContinue;
    private boolean mSetLocation = false;
    private boolean mSetLanguage = false;
    private final int REQUEST_SOURCE_LOCATION = 42;
    private final int REQUEST_SOURCE_LANGUAGE = 43;
    private int mCurrentFragment;
    private Searchable mFragment;
    private FragmentManager mFragmentManager;
    protected String mSearchText;
    private boolean mShowSearch = false;

    public static Intent getSourceAudioIntent(Context ctx, Project project){
        Intent intent = new Intent(ctx, SourceAudioActivity.class);
        intent.putExtra(Project.PROJECT_EXTRA, project);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_audio);
        Intent i = getIntent();
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        mFragmentManager = getFragmentManager();

        btnSourceLanguage = (Button) findViewById(R.id.language_btn);
        btnSourceLanguage.setOnClickListener(btnClick);
        btnSourceLocation = (Button) findViewById(R.id.location_btn);
        btnSourceLocation.setOnClickListener(btnClick);
        btnContinue = (Button) findViewById(R.id.continue_btn);
        btnContinue.setOnClickListener(btnClick);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
//        // Remember that you should never show the action bar if the
//        // status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Source Audio");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(mProjectKey, mProject);
        outState.putBoolean(mSetLanguageKey, mSetLanguage);
        outState.putBoolean(mSetLocationKey, mSetLocation);
        outState.putString(mSearchTextKey, mSearchText);
        outState.putBoolean(mUserSearchingLanguageKey, (mFragment != null)? true : false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mProject = savedInstanceState.getParcelable(mProjectKey);
        mSetLanguage = savedInstanceState.getBoolean(mSetLanguageKey);
        mSetLocation = savedInstanceState.getBoolean(mSetLocationKey);
        if (mSetLocation) {
            btnSourceLocation.setText("Source Location: " + mProject.getSourceAudioPath());
        }
        if (mSetLanguage) {
            btnSourceLanguage.setText("Source Language: " + mProject.getSourceLanguageSlug());
        }
        if(savedInstanceState.getBoolean(mUserSearchingLanguageKey)){
            mSearchText = savedInstanceState.getString(mSearchTextKey);
            setSourceLanguage();
        } else {
            continueIfBothSet();
        }
    }

    @Override
    public void onBackPressed() {
        //if the source language fragment is showing, then close that, otherwise proceed with back press
        if (findViewById(R.id.fragment_container).getVisibility() == View.VISIBLE) {
            findViewById(R.id.fragment_container).setVisibility(View.INVISIBLE);
            hideSearchMenu();
        } else {
            super.onBackPressed();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //if(mFragment instanceof LanguageListFragment) {
        menu.findItem(R.id.action_update).setVisible(false);
//        } else {
//            menu.findItem(R.id.action_update).setVisible(false);
//        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setVisible(mShowSearch);
        final SearchView searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchText = s;
                if(mFragment != null) {
                    mFragment.onSearchQuery(s);
                }
                return true;
            }
        });
        searchViewAction.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        if (mSearchText != null) {
            searchViewAction.setQuery(mSearchText, true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (findViewById(R.id.fragment_container).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.fragment_container).setVisibility(View.INVISIBLE);
                } else {
                    finish();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSourceLanguage() {
        if (mFragment != null) {
            mFragmentManager.beginTransaction().remove((Fragment) mFragment).commit();
        }
        mFragment = new ScrollableListFragment.Builder(new TargetLanguageAdapter(ParseJSON.getLanguages(this), this)).setSearchHint("Choose Source Language:").build();
        mFragmentManager.beginTransaction().add(R.id.fragment_container, (Fragment) mFragment).commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
    }

    public void setSourceLocation() {
        startActivityForResult(new Intent(this, SelectSourceDirectory.class), REQUEST_SOURCE_LOCATION);
    }

    public void proceed() {
        Intent intent = new Intent();
        intent.putExtra(Project.PROJECT_EXTRA, mProject);
        setResult(RESULT_OK, intent);
        finish();
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.location_btn: {
                    setSourceLocation();
                    break;
                }
                case R.id.language_btn: {
                    displaySearchMenu();
                    setSourceLanguage();
                    break;
                }
                case R.id.continue_btn: {
                    proceed();
                    break;
                }
            }
        }
    };

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

    public void continueIfBothSet() {
        if (mSetLocation && mSetLanguage) {
            btnContinue.setText("Continue");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SOURCE_LOCATION) {
            if (data.hasExtra(SelectSourceDirectory.SOURCE_LOCATION)) {
                mProject.setSourceAudioPath(data.getStringExtra(SelectSourceDirectory.SOURCE_LOCATION));
                btnSourceLocation.setText("Source Location: " + mProject.getSourceAudioPath());
                mSetLocation = true;
                continueIfBothSet();
            }
        }
    }

    @Override
    public void onItemClick(Object result) {
        Utils.closeKeyboard(this);
        hideSearchMenu();
        mProject.setSourceLanguage(((Language) result).getSlug());
        btnSourceLanguage.setText("Source Language: " + mProject.getSourceLanguageSlug());
        mSetLanguage = true;
        mFragmentManager.beginTransaction().remove((Fragment) mFragment).commit();
        findViewById(R.id.fragment_container).setVisibility(View.INVISIBLE);
        continueIfBothSet();
    }
}
