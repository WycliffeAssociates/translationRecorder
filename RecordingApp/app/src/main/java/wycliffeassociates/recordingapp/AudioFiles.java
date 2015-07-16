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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class AudioFiles extends Activity {
    Hashtable<String, Date> audioHash = new Hashtable<String, Date>();
    ArrayList<String> audioNameList;
    ArrayList<Date> dateList;
    //0 for Recent Modified
    //1 for Ancient Modified
    //2 for A-Z
    //3 for Z-A
    //4 for 0:00 - 9:99
    //5 for 9:99 - 0:00
    int sortBy = 0;

    String directory = "";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);


        ListView audioList = (ListView)findViewById(R.id.listViewAudio);
        audioNameList = new ArrayList<String>();
        dateList = new ArrayList<Date>();

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

                audioHash.put(file[i].getName(), lastModDate);
            }
        }



        ArrayList<String> stringList = new ArrayList<String>();
        stringList = sortString(audioNameList, true);
        //new ArrayList<String>();

        for(int e = 0; e < stringList.size(); e++){
            System.out.println(stringList.get(e));
        }




        //add files to adapter to display to the user
        ArrayAdapter<Date> arrayAdapter =
                new ArrayAdapter<Date>(this,android.R.layout.simple_list_item_1, dateList);
        audioList.setAdapter(arrayAdapter);


        /*Boolean flag = true;
        /*System.out.println("---------------");
        ArrayList<Date> dateList2 = new ArrayList<Date>();
        ArrayList<Date> dateList3 = new ArrayList<Date>();
        for(int u= 0; u < dateList.size()-1; u++){
            dateList2.add(dateList.get(u));
            dateList3.add(dateList.get(u));
        }

        ArrayList<String> audioList2 = new ArrayList<String>();
        ArrayList<String> audioList3 = new ArrayList<String>();
        for(int u= 0; u < audioNameList.size()-1; u++){
            audioList2.add(audioNameList.get(u));
            audioList3.add(audioNameList.get(u));
        }

        ArrayList<Date> test = sortDate(dateList2, false);
        for(int l = 0; l < test.size(); l++){
            System.out.println(test.get(l));
        }
        System.out.println("---------------");
        ArrayList<Date> test3 = sortDate(dateList3, true);
        for(int l = 0; l < test3.size(); l++){
            System.out.println(test3.get(l));
        }
        System.out.println("---------------");
        ArrayList<String> test2 = sortString(audioList2, flag);
        for(int l = 0; l < test2.size(); l++){
            System.out.println(test2.get(l));
        }
        System.out.println("---------------");
        ArrayList<String> test4 = sortString(audioList3, false);
        for(int l = 0; l < test4.size(); l++){
            System.out.println(test4.get(l));
        }*/
        //on item list click -- play
        audioList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                String selectedAudio = audioNameList.get(position);
                Toast.makeText(getApplicationContext(), "Audio : " + selectedAudio, Toast.LENGTH_LONG).show();
                WavPlayer.play(directory + "/" + selectedAudio);
            }
        });
    }

    //flag == true : sort by most recently modified
    //flag == false : sort by least recently modified
    private ArrayList<Date> sortDate(ArrayList<Date> dList, Boolean flag){
        ArrayList<Date> outputList = new ArrayList<Date>();
        Date cmp = new Date();
        int val = 0;
        int size = dList.size() - 1;

        //as long as there are items
        do{
            size = dList.size()-1;
            cmp = dList.get(size);

            //compare with other items
            for(int x = 0; x < size; x++){
                if (cmp.after(dList.get(x))) {
                    if(flag){
                        //A-Z
                    }else{
                        //Z-A
                        val = x;
                        cmp = dList.get(x);
                    }
                }else{
                    if(flag) {
                        //A-Z
                        val = x;
                        cmp = dList.get(x);
                    }else{
                        //Z-A
                    }
                }
            }
            dList.remove(val);
            outputList.add(cmp);

        }while(size > 0);

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
            cmp = nList.get(size);

            //compare with other items
            for(int x = 0; x < size; x++){
                if (cmp.compareTo(nList.get(x)) < 0) {
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
            nList.remove(val);
            outputList.add(cmp);

        }while(size > 0);

        return outputList;
    }
}
