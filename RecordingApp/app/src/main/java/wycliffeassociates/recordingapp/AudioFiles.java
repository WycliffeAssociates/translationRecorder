package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Butler on 7/14/2015.
 */
public class AudioFiles extends Activity {
    ArrayList<String> audioNameList;
    String directory = "";
/*
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);


        ListView audioList = (ListView)findViewById(R.id.listViewAudio);
        audioNameList = new ArrayList<String>();

        //get output directory
        WavRecorder temp = new WavRecorder();
        directory = temp.getFileDirectory();

        //get files in the directory
        File f = new File(directory);
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++) {
            int len = file[i].getName().length();
            String sub = file[i].getName().substring(len - 4);

            if (sub.equalsIgnoreCase(".3gp") || sub.equalsIgnoreCase(".wav")
                    || sub.equalsIgnoreCase(".mp3")) {
                audioNameList.add(file[i].getName());
            }
        }

        //add files to adapter to display to the user
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, audioNameList);
        audioList.setAdapter(arrayAdapter);

        //on item list click -- play
        audioList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                String selectedAudio = audioNameList.get(position);
                Toast.makeText(getApplicationContext(), "Audio : " + selectedAudio, Toast.LENGTH_LONG).show();
                WavPlayer.play(directory + "/" + selectedAudio);
            }
        });
    }*/
}
