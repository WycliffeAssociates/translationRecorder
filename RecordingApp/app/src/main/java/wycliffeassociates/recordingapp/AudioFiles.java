package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;



import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;


import wycliffeassociates.recordingapp.model.AudioItem;

public class AudioFiles extends Activity {
    ListView audioFileView;

    ArrayList<String> audioNameList;
    ArrayList<Date> dateList;
    ArrayList<AudioItem> items;

    Hashtable<Date, String> audioHash = new Hashtable<Date, String>();
    //ArrayList<Button> buttonList;
    //0 for Recent Modified
    //1 for Ancient Modified
    //2 for A-Z
    //3 for Z-A
    //4 for 0:00 - 9:99
    //5 for 9:99 - 0:00
    int sortBy = 0;

    static String directory = "";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);


        audioFileView = (ListView) findViewById(R.id.listViewAudio);

        audioNameList = new ArrayList<String>();
        dateList = new ArrayList<Date>();
        items = new ArrayList<AudioItem>();


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

                Date lastModDate = new Date(file[i].lastModified());
                dateList.add(lastModDate);

                audioHash.put(lastModDate, file[i].getName());

                //items.add(new AudioItem(file[i].getName()));
            }
        }

        //sort by date
        ArrayList<Date> testDate = sortDate(dateList, true);

        AudioItem[] items2 = new AudioItem[testDate.size()];
        //get names of files that are now sorted by date
        for (int j = 0; j < testDate.size(); j++) {
            //audioNameList.set(j,audioHash.get(testDate.get(j)));
            items2[j] = new AudioItem(audioHash.get(testDate.get(j)), testDate.get(j), 0);
        }

        AudioFilesAdapter adapter = new AudioFilesAdapter(this, items2);
        audioFileView.setAdapter(adapter);


        /*audioFileView.setOnClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                String selectedAudio = audioNameList.get(position);
                //System.out.println("====");
                Toast.makeText(getApplicationContext(), "Audio : " + selectedAudio, Toast.LENGTH_LONG).show();
                WavPlayer.play(directory + "/" + selectedAudio);
            }
        });*/

    }

    //this is terrible... please interface
    public static void AudioPlay (String fileName){
        WavPlayer.play(directory + "/" + fileName);
    }

    static ArrayList<String> exportList = new ArrayList<String>();

    public static void AudioExport (String fileName){
        //System.out.println
        exportList.add(fileName);
    }

    private void ExportLocal (String currDir, String nDir, String fileName){

    }

    //move

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    //flag == true : sort by most recently modified
    //flag == false : sort by least recently modified
    private ArrayList<Date> sortDate(ArrayList<Date> dList, Boolean flag) {
        ArrayList<Date> outputList = new ArrayList<Date>();
        Date cmp = new Date();
        int val = 0;
        int size = dList.size() - 1;

        //as long as there are items
        do {
            size = dList.size() - 1;
            cmp = dList.get(size);
            val = size;

            //compare with other items
            for (int x = 0; x < size; x++) {
                if (cmp.after(dList.get(x))) {
                    if (flag) {
                        //A-Z
                    } else {
                        //Z-A
                        val = x;
                        cmp = dList.get(x);
                    }
                } else {
                    if (flag) {
                        //A-Z
                        val = x;
                        cmp = dList.get(x);
                    } else {
                        //Z-A
                    }
                }
            }
            dList.remove(val);
            outputList.add(cmp);

        } while (size > 0);

        return outputList;
    }


    //._. Generics tho
    //flag == true : sort by A-Z
    //flag == false : sort by Z-A
    private ArrayList<String> sortString (ArrayList<String> nList, Boolean flag){
        ArrayList<String> outputList = new ArrayList<String>();

        String cmp = "";
        int val = 0;
        int size = nList.size() - 1;

        //as long as there are items
        do{
            size = nList.size()-1;
            cmp = nList.get(size).toLowerCase();
            val = size;

            //compare with other items
            for(int x = 0; x < size; x++){
                if (cmp.compareTo(nList.get(x).toLowerCase()) < 0) {
                    if(flag){
                        //A-Z
                    }else{
                        //Z-A
                        val = x;
                        cmp = nList.get(x);
                    }
                }else{
                    if(flag) {
                        //A-Z
                        val = x;
                        cmp = nList.get(x);
                    }else{
                        //Z-A
                    }
                }
            }
            System.out.println(val);
            nList.remove(val);
            outputList.add(cmp);

        }while(size > 0);

        return outputList;
    }


}
