package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wycliffeassociates.recordingapp.ConstantsDatabaseHelper;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.project.Chunks;
import wycliffeassociates.recordingapp.widgets.VerseCard;

/**
 * Created by sarabiaj on 6/29/2016.
 */
public class VerseAdapter extends ArrayAdapter {

    //class for caching the views in a row
    private static class ViewHolder {
        TextView mVerseView, mBook;
        ImageView mRecord, mPlayback;
        LinearLayout mTextLayout;
        VerseCard mVerseCard;
    }

    LayoutInflater mLayoutInflater;
    Activity mCtx;
    ConstantsDatabaseHelper mDb;
    Project mProject;
    int mChapter;

    public VerseAdapter(Activity context, Project project, Chunks chunks, int chapter){
        super(context, R.layout.project_list_item, createList(project, chunks, chapter));
        mCtx = context;
        mLayoutInflater = context.getLayoutInflater();
        mDb = new ConstantsDatabaseHelper(context);
        mProject = project;
        mChapter = chapter;
    }

    private static List<Pair<Pair<String, String>, Boolean>> createList(Project project, Chunks chunks, int chapter) {
        List<Map<String, String>> chunkList = chunks.getChunks(chapter);
        int numChunks = chunkList.size();
        String firstvs, endvs;
        ArrayList<Pair<Pair<String, String>, Boolean>> result = new ArrayList<>();
        if(project.getMode().compareTo("chunk") == 0) {
            for (int i = 0; i < numChunks; i++) {
                firstvs = chunkList.get(i).get(Chunks.FIRST_VERSE);
                endvs = chunkList.get(i).get(Chunks.LAST_VERSE);
                boolean exists = getVerseFiles(project, chapter, firstvs, endvs).size() > 0;
                result.add(new Pair(new Pair(firstvs, endvs), exists));
            }
        } else {
            numChunks = Integer.parseInt(chunkList.get(numChunks-1).get(Chunks.LAST_VERSE));
            for (int i = 0; i < numChunks; i++) {
                firstvs = String.valueOf(i+1);
                endvs = "-1";
                boolean exists = getVerseFiles(project, chapter, firstvs, endvs).size() > 0;
                result.add(new Pair(new Pair(firstvs, endvs), exists));
            }
        }
        return result;
    }

    public static List<File> getVerseFiles(Project project, int chapter, String firstvs, String endvs){
        File root = Project.getProjectDirectory(project);
        String chap = String.valueOf(chapter);
        if(chap.length() == 1){
            chap = "0" + chap;
        }
        File folder = new File(root, chap);
        File[] files = folder.listFiles();
        FileNameExtractor fne;
        int first = Integer.parseInt(firstvs);
        int end = Integer.parseInt(endvs);
        ArrayList<File> resultFiles = new ArrayList<>();
        if(files != null){
            for(File file : files){
                fne = new FileNameExtractor(file);
                if(fne.getStartVerse() == first && fne.getEndVerse() == end){
                    resultFiles.add(file);
                }
            }
        }
        return resultFiles;
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        ViewHolder holder;
        if(convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.verse_item, null);
            holder = new ViewHolder();
            //holder.mBook = (TextView) convertView.findViewById(R.id.book_text_view);
            holder.mVerseView = (TextView) convertView.findViewById(R.id.verse_text_view);
            holder.mPlayback = (ImageView) convertView.findViewById(R.id.play_button);
            holder.mRecord = (ImageView) convertView.findViewById(R.id.record_button);
            holder.mVerseCard = (VerseCard) convertView.findViewById(R.id.verse_card);
            //holder.mTextLayout = (LinearLayout) convertView.findViewById(R.id.text_layout);

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mVerseView.setText(mProject.getMode() + " " + ((Pair<Pair<String,String>, Boolean>)(getItem(position))).first.first);
        if(((Pair<Pair<String,String>, Boolean>)(getItem(position))).second){
            holder.mVerseView.setTextColor(mCtx.getResources().getColor(R.color.dark_primary_text));
        } else {
            holder.mVerseView.setTextColor(mCtx.getResources().getColor(R.color.text_light_disabled));
        }
//        holder.mBook.setVisibility(View.INVISIBLE);
//        holder.mInfo.setVisibility(View.INVISIBLE);

        holder.mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Project.loadProjectIntoPreferences(mCtx, mProject);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
                String startVerse, endVerse, verse;
                startVerse = ((Pair<Pair<String,String>, Boolean>)getItem(position)).first.first;
                verse = ((Pair<Pair<String,String>, Boolean>)getItem(position)).first.first;
                endVerse = ((Pair<Pair<String,String>, Boolean>)getItem(position)).first.second;
                pref.edit().putString(Settings.KEY_PREF_START_VERSE, startVerse).commit();
                pref.edit().putString(Settings.KEY_PREF_END_VERSE, endVerse).commit();
                pref.edit().putString(Settings.KEY_PREF_VERSE, verse).commit();
                pref.edit().putString(Settings.KEY_PREF_CHUNK, verse).commit();
                pref.edit().putString(Settings.KEY_PREF_CHAPTER, String.valueOf(mChapter)).commit();
                Settings.updateFilename(getContext());
                v.getContext().startActivity(new Intent(v.getContext(), RecordingScreen.class));
            }
        });

        String firstvs, endvs;
        firstvs = ((Pair<Pair<String,String>, Boolean>)getItem(position)).first.first;
        endvs = ((Pair<Pair<String,String>, Boolean>)getItem(position)).first.second;
        holder.mVerseCard.initialize(getVerseFiles(mProject, mChapter, firstvs, endvs));
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