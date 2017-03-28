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
import org.wycliffeassociates.translationrecorder.project.components.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by joel on 9/4/2015.
 */
public class TargetLanguageAdapter extends ArrayAdapter {
    private Language[] mLanguages;
    private Language[] mFilteredLanguages;
    private LanguageFilter mLanguageFilter;

    public TargetLanguageAdapter(Language[] targetLanguages, Context ctx) {
        super(ctx, R.layout.fragment_scroll_list_item);
        List<Language> targetLanguagesList = Arrays.asList(targetLanguages);
        Collections.sort(targetLanguagesList);
        mLanguages = targetLanguagesList.toArray(new Language[targetLanguagesList.size()]);
        mFilteredLanguages = mLanguages;
    }


    @Override
    public int getCount() {
        if(mFilteredLanguages != null) {
            return mFilteredLanguages.length;
        } else {
            return 0;
        }
    }

    @Override
    public Language getItem(int position) {
        return mFilteredLanguages[position];
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
        holder.mLanguageView.setText(getItem(position).getName());
        holder.mCodeView.setText(getItem(position).getSlug());


        LinearLayout ll = (LinearLayout)v.findViewById(R.id.scroll_list_item_layout);
        ll.removeView(ll.findViewById(R.id.itemIcon));
        LinearLayout rmll = (LinearLayout)ll.findViewById(R.id.rightmost_scroll_list_item_layout);
        rmll.removeView((rmll.findViewById(R.id.moreIcon)));


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
        if(mLanguageFilter == null) {
            mLanguageFilter = new LanguageFilter();
        }
        return mLanguageFilter;
    }

    public static class ViewHolder {
        public TextView mLanguageView;
        public TextView mCodeView;

        public ViewHolder(View view) {
            mLanguageView = (TextView) view.findViewById(R.id.majorText);
            mCodeView = (TextView) view.findViewById(R.id.minorText);
            view.setTag(this);
        }
    }

    private class LanguageFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                // no filter
                results.values = Arrays.asList(mLanguages);
                results.count = mLanguages.length;
            } else {
                // perform filter
                List<Language> filteredCategories = new ArrayList<>();
                for(Language language:mLanguages) {
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
            List<Language> filteredLanguages = (List<Language>)filterResults.values;
            if(charSequence != null && charSequence.length() > 0) {
                sortLanguages(filteredLanguages, charSequence);
            }
            mFilteredLanguages = filteredLanguages.toArray(new Language[filteredLanguages.size()]);
            notifyDataSetChanged();
        }
    }

    /**
     * Sorts target languages by id
     * @param languages
     * @param referenceId languages are sorted according to the reference id
     */
    private static void sortLanguages(List<Language> languages, final CharSequence referenceId) {
        Collections.sort(languages, new Comparator<Language>() {
            @Override
            public int compare(Language lhs, Language rhs) {
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