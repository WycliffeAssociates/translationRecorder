package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;

/**
 *
 * Creates a custom view for the audio entries in the file screen.
 *
 */
public class ProjectAdapter extends ArrayAdapter {
    //class for caching the views in a row
    private static class ViewHolder {
        TextView mLanguage, mBook;
        ImageButton mRecord, mInfo;
    }

    LayoutInflater mLayoutInflater;
    List<Project> mProjectList;

    public ProjectAdapter(Activity context, List<Project> projectList){
        super(context, R.layout.project_list_item, projectList);
        mProjectList = projectList;
        mLayoutInflater = context.getLayoutInflater();
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if(convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.project_list_item, null);
            holder = new ViewHolder();
            holder.mBook = (TextView) convertView.findViewById(R.id.book_text_view);
            holder.mLanguage = (TextView) convertView.findViewById(R.id.language_text_view);
            holder.mInfo = (ImageButton) convertView.findViewById(R.id.info_button);
            holder.mRecord = (ImageButton) convertView.findViewById(R.id.record_button);

            // Link the cached views to the convertView
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mBook.setText(mProjectList.get(position).getSlug());
        holder.mLanguage.setText(mProjectList.get(position).getTargetLang());

        holder.mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), RecordingScreen.class));
            }
        });

        return convertView;

//        // Set items to be displayed
//        viewHolder.filename.setText(fileItems[position].getName().replace(".wav", ""));
//
//        //
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
//        String output = format.format(fileItems[position].getDate());
//        viewHolder.date.setText(output);
//
//        //
//        format = new SimpleDateFormat("hh:mm");
//        output = format.format(fileItems[position].getDate());
//        viewHolder.time.setText(output);
//
//        if(!((FileItem)getItem(position)).isDirectory()) {
//            int length = fileItems[position].getDuration();
//            int hours = (length / (60 * 60));
//            int minutes = ((length - (60 * 60 * hours)) / 60);
//            int seconds = length - (60 * 60 * hours) - (60 * minutes);
//            String duration = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
//            viewHolder.duration.setText(duration);
//        }
//        //
//        viewHolder.checkBox.setChecked(checkBoxState[position]);
//        if (checkBoxState[position]) {
//            viewHolder.checkBox.setButtonDrawable(R.drawable.ic_check_box_selected);
//        } else {
//            viewHolder.checkBox.setButtonDrawable(R.drawable.ic_check_box_empty);
//        }
//        if (isAllFalse(checkBoxState)) {
//            ((AudioFiles) aContext). hideFragment(R.id.file_actions);
//        } else {
//            ((AudioFiles) aContext).showFragment(R.id.file_actions);
//        }
//
//        if(!((FileItem)getItem(position)).isDirectory()) {
//            // convertView.setOnClickListener(new View.OnClickListener() {
//            viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    String filename = fileItems[position].getName();
//                    System.out.println("FILENAME: " + filename);
//                    Intent intent = new Intent(getContext(), PlaybackScreen.class);
//                    intent.putExtra("recordedFilename", AudioInfo.fileDir + "/" + filename);
//                    intent.putExtra("loadFile", true);
//                    getContext().startActivity(intent);
//                }
//            });
//        }
//
//        // For managing the state of the boolean array according to the state of the
//        //    checkBox
//        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Check state and icon
//                if (((CheckBox) v).isChecked()) {
//                    checkBoxState[position] = true;
//                    ((CheckBox) v).setButtonDrawable(R.drawable.ic_check_box_selected);
//                } else {
//                    checkBoxState[position] = false;
//                    ((CheckBox) v).setButtonDrawable(R.drawable.ic_check_box_empty);
//                }
//
//                // Check whether to display actions or not
//                if (isAllFalse(checkBoxState)) {
//                    ((AudioFiles) aContext).hideFragment(R.id.file_actions);
//                } else {
//                    ((AudioFiles) aContext).showFragment(R.id.file_actions);
//                }
//            }
//        });
//
//        if(((FileItem)getItem(position)).isDirectory()) {
//            View.OnClickListener ocl = new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(aContext);
//                    String oldDir = pref.getString("fileDirectory", "");
//                    pref.edit().putString("fileDirectory", oldDir + "/" + ((FileItem)getItem(position)).getName()).commit();
//                    System.out.println(pref.getString("fileDirectory", ""));
//                    ((AudioFiles) aContext).refreshView();
//                }
//            };
//            viewHolder.filename.setOnClickListener(ocl);
//            viewHolder.date.setOnClickListener(ocl);
//            convertView.findViewById(R.id.directoryImage).setOnClickListener(ocl);
//        }
    }

}
