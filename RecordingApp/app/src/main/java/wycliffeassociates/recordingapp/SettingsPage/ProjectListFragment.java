package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;


import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.Searchable;

/**
 * Created by joel on 9/4/2015.
 */
public class ProjectListFragment extends PreferenceFragment implements Searchable {
    private OnItemClickListener mListener;
    private ProjectCategoryAdapter mAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_project_list, container, false);

        EditText searchView = (EditText) rootView.findViewById(R.id.search_text);
        searchView.setHint(R.string.choose_a_project);
        searchView.setEnabled(false);
        ImageButton searchBackButton = (ImageButton) rootView.findViewById(R.id.search_back_button);
        searchBackButton.setVisibility(View.GONE);

        final ImageView updateIcon = (ImageView) rootView.findViewById(R.id.search_mag_icon);
        updateIcon.setBackgroundResource(R.drawable.ic_refresh_black_24dp);
        // TODO: set up update button

        ListView list = (ListView) rootView.findViewById(R.id.list);

        String[] projectCategories = {"Bible: OT", "Bible: NT", "Open Bible Stories"};

        mAdapter = new ProjectCategoryAdapter(projectCategories, getActivity());
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String category = mAdapter.getItem(position);
                mListener.onItemClick(category);
            }
        });

        return rootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        void onItemClick(String projectId);
    }
}
