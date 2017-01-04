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
import org.wycliffeassociates.translationrecorder.project.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sarabiaj on 2/25/2016.
 */
public class TargetBookAdapter extends ArrayAdapter {
    private Book[] mBooks;
    private Book[] mFilteredBooks;
    private BookFilter mBookFilter;

    public TargetBookAdapter(Book[] targetBooks, Context ctx) {
        super(ctx, R.layout.fragment_scroll_list_item);
        List<Book> targetBooksList = Arrays.asList(targetBooks);
        //Collections.sort(targetBooksList);
        mBooks = targetBooksList.toArray(new Book[targetBooksList.size()]);
        mFilteredBooks = mBooks;
    }

    @Override
    public int getCount() {
        if(mFilteredBooks != null) {
            return mFilteredBooks.length;
        } else {
            return 0;
        }
    }

    @Override
    public Book getItem(int position) {
        return mFilteredBooks[position];
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
        holder.mBookView.setText(getItem(position).getName());
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
        if(mBookFilter == null) {
            mBookFilter = new BookFilter();
        }
        return mBookFilter;
    }

    public static class ViewHolder {
        public TextView mBookView;
        public TextView mCodeView;

        public ViewHolder(View view) {
            mBookView = (TextView) view.findViewById(R.id.majorText);
            mCodeView = (TextView) view.findViewById(R.id.minorText);
            view.setTag(this);
        }
    }

    private class BookFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                // no filter
                results.values = Arrays.asList(mBooks);
                results.count = mBooks.length;
            } else {
                // perform filter
                List<Book> filteredCategories = new ArrayList<>();
                for(Book language:mBooks) {
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
            List<Book> filteredBooks = (List<Book>)filterResults.values;
            if(charSequence != null && charSequence.length() > 0) {
                sortBooks(filteredBooks, charSequence);
            }
            mFilteredBooks = filteredBooks.toArray(new Book[filteredBooks.size()]);
            notifyDataSetChanged();
        }
    }

    /**
     * Sorts target books by id
     * @param books
     * @param referenceId languages are sorted according to the reference id
     */
    private static void sortBooks(List<Book> books, final CharSequence referenceId) {
        Collections.sort(books, new Comparator<Book>() {
            @Override
            public int compare(Book lhs, Book rhs) {
                String lhId = lhs.getSlug();
                String rhId = rhs.getSlug();
                // give priority to matches with the reference
                if (lhId.startsWith(referenceId.toString().toLowerCase())) {
                    lhId = "!" + lhId;
                }
                if (rhId.startsWith(referenceId.toString().toLowerCase())) {
                    rhId = "!" + rhId;
                }
                return lhId.compareToIgnoreCase(rhId);
            }
        });
    }
}