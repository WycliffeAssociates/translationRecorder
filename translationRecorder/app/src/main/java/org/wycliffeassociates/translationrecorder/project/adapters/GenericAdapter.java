package org.wycliffeassociates.translationrecorder.project.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.project.components.ProjectComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by joel on 9/4/2015.
 */
public class GenericAdapter extends ArrayAdapter {
    private ProjectComponent[] mProjectComponents;
    private ProjectComponent[] mFilteredProjectComponents;
    private ProjectComponentFilter mProjectComponentFilter;

    public GenericAdapter(ProjectComponent[] component, Context ctx) {
        super(ctx, R.layout.fragment_scroll_list_item);
        List<ProjectComponent> targetProjectComponentsList = Arrays.asList(component);
        Collections.sort(targetProjectComponentsList);
        mProjectComponents = targetProjectComponentsList.toArray(new ProjectComponent[targetProjectComponentsList.size()]);
        mFilteredProjectComponents = mProjectComponents;
    }


    @Override
    public int getCount() {
        if(mFilteredProjectComponents != null) {
            return mFilteredProjectComponents.length;
        } else {
            return 0;
        }
    }

    @Override
    public ProjectComponent getItem(int position) {
        return mFilteredProjectComponents[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if(convertView == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_scroll_list_item, null);
            holder = new ViewHolder(v);
        } else {
            holder = (ViewHolder)v.getTag();
        }

        // render view
        holder.mProjectComponentView.setText(getItem(position).getLabel());
        holder.mCodeView.setText(getItem(position).getSlug());


        LinearLayout ll = (LinearLayout)v.findViewById(R.id.scroll_list_item_layout);
        if(!mFilteredProjectComponents[position].displayItemIcon()) {
            ll.removeView(ll.findViewById(R.id.itemIcon));
        }
        LinearLayout rmll = (LinearLayout)ll.findViewById(R.id.rightmost_scroll_list_item_layout);
        if(!mFilteredProjectComponents[position].displayMoreIcon()) {
            rmll.removeView((rmll.findViewById(R.id.moreIcon)));
        }


        return v;
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }

    /**
     * Returns the target language filter
     * @return
     */
    public Filter getFilter() {
        if(mProjectComponentFilter == null) {
            mProjectComponentFilter = new ProjectComponentFilter();
        }
        return mProjectComponentFilter;
    }

    public static class ViewHolder {
        public TextView mProjectComponentView;
        public TextView mCodeView;

        public ViewHolder(View view) {
            mProjectComponentView = (TextView) view.findViewById(R.id.majorText);
            mCodeView = (TextView) view.findViewById(R.id.minorText);
            view.setTag(this);
        }
    }

    private class ProjectComponentFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                // no filter
                results.values = Arrays.asList(mProjectComponents);
                results.count = mProjectComponents.length;
            } else {
                // perform filter
                List<ProjectComponent> filteredCategories = new ArrayList<>();
                for(ProjectComponent language:mProjectComponents) {
                    // match the target language id
                    boolean match = language.getSlug().toLowerCase().startsWith(charSequence.toString().toLowerCase());
                    if(!match) {
                        if (language.getName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                            // match the target language name
                            match = true;
                        }
                    }
                    if(match) {
                        filteredCategories.add(language);
                    }
                }
                results.values = filteredCategories;
                results.count = filteredCategories.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            List<ProjectComponent> filteredProjectComponents = (List<ProjectComponent>)filterResults.values;
            if(charSequence != null && charSequence.length() > 0) {
                sortProjectComponents(filteredProjectComponents, charSequence);
            }
            mFilteredProjectComponents = filteredProjectComponents.toArray(new ProjectComponent[filteredProjectComponents.size()]);
            notifyDataSetChanged();
        }
    }

    /**
     * Sorts target languages by id
     * @param languages
     * @param referenceId languages are sorted according to the reference id
     */
    private static void sortProjectComponents(List<ProjectComponent> languages, final CharSequence referenceId) {
        Collections.sort(languages, new Comparator<ProjectComponent>() {
            @Override
            public int compare(ProjectComponent lhs, ProjectComponent rhs) {
                String lhId = lhs.getSlug();
                String rhId = rhs.getSlug();
                // give priority to matches with the reference
                if(lhId.startsWith(referenceId.toString().toLowerCase())) {
                    lhId = "!" + lhId;
                }
                if(rhId.startsWith(referenceId.toString().toLowerCase())) {
                    rhId = "!" + rhId;
                }
                return lhId.compareToIgnoreCase(rhId);
            }
        });
    }
}