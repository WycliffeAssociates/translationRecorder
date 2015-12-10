package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Arrays;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.PlaybackScreen;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;

/**
 *
 * Creates a custom view for the audio entries in the file screen.
 *
 */
public class AudioFilesAdapter extends ArrayAdapter //implements AudioFilesInterface
{

    /**
     * Populate with audio items
     */
    AudioItem[] audioItems = null;

    /**
     * Store the current context
     */
    Context aContext;

    // Boolean array for storing the state of each CheckBox
    boolean[] checkBoxState;

    ViewHolder viewHolder;

    //class for caching the views in a row
    private class ViewHolder {
        TextView filename, date, time, duration;
        CheckBox checkBox;
        ImageButton playButton;
    }

    /**
     *
     * @param context
     *            The current context
     * @param resource
     *            Array of audio items
     */
    public AudioFilesAdapter(Context context, AudioItem[] resource){
        super(context, R.layout.audio_list_item, resource);
        this.aContext = context;
        this.audioItems = resource;
        // Create the boolean array with initial state as false
        checkBoxState = new boolean[resource.length];
    }

    /**
     * Binds the views into a single view and set the correct information for
     * each of the views
     *
     * @param position
     *            Index of current audio item being manipulated
     * @param convertView
     *            The new view to be created
     * @param parent
     *            The parent that this view will be attached to
     */
    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null) {
            LayoutInflater inflater = ((Activity) aContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.audio_list_item, null);

            viewHolder = new ViewHolder();

            // Cache the views
            viewHolder.filename = (TextView) convertView.findViewById(R.id.filename);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.playButton = (ImageButton) convertView.findViewById(R.id.playButton);

            // Link the cached views to the convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set items to be displayed
        // TODO: Remove extension using Regex
        viewHolder.filename.setText(audioItems[position].getName().replace(".wav", ""));

        //
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String output = format.format(audioItems[position].getDate());
        viewHolder.date.setText(output);

        //
        format = new SimpleDateFormat("hh:mm");
        output = format.format(audioItems[position].getDate());
        viewHolder.time.setText(output);

        //
        int length = audioItems[position].getDuration();
        int hours = (length / (60 * 60));
        int minutes = ((length - (60 * 60 * hours)) / 60);
        int seconds = length - (60 * 60 * hours) - (60 * minutes);
        String duration = String.format("%02d",hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        viewHolder.duration.setText(duration);

        // Set text sizes based on orientation
        float text_coeff;
        if (aContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            text_coeff = 18;
        }
        else {
            text_coeff = 16;
        }
        float textSize = text_coeff * aContext.getResources().getDisplayMetrics().density;
        viewHolder.filename.setTextSize(textSize);
        viewHolder.duration.setTextSize(textSize);
        viewHolder.date.setTextSize(textSize);
        viewHolder.time.setTextSize(textSize);

        //
        viewHolder.checkBox.setChecked(checkBoxState[position]);
        if (checkBoxState[position]) {
            viewHolder.checkBox.setButtonDrawable(R.drawable.ic_check_box_selected);
        }
        else {
            viewHolder.checkBox.setButtonDrawable(R.drawable.ic_check_box_empty);
        }
        if (isAllFalse(checkBoxState)) {
            ((AudioFiles) aContext). hideFragment(R.id.file_actions);
        }
        else {
            ((AudioFiles) aContext).showFragment(R.id.file_actions);
        }

        //
        // convertView.setOnClickListener(new View.OnClickListener() {
        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String filename = audioItems[position].getName();
                System.out.println("FILENAME: " + filename);
                Intent intent = new Intent(getContext(), PlaybackScreen.class);
                intent.putExtra("recordedFilename", AudioInfo.fileDir + "/" + filename);
                intent.putExtra("loadFile", true);
                getContext().startActivity(intent);
            }
        });

        // For managing the state of the boolean array according to the state of the
        //    checkBox
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check state and icon
                if (((CheckBox) v).isChecked()) {
                    checkBoxState[position] = true;
                    ((CheckBox) v).setButtonDrawable(R.drawable.ic_check_box_selected);
                }
                else {
                    checkBoxState[position] = false;
                    ((CheckBox) v).setButtonDrawable(R.drawable.ic_check_box_empty);
                }

                // Check whether to display actions or not
                if (isAllFalse(checkBoxState)) {
                    ((AudioFiles) aContext).hideFragment(R.id.file_actions);
                }
                else {
                    ((AudioFiles) aContext).showFragment(R.id.file_actions);
                }
            }
        });

        return convertView;
    }

    public static boolean isAllFalse(boolean[] array) {
        for (boolean b : array) if (b) return false;
        return true;
    }

}
