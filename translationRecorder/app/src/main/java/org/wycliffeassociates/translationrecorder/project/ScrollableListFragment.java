package org.wycliffeassociates.translationrecorder.project;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import org.wycliffeassociates.translationrecorder.R;


public class ScrollableListFragment extends Fragment implements Searchable {
    private OnItemClickListener mListener;
    private ArrayAdapter mAdapter;
    private String mSearchHint = "";

    public interface OnItemClickListener {
        void onItemClick(Object result);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scroll_list, container, false);
        ImageButton searchBackButton = (ImageButton) rootView.findViewById(R.id.search_back_button);
        searchBackButton.setVisibility(View.GONE);
        ImageView searchIcon = (ImageView) rootView.findViewById(R.id.search_mag_icon);
        searchIcon.setVisibility(View.GONE);

        ListView list = (ListView) rootView.findViewById(R.id.list);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemClick(mAdapter.getItem(position));
            }
        });

        //if only one item is in the adapter, just choose it and continue on in the project wizard
        if(mAdapter != null && mAdapter.getCount() == 1) {
            mListener.onItemClick(mAdapter.getItem(0));
        }
        EditText searchView = (EditText) rootView.findViewById(R.id.search_text);
        searchView.setHint(mSearchHint);
        searchView.setEnabled(false);

        return rootView;
    }

    public void setAdapter(ArrayAdapter adapter){
        mAdapter = adapter;
    }

    public void setSearchHint(String hint){
        mSearchHint = hint;
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

    public static class Builder{
        private ScrollableListFragment mFragment;

        public Builder(ArrayAdapter adapter){
            mFragment = new ScrollableListFragment();
            mFragment.setAdapter(adapter);
        }
        public Builder setSearchHint(String hint){
            mFragment.setSearchHint(hint);
            return this;
        }

        public ScrollableListFragment build(){
            return mFragment;
        }

    }
}