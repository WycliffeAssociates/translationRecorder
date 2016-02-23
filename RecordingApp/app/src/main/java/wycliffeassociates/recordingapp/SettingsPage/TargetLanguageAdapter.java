package wycliffeassociates.recordingapp.SettingsPage;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
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
public class TargetLanguageAdapter extends BaseAdapter {
    private Language[] mLanguages;
    private Language[] mFilteredLanguages;
    private LanguageFilter mLanguageFilter;

    public TargetLanguageAdapter(Language[] targetLanguages) {
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
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_language_list_item, null);
            holder = new ViewHolder(v);
        } else {
            holder = (ViewHolder)v.getTag();
        }

        // render view
        holder.mLanguageView.setText(getItem(position).getName());
        holder.mCodeView.setText(getItem(position).getCode());

        return v;
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
            mLanguageView = (TextView) view.findViewById(R.id.languageName);
            mCodeView = (TextView) view.findViewById(R.id.languageCode);
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
                    boolean match = language.getCode().toLowerCase().startsWith(charSequence.toString().toLowerCase());
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
                String lhId = lhs.getCode();
                String rhId = rhs.getCode();
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