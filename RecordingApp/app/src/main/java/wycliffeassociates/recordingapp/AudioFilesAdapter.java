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
 * Created by Butler on 7/17/2015.
 */
public class AudioFilesAdapter extends ArrayAdapter{
    Context context;
    AudioItem[] audioItems= null;

    public AudioFilesAdapter(Context context, AudioItem[] item){
        super(context, R.layout.audio_list_item, item);
        this.context = context;
        this.audioItems = item;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.audio_list_item, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);

        TextView date = (TextView) convertView.findViewById(R.id.date);

        name.setText(audioItems[position].getName());
        date.setText(audioItems[position].getDate().toString());


        return convertView;
    }

}
