package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.project.Chunks;

/**
 * Created by sarabiaj on 6/29/2016.
 */
public class VerseAdapter extends ArrayAdapter {

    //class for caching the views in a row
    private static class ViewHolder {
        TextView mVerseView, mBook;
        ImageButton mRecord, mInfo;
        LinearLayout mTextLayout;
    }

    LayoutInflater mLayoutInflater;
    Activity mCtx;
    ConstantsDatabaseHelper mDb;
    Project mProject;

    public VerseAdapter(Activity context, Project project, Chunks chunks){
        super(context, R.layout.project_list_item, createList(chunks.getNumChapters()));
        mCtx = context;
        mLayoutInflater = context.getLayoutInflater();
        mDb = new ConstantsDatabaseHelper(context);
        mProject = project;
    }

    private static List<Integer> createList(int numChapters) {
        List<Integer> chapterList = new ArrayList<>();
        for(int i = 0; i < numChapters; i++){
            chapterList.add(new Integer(i+1));
        }
        return chapterList;
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        ViewHolder holder;
        if(convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.verse_item, null);
            holder = new ViewHolder();
            //holder.mBook = (TextView) convertView.findViewById(R.id.book_text_view);
            //holder.mVerseView = (TextView) convertView.findViewById(R.id.verse_text_view);
            //holder.mInfo = (ImageButton) convertView.findViewById(R.id.info_button);
            //holder.mRecord = (ImageButton) convertView.findViewById(R.id.record_button);
            //holder.mTextLayout = (LinearLayout) convertView.findViewById(R.id.text_layout);

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.mVerseView.setText("Verse " + (position+1));
//        holder.mBook.setVisibility(View.INVISIBLE);
//        holder.mInfo.setVisibility(View.INVISIBLE);

//        holder.mRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Project.loadProjectIntoPreferences(mCtx, mProject);
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
//                pref.edit().putString(Settings.KEY_PREF_CHAPTER, String.valueOf(position+1)).commit();
//                v.getContext().startActivity(new Intent(v.getContext(), RecordingScreen.class));
//            }
//        });
//
//        holder.mTextLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), ActivityVerseList.class);
//                intent.putExtra(Settings.KEY_PREF_CHAPTER, String.valueOf((position+1)));
//                intent.putExtra(Project.PROJECT_EXTRA, mProject);
//                v.getContext().startActivity(intent);
//            }
//        });

        return convertView;
    }


}