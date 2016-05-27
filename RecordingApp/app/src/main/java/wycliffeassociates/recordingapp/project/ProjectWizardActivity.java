package wycliffeassociates.recordingapp.project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Book;
import wycliffeassociates.recordingapp.SettingsPage.Language;
import wycliffeassociates.recordingapp.SettingsPage.LanguageListFragment;
import wycliffeassociates.recordingapp.SettingsPage.ModeCategoryAdapter;
import wycliffeassociates.recordingapp.SettingsPage.ParseJSON;
import wycliffeassociates.recordingapp.SettingsPage.ProjectCategoryAdapter;
import wycliffeassociates.recordingapp.SettingsPage.TargetBookAdapter;
import wycliffeassociates.recordingapp.SettingsPage.TargetLanguageAdapter;


/**
 * Created by sarabiaj on 5/27/2016.
 */
public class ProjectWizardActivity extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener{

    protected static final String mProjectKey = "project_key";
    protected Project mProject;
    protected ScrollableListFragment mFragment;
    protected String mSearchText;
    protected FragmentManager mFragmentManager;

    interface ProjectContract{
        String PROJECT_KEY = mProjectKey;
    }

    public static final int BASE_PROJECT = 1;
    public static final int TARGET_LANGUAGE = BASE_PROJECT;
    public static final int PROJECT = BASE_PROJECT+1;
    public static final int BOOK = BASE_PROJECT+2;
    public static final int SOURCE_TEXT = BASE_PROJECT+3;
    public static final int MODE = BASE_PROJECT+4;
    public static final int SOURCE_LANGUAGE = BASE_PROJECT+5;
    private int mCurrentFragment = BASE_PROJECT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();

        mFragment = new ScrollableListFragment.Builder(new TargetLanguageAdapter(getLanguages(), this)).setSearchHint("Choose Target Language:").build();
        mFragmentManager.beginTransaction().add(R.id.fragment_container,mFragment).commit();
        mProject = new Project();

        setContentView(R.layout.activity_scrollable_list);
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

    private Book[] getBooks(){
        ParseJSON parse = new ParseJSON(this);
        ArrayList<Book> books= new ArrayList<>(Arrays.asList(parse.pullBooks()));
        for(int i = 0; i < books.size(); i++){
            if(mProject.getProject().compareTo("nt") == 0){
                if(books.get(i).getOrder() < 40){
                    books.remove(i);
                    i--;
                }
            } else {
                if(books.get(i).getOrder() > 39){
                    books.remove(i);
                    i--;
                }
            }
        }
        Book[] bookArray = new Book[books.size()];
        return books.toArray(bookArray);
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
    public void onItemClick(Object result) {
        if(result instanceof Language && mCurrentFragment == TARGET_LANGUAGE){
            ((Project)mProject).setSourceLanguage(((Language)result).getCode());
            mFragmentManager.beginTransaction().remove(mFragment).commit();
            mFragment = new ScrollableListFragment.Builder(new ProjectCategoryAdapter(new String[]{"Bible: OT", "Bible: NT", "Open Bible Stories"}, this)).setSearchHint("Choose a project").build();
            mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment).commit();
            mCurrentFragment++;
        } else if (result instanceof String && mCurrentFragment == PROJECT){
            String project = "";
            if(((String)result).compareTo("Bible: OT") == 0){
                project = "ot";
            } else if (((String)result).compareTo("Bible: NT") == 0){
                project = "nt";
            } else {
                project = "obs";
            }
            ((Project)mProject).setProject(project);
            mFragmentManager.beginTransaction().remove(mFragment).commit();
//            if(project.compareTo("obs") == 0){
//                mCurrentFragment = SOURCE_LANGUAGE;
//            }
            mFragment = new ScrollableListFragment.Builder(new TargetBookAdapter(getBooks(), this)).setSearchHint("Choose a book").build();
            mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment).commit();
            mCurrentFragment++;
        } else if (result instanceof Book  && mCurrentFragment == BOOK){
            Book book = (Book)result;
            mProject.setBookNumber(book.getOrder());
            mProject.setSlug(book.getSlug());
            mFragmentManager.beginTransaction().remove(mFragment).commit();
            mFragment = new ScrollableListFragment.Builder(new ProjectCategoryAdapter(new String[]{"ulb", "udb", "reg"}, this)).setSearchHint("Choose a source text").build();
            mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment).commit();
            mCurrentFragment++;
        } else if (result instanceof String && mCurrentFragment == SOURCE_TEXT){
            ((Project)mProject).setProject((String)result);
            mFragmentManager.beginTransaction().remove(mFragment).commit();
            finish();
        }
    }

}
