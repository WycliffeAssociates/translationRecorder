package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 2/25/2016.
 */
public class BookListFragment extends PreferenceFragment implements Searchable {
    private OnItemClickListener mListener;
    private TargetBookAdapter mAdapter;
    String mProject = "nt";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        ListView list = (ListView) rootView.findViewById(R.id.list_view);
        mAdapter = new TargetBookAdapter(getBooks());
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemClick(mAdapter.getItem(position));
            }
        });

        EditText searchView = (EditText) rootView.findViewById(R.id.search_text);
        searchView.setHint(R.string.choose_target_book);
        searchView.setEnabled(false);
        ImageButton searchBackButton = (ImageButton) rootView.findViewById(R.id.search_back_button);
        searchBackButton.setVisibility(View.GONE);
        ImageView searchIcon = (ImageView) rootView.findViewById(R.id.search_mag_icon);
        searchIcon.setVisibility(View.GONE);

        return rootView;
    }

    private Book[] getBooks(){
        ParseJSON parse = new ParseJSON(this.getActivity());
        ArrayList<Book> books= new ArrayList<>(Arrays.asList(parse.pullBooks()));
        for(int i = 0; i < books.size(); i++){
            if(mProject.compareTo("nt") == 0){
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Project project = activity.getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        mProject = project.getProject();
        try {
            this.mListener = (OnItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onSearchQuery(String query) {
        if(mAdapter != null) {
            mAdapter.getFilter().filter(query);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Book targetLanguage);
    }
}

