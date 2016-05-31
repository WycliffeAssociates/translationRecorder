package wycliffeassociates.recordingapp.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wycliffeassociates.recordingapp.R;

/**
 * Created by joel on 9/4/2015.
 */
public class ModeCategoryAdapter extends ArrayAdapter {
    private String[] mCategories;
    private String[] mFilteredCategories;
    private ProjectCategoryFilter mProjectFilter;

    public ModeCategoryAdapter(String[] categories, Context context) {
        super(context, R.layout.fragment_scroll_list_item);
        List<String> categoriesList = Arrays.asList(categories);
        mCategories = categoriesList.toArray(new String[categoriesList.size()]);
        mFilteredCategories = mCategories;
    }

    @Override
    public int getCount() {
        if(mFilteredCategories != null) {
            return mFilteredCategories.length;
        } else {
            return 0;
        }
    }

    @Override
    public String getItem(int position) {
        return mFilteredCategories[position];
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
        holder.mProjectView.setText(getItem(position));

        LinearLayout ll = (LinearLayout)v.findViewById(R.id.scroll_list_item_layout);
        LinearLayout rmll = (LinearLayout)ll.findViewById(R.id.rightmost_scroll_list_item_layout);
        rmll.removeView((rmll.findViewById(R.id.minorText)));

        return v;
    }

    /**
     * Updates the data set
     * @param categories
     */
    public void changeData(String[] categories) {
        mCategories = categories;
        mFilteredCategories = categories;
        notifyDataSetChanged();
    }

    /**
     * Returns the project filter
     * @return
     */
    public Filter getFilter() {
        if(mProjectFilter == null) {
            mProjectFilter = new ProjectCategoryFilter();
        }
        return mProjectFilter;
    }

    public static class ViewHolder {
        public ImageView mIconImage;
        public TextView mProjectView;
        public ImageView mMoreImage;

        public ViewHolder(View view) {
            mIconImage = (ImageView) view.findViewById(R.id.itemIcon);
            mProjectView = (TextView) view.findViewById(R.id.majorText);
            mMoreImage = (ImageView) view.findViewById(R.id.moreIcon);
            view.setTag(this);
        }
    }

    /**
     * A filter for projects
     */
    private class ProjectCategoryFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                // no filter
                results.values = Arrays.asList(mCategories);
                results.count = mCategories.length;
            } else {
                // perform filter
                List<String> filteredCategories = new ArrayList<>();
                for(String category:mCategories) {
                    boolean match = false;

                    String[] categoryComponents = category.split("-");
                    String[] titleComponents = category.split(" ");
                    if (category.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        // match the project title in any language
                        match = true;
                    } else if (category.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {// || l.getName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        // match the language id or name
                        match = true;
                    } else {
                        // match category id components
                        for(String component:categoryComponents) {
                            if (component.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                                match = true;
                                break;
                            }
                        }
                        if(!match) {
                            // match title components
                            for(String component:titleComponents) {
                                if (component.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }

                    if(match) {
                        filteredCategories.add(category);
                    }
                }
                results.values = filteredCategories;
                results.count = filteredCategories.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            List<String> filteredProjects = ((List<String>) filterResults.values);
            if(filteredProjects != null) {
                mFilteredCategories = filteredProjects.toArray(new String[filterResults.count]);
            } else {
                mFilteredCategories = new String[0];
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Sorts project categories by id
     * @param categories
     * @param referenceId categories are sorted according to the reference id
     */
    private static void sortProjectCategories(List<String> categories, final CharSequence referenceId) {

    }
}
