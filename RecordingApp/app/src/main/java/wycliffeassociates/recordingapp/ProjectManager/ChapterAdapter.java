package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
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
public class ChapterAdapter extends ArrayAdapter {

    //class for caching the views in a row
    private static class ViewHolder {
        TextView mChapterView, mBook;
        ImageButton mRecord, mInfo;
        LinearLayout mTextLayout;
    }

    LayoutInflater mLayoutInflater;
    Activity mCtx;
    ConstantsDatabaseHelper mDb;
    Project mProject;

    public ChapterAdapter(Activity context, Project project, Chunks chunks){
        super(context, R.layout.project_list_item, createList(chunks.getNumChapters(), project));
        mCtx = context;
        mLayoutInflater = context.getLayoutInflater();
        mDb = new ConstantsDatabaseHelper(context);
        mProject = project;
    }

    private static List<Pair<Integer, Boolean>> createList(int numChapters, Project project) {
        List<Pair<Integer, Boolean>> chapterList = new ArrayList<>();
        int chapter;
        for(int i = 0; i < numChapters; i++){
            chapter = i + 1;
            chapterList.add(new Pair<>( chapter, (isChapterStarted(project, chapter)) ));
        }
        return chapterList;
    }

    private static boolean isChapterStarted(Project project, int chapter){
        File dir = Project.getProjectDirectory(project);
        File[] files = dir.listFiles();
        if(files != null) {
            for (File f : files) {
                if (f.getName().compareTo(String.format("%02d", chapter)) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        ViewHolder holder;
        if(convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.project_list_item, null);
            holder = new ViewHolder();
            holder.mBook = (TextView) convertView.findViewById(R.id.book_text_view);
            holder.mChapterView = (TextView) convertView.findViewById(R.id.language_text_view);
            holder.mInfo = (ImageButton) convertView.findViewById(R.id.info_button);
            holder.mRecord = (ImageButton) convertView.findViewById(R.id.record_button);
            holder.mTextLayout = (LinearLayout) convertView.findViewById(R.id.text_layout);

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mChapterView.setText("Chapter " + (position+1));
        holder.mBook.setVisibility(View.INVISIBLE);
        holder.mInfo.setVisibility(View.INVISIBLE);


        Pair<Integer, Boolean> chapter = (Pair<Integer, Boolean>)this.getItem(position);
        //if the chapter doesn't exist, gray it out
        if(chapter.second == false){
            holder.mChapterView.setTextColor(convertView.getContext().getResources().getColor(R.color.text_light_disabled));
        } else {
            holder.mChapterView.setTextColor(convertView.getContext().getResources().getColor(R.color.dark_primary_text));
        }

        holder.mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Project.loadProjectIntoPreferences(mCtx, mProject);
                v.getContext().startActivity(RecordingScreen.getNewRecordingIntent(v.getContext(), mProject, (position+1), 1));
            }
        });

        holder.mTextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent verseListIntent = ActivityVerseList.getActivityVerseListIntent(v.getContext(), mProject, (position+1));
                v.getContext().startActivity(verseListIntent);
            }
        });

        return convertView;
    }


}
