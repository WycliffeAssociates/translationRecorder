package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import wycliffeassociates.recordingapp.model.AudioItem;

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
    AudioItem[] audioItems= null;

    /**
     * Store the current context
     */
    Context aContext;

    // boolean array for storing
    //the state of each CheckBox
    boolean[] checkBoxState = null;

    ViewHolder viewHolder;

    private class ViewHolder {
        TextView filename;
        CheckBox checkBox;
        TextView date;
    }

    /**
     *
     * @param context
     *            The current context
     * @param resource
     *            Array of audio items
     */
    public AudioFilesAdapter(Context context, AudioItem[] resource){
        super(context, R.layout.listitem, resource);
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
    public View getView(final int position, View convertView, ViewGroup parent){
        checkBoxState = new boolean[audioItems.length];
        LayoutInflater inflater = ((Activity)aContext).getLayoutInflater();
        convertView = inflater.inflate(R.layout.listitem, parent, false);
        viewHolder=new ViewHolder();
        viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        viewHolder.filename = (TextView) convertView.findViewById(R.id.filename);

        viewHolder.date = (TextView) convertView.findViewById(R.id.date);
        viewHolder.date.setGravity(Gravity.RIGHT);

        viewHolder.filename.setText(audioItems[position].getName());
        viewHolder.date.setText(audioItems[position].getDate().toString());

        convertView.setTag(viewHolder);
        viewHolder.checkBox.setChecked(checkBoxState[position]);

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    checkBoxState[position] = true;
                else
                    checkBoxState[position] = false;

            }

            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    AudioFiles.AudioExport(audioItems[position].getName(), isChecked);
                    //System.out.println("EXPORT : " + audioItems[position].getName());
                } else {
                    AudioFiles.AudioExport(audioItems[position].getName(), isChecked);
                    //System.out.println("UNCHECKED");
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //System.out.println("PLAY AUDIO");
                AudioFiles.AudioPlay(audioItems[position].getName());
                // Do something here.
            }
        });

        return convertView;
    }

}