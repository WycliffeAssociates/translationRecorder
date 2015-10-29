package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


import java.text.SimpleDateFormat;

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
    AudioItem[] audioItems= null;

    /**
     * Store the current context
     */
    Context aContext;


    // boolean array for storing
    //the state of each CheckBox
    boolean[] checkBoxState;

    ViewHolder viewHolder;

    //class for caching the views in a row
    private class ViewHolder
    {
        TextView filename, date, duration;
        CheckBox checkBox;
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
        //create the boolean array with
        //initial state as false
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

            //cache the views
            viewHolder.filename = (TextView) convertView.findViewById(R.id.filename);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
            viewHolder.duration.setGravity(Gravity.RIGHT);


            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.date.setGravity(Gravity.RIGHT);

            viewHolder.filename.setTextSize(20f);


            //link the cached views to the convertview
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // set items to be displayed
        viewHolder.filename.setText(audioItems[position].getName());

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String output = format.format(audioItems[position].getDate());
        viewHolder.date.setText(output);

        int length = audioItems[position].getDuration();
        int hours = (length / (60 * 60));
        int minutes = ((length - (60 * 60 * hours)) / 60);
        int seconds = length - (60 * 60 * hours) - (60 * minutes);
        String duration = String.format("%02d",hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        viewHolder.duration.setText(duration);

        viewHolder.checkBox.setChecked(checkBoxState[position]);

        //======
        //AudioFilesListener
        //======
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //AudioFiles.AudioPlay(audioItems[position].getName());
            }

        });


        // for managing the state of the boolean array according to the state of the
        //CheckBox
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    checkBoxState[position] = true;
                }
                else {
                    checkBoxState[position] = false;
                }

            }
        });

            return convertView;
        }
    }