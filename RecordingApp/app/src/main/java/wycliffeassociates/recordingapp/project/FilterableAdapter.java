package wycliffeassociates.recordingapp.project;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 5/26/2016.
 */
public abstract class FilterableAdapter extends BaseAdapter {
    protected Object[] mCategories;
    protected Object[] mFilteredCategories;
    protected Filter mFilter;

    public FilterableAdapter(Object[] categories) {
        List<Object> categoriesList = Arrays.asList(categories);
        mCategories = categoriesList.toArray(new Object[categoriesList.size()]);
        mFilteredCategories = mCategories;
    }

    @Override
    public int getCount() {
        if (mFilteredCategories != null) {
            return mFilteredCategories.length;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mFilteredCategories[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    /**
     * Updates the data set
     *
     * @param categories
     */
    public void changeData(Object[] categories) {
        mCategories = categories;
        mFilteredCategories = categories;
        notifyDataSetChanged();
    }

    /**
     * Returns the project filter
     *
     * @return
     */
    public abstract Filter getFilter();

    public static class ViewHolder {
        public ImageView mIconImage;
        public TextView mProjectView;
        public ImageView mMoreImage;

        public ViewHolder(View view) {
            mIconImage = (ImageView) view.findViewById(R.id.projectIcon);
            mProjectView = (TextView) view.findViewById(R.id.projectName);
            mMoreImage = (ImageView) view.findViewById(R.id.moreIcon);
            view.setTag(this);
        }
    }

    /**
     * A filter for projects
     */
    private abstract class CategoryFilter extends Filter {

        @Override
        protected abstract FilterResults performFiltering(CharSequence charSequence);

        @Override
        protected abstract void publishResults(CharSequence charSequence, FilterResults filterResults);

//    /**
//     * Sorts project categories by id
//     *
//     * @param categories
//     * @param referenceId categories are sorted according to the reference id
//     */
//    private static void sortProjectCategories(List<String> categories, final CharSequence referenceId) {
//
//    }
    }
}

