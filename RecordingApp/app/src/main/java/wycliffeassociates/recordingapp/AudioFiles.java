package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;


import wycliffeassociates.recordingapp.model.AudioItem;

public class AudioFiles extends Activity {

    private DrawerLayout mDrawerLayout;
    private ImageButton btnExport;
    private ImageButton btnCheckAll, btnSortName, btnSortDuration, btnSortDate;

    private ImageButton btnExportApp, btnExportFTP, btnExportFolder;

    private ListView audioFileView;
    private String currentDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private TextView file_path;
    AudioItem[] items2;
    private File file[];

    //0 - check all
    //1,2 - sortName
    //3,4 - sortDuration
    //5,6 - sortDate
    boolean checkAll = true;

    //0, Z-A
    //1, A-Z
    //2, 0:00 - 9:99
    //3, 9:99 - 0:00
    //4, Oldest First
    //5, Recent First
    int sort = 5;

    public AudioFilesAdapter adapter;

    ArrayList<String> audioNameList;
    ArrayList<Date> dateList;
    ArrayList<AudioItem> items;

    Hashtable<Date, String> audioHash = new Hashtable<Date, String>();

    static String directory = "";

    /**
     * the current filepath being exported
     */
    private String thisPath;

    /**
     * the total number of files being exported
     */
    private int totalFiles = 0;

    /**
     * the number of the current file bein exported (corresponds with allMoving)
     */
    private int fileNum =0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);

        audioFileView = (ListView) findViewById(R.id.listViewExport);

        file_path = (TextView)findViewById(R.id.filepath);
        file_path.setText(currentDir);

        audioNameList = new ArrayList<String>();
        dateList = new ArrayList<Date>();
        items = new ArrayList<AudioItem>();

        final PreferencesManager pref = new PreferencesManager(this);
        directory = pref.getPreferences("fileDirectory") + "/" + pref.getPreferences("fileFolder");

        sort = (int) pref.getPreferences("displaySort");

        //get files in the directory
        File f = new File(directory);
        file = f.listFiles();
        if (file == null) {
            Toast.makeText(AudioFiles.this, "No Audio Files in Folder", Toast.LENGTH_SHORT).show();
        }
        else {
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
            ArrayList<Date> testDate = new ArrayList<Date>();
            ArrayList<String> testName = new ArrayList<String>();
            switch(sort){
                case 0:
                    testName = sortString(audioNameList, false);
                    break;
                case 1:
                    testName = sortString(audioNameList, true);
                    break;
                case 4:
                    testDate = sortDate(dateList, false);
                    break;
                case 5:
                default:
                    testDate = sortDate(dateList, true);
                    break;
            }
            if(sort == 0 || sort == 1){
                items2 = new AudioItem[testName.size()];
                for (int j = 0; j < testName.size(); j++) {
                    //audioNameList.set(j,audioHash.get(testDate.get(j)));
                    items2[j] = new AudioItem((testName.get(j)), dateList.get(j), 0);
                }
            }else{
                items2 = new AudioItem[testDate.size()];
                for (int j = 0; j < testDate.size(); j++) {
                    //audioNameList.set(j,audioHash.get(testDate.get(j)));
                    items2[j] = new AudioItem(audioHash.get(testDate.get(j)), testDate.get(j), 0);
                }
            }
            adapter = new AudioFilesAdapter(this, items2);
            audioFileView.setAdapter(adapter);

        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //move this to AudioFilesAdapter -- ultimately to AudioFilesListener

        btnExport = (ImageButton)findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        btnExportFolder = (ImageButton)findViewById(R.id.btnExportFolder);
        btnExportFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportList = new ArrayList<String>();
                if ((file == null)) {
                    Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (int i = 0; i < adapter.checkBoxState.length; i++) {
                        if (adapter.checkBoxState[i] == true) {
                            AudioFiles.AudioExport(items2[i].getName(), adapter.checkBoxState[i]);
                        }
                    }
//                    Intent intent = new Intent(v.getContext(), ExportFiles.class);
                    if (exportList.size() > 0) {
//                        intent.putExtra("exportList", exportList);
//                        startActivityForResult(intent, 0);
                        totalFiles = exportList.size();
                        thisPath = exportList.get(0);
                        createFile("audio/*", getNameFromPath(thisPath));
                    } else {
                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnExportApp = (ImageButton)findViewById(R.id.btnExportApp);
        btnExportApp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                exportList = new ArrayList<String>();
                for (int i = 0; i < adapter.checkBoxState.length; i++) {
                    if (adapter.checkBoxState[i] == true) {
                        AudioFiles.AudioExport(items2[i].getName(), adapter.checkBoxState[i]);
                    }
                }

                //if something is checked
                if(exportList.size() > 0) {
                    exportShareIT(exportList);
                }
                else {
                    Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });



        audioFileView = (ListView) findViewById(R.id.listViewExport);

        audioNameList = new ArrayList<String>();
        dateList = new ArrayList<Date>();
        items = new ArrayList<AudioItem>();


        //get output directory
        //global current directory?

        /*WavRecorder temp = new WavRecorder(new RecordingManager() {
            @Override
            public void onClick(View v) {
                if (file == null) {

                } else {
                    for (int i = 0; i < audioFileView.getCount(); i++) {
                        adapter.checkBoxState[i] = checkAll;
                        adapter.notifyDataSetChanged();
                    }
                    if (checkAll == false) {
                        exportList = new ArrayList<String>();
                        Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
                        adapter.notifyDataSetChanged();
                    }
                    checkAll = !checkAll;
                }
            }

        });*/
        //directory = temp.getFileDirectory();

    //move
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void exportShareIT(ArrayList<String> exportList){

        Intent sendIntent = new Intent();
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //share it implementation
        File tFile;

        //individual file
        if(exportList.size() < 2){
            Uri audioUri;

            tFile = new File (exportList.get(0));
            audioUri = Uri.fromFile(tFile);
            sendIntent.setAction(Intent.ACTION_SEND);

            //send individual URI
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);

            //multiple files
        }else{

            ArrayList<Uri> audioUris = new ArrayList<Uri>();
            for(int i=0; i<exportList.size(); i++){
                tFile = new File(exportList.get(i));
                audioUris.add(Uri.fromFile(tFile));
            }
            sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);

            //send multiple arrayList of URIs
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
        }

        //open
        sendIntent.setType("audio/*");
        startActivity(Intent.createChooser(sendIntent, "Export Audio"));
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
            nList.remove(val);
            outputList.add(cmp);

        }while(size > 0);

        return outputList;
    }

    //pls, so bad
    public static void AudioPlay (String fileName){
        WavPlayer.play(directory + "/" + fileName);
    }

    static ArrayList<String> exportList = new ArrayList<String>();

    public static void AudioExport (String fileName, boolean flag){
        if(flag){
            if(exportList.contains(directory + "/" + fileName)){

            }else{
                exportList.add(directory + "/" + fileName);
            }
        }else{
            exportList.remove(directory + "/" + fileName);
        }
    }

    @Override
    public void onBackPressed(){
        if (file == null) {}
        else {
            exportList = new ArrayList<String>();
            Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
        }
        finish();
    }

    /**
     * Iterates the file number that is being looked a
     * @return Returns true if iteration worked, false if the end has been reached
     */
    public boolean iteratePath(){
        if(fileNum + 1 < totalFiles) {
            fileNum++;
            return true;
        }
        if(fileNum + 1 == totalFiles){
            fileNum++;
            return false;
        }
        return false;
    }

    /**
     * A method to extract filename from the path
     * @param path The paths to the files
     * @return The simple filename of the file
     */
    public String getNameFromPath(String path){
        String[] temp = path.split("/");
        return temp[temp.length-1];
    }

    /**
     * Copies a file from a path to a uri
     * @param destUri The desination of the file
     * @param path The original path to the file
     */
    public void savefile(Uri destUri, String path)
    {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            String sourceFilename = path;
            ParcelFileDescriptor destinationFilename = getContentResolver().
                    openFileDescriptor(destUri, "w");
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename.getFileDescriptor()));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            iteratePath();
            if(fileNum < totalFiles) {
                thisPath = exportList.get(fileNum);
                createFile("audio/*", getNameFromPath(thisPath));
            }
        }

    }

    /**
     * Creates a file in folder selected by user
     * @param mimeType Typically going to be "audio/*" for this app
     * @param fileName The name of the file selected.
     */
    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT );
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        this.startActivityForResult(intent, 43);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 43) {
                currentUri = resultData.getData();
                savefile(currentUri, exportList.get(fileNum));

            }
        }
    }
}

