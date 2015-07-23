package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
    boolean[] checkBoxState;

    ViewHolder viewHolder;

    //class for caching the views in a row
    private class ViewHolder
    {
        TextView filename, date;
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
        super(context, R.layout.listitem, resource);
        this.aContext = context;
        this.audioItems = resource;
        //create the boolean array with
        //initial state as false
        System.out.println(resource.length);
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
            convertView = inflater.inflate(R.layout.listitem, null);

            viewHolder = new ViewHolder();

            //cache the views
            viewHolder.filename = (TextView) convertView.findViewById(R.id.filename);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);


            viewHolder.checkBox.setScaleX(3f);
            viewHolder.checkBox.setScaleY(3f);


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

        viewHolder.checkBox.setChecked(checkBoxState[position]);

        //======
        //AudioFilesListener
        //======
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //System.out.println("PLAY AUDIO");
                AudioFiles.AudioPlay(audioItems[position].getName());
                // Do something here.
            }

        });


        // for managing the state of the boolean array according to the state of the
        //CheckBox
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    checkBoxState[position] = true;
                else
                    checkBoxState[position] = false;

            }
        });

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        AudioFiles.AudioExport(audioItems[position].getName(), isChecked);
                    } else {
                        AudioFiles.AudioExport(audioItems[position].getName(), isChecked);
                    }
                }
            });
            return convertView;
        }
    }