package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import wycliffeassociates.recordingapp.model.AudioItem;

/**
 *
 * Creates a custom view for the audio entries in the file screen.
 *
 */
public class AudioFilesAdapter extends ArrayAdapter{
    /**
     * Populate with audio items
     */
    AudioItem[] audioItems= null;

    /**
     * Store the current context
     */
    Context aContext;

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
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity)aContext).getLayoutInflater();
        convertView = inflater.inflate(R.layout.audio_list_item, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);

        TextView date = (TextView) convertView.findViewById(R.id.date);

        name.setText(audioItems[position].getName());
        date.setText(audioItems[position].getDate().toString());


        return convertView;
    }

}
