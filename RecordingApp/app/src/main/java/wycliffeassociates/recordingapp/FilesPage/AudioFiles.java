package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;

public class AudioFiles extends Activity {

    private CheckBox btnCheckAll;

    private ImageButton btnSortName, btnSortDuration, btnSortDate;

    private Menu mMenu;
    private ListView audioFileView;
    // private TextView file_path;
    private static String currentDir;
    private File file[];

    private ArrayList<AudioItem> audioItemList;
    private ArrayList<AudioItem> tempItemList;
    static ArrayList<String> exportList;

    private boolean checkAll = true;

    // 0: Z-A
    // 1: A-Z
    // 2: 0:00 - 9:99
    // 3: 9:99 - 0:00
    // 4: Oldest First
    // 5: Recent First
    int sort = 5;

    public AudioFilesAdapter adapter;

    Hashtable<Date, String> audioHash;

    /**
     * URI of the document in storage where it was exported by the user
     */
    private Uri currentUri;

    /**
     * The path to the zipfile
     */
    private String zipPath = null;

    /**
     * the current filepath being exported
     */
    private String thisPath;

    /**
     * the total number of files being exported
     */
    private int totalFiles = 0;

    /**
     * the number of the current file being exported (corresponds with allMoving)
     */
    private int fileNum = 0;

    PreferencesManager pref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);

        // Hide the fragment to start with
        hideFragment(R.id.file_actions);

        // Pull file directory and sorting preferences
        pref = new PreferencesManager(this);
        currentDir = (String) pref.getPreferences("fileDirectory");
        AudioInfo.fileDir = currentDir;
        sort = (int) pref.getPreferences("displaySort");

        audioFileView = (ListView) findViewById(R.id.main_content);

        // Initialization
        audioItemList = new ArrayList<AudioItem>();
        tempItemList = new ArrayList<AudioItem>();
        audioHash = new Hashtable<Date, String>();

        // Cleanup any leftover visualization files
        removeUnusedVisualizationFiles(currentDir);

        //get files in the directory
        File f = new File(currentDir);
        file = f.listFiles();
        // No files
        if (file == null) {
            Toast.makeText(AudioFiles.this, "No Audio Files in Folder", Toast.LENGTH_SHORT).show();
        // Get audio files
        } else {
            initFiles(file);
        }
        setButtonHandlers();
    }

    private void initFiles(File[] file){
        for (int i = 0; i < file.length; i++) {
            int len = file[i].getName().length();
            if (len > 3) {
                String sub = file[i].getName().substring(len - 4);
                if (sub.equalsIgnoreCase(".3gp") || sub.equalsIgnoreCase(".wav")
                        || sub.equalsIgnoreCase(".mp3")) {
                    // Add file names
                    Date lastModDate = new Date(file[i].lastModified());
                    File tFile = new File(currentDir + "/" + file[i].getName());
                    long time = (((tFile.length() - 44) / 2) / 44100);
                    //create an Audio Item
                    tempItemList.add(new AudioItem(file[i].getName(), lastModDate, (int) time));
                }
            }
            generateAdapterView(tempItemList, sort);
        }
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnCheckAll).setOnClickListener(btnClick);
        findViewById(R.id.btnSortName).setOnClickListener(btnClick);
        findViewById(R.id.btnSortDuration).setOnClickListener(btnClick);
        findViewById(R.id.btnSortDate).setOnClickListener(btnClick);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnCheckAll: {
                    checkAll();
                    break;
                }
                case R.id.btnSortName: {
                    sortName();
                    break;
                }
                case R.id.btnSortDuration: {
                    sortDuration();
                    break;
                }
                case R.id.btnSortDate: {
                    sortDate();
                    break;
                }
            }
        }
    };

    private void checkAll(){
        if (file != null) {
            for (int i = 0; i < audioFileView.getCount(); i++) {
                adapter.checkBoxState[i] = checkAll;
                adapter.notifyDataSetChanged();
            }
            if (!checkAll) {
                exportList = new ArrayList<String>();
                if(adapter != null && adapter.checkBoxState != null){
                    Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
                    adapter.notifyDataSetChanged();
                }
                btnCheckAll.setButtonDrawable(R.drawable.ic_select_all_empty);
                hideFragment(R.id.file_actions);
            } else {
                btnCheckAll.setButtonDrawable(R.drawable.ic_select_all_selected);
                showFragment(R.id.file_actions);
            }
            checkAll = !checkAll;
        }
    }

    private void sortName(){
        if (sort == 1) {
            pref.setPreferences("displaySort", 0);
        } else {
            pref.setPreferences("displaySort", 1);
        }
        sort = (int) pref.getPreferences("displaySort");
        generateAdapterView(tempItemList, sort);
    }

    private void sortDuration(){
        if (sort == 3) {
            pref.setPreferences("displaySort", 2);
        } else {
            pref.setPreferences("displaySort", 3);
        }
        sort = (int) pref.getPreferences("displaySort");
        generateAdapterView(tempItemList, sort);
    }

    private void sortDate(){
        if (sort == 5) {
            pref.setPreferences("displaySort", 4);
        } else {
            pref.setPreferences("displaySort", 5);
        }
        sort = (int) pref.getPreferences("displaySort");
        generateAdapterView(tempItemList, sort);
    }

    public void showShareDialog(View v){
        FragmentManager fm = getFragmentManager();
        FragmentShareDialog d = new FragmentShareDialog();
        d.setFilesForExporting(audioItemList, adapter, currentDir);
        d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        d.show(fm, "Share Dialog");
    }

    public void showDeleteConfirmDialog(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentDeleteDialog d = new FragmentDeleteDialog();
        d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        d.show(fm, "Delete Confirm Dialog");
    }

    public void confirmDelete() {
        exportList = new ArrayList<String>();
        for (int i = 0; i < adapter.checkBoxState.length; i++) {
            if (adapter.checkBoxState[i]) {
                exportList.add(currentDir + "/" + audioItemList.get(i).getName());
            }
        }
        if (exportList.size() > 0) {
            deleteFiles(exportList);
            Toast.makeText(AudioFiles.this, "File has been deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AudioFiles.this, "Select a file to delete", Toast.LENGTH_SHORT).show();
        }
        sort = (int) pref.getPreferences("displaySort");
        generateAdapterView(tempItemList, sort);
        hideFragment(R.id.file_actions);
    }

    private void removeUnusedVisualizationFiles(String filesDir){
        File audioFilesLocation = new File(filesDir);
        File visFilesLocation = new File(AudioInfo.pathToVisFile);
        File[] visFiles = visFilesLocation.listFiles();
        File[] audioFiles = audioFilesLocation.listFiles();
        for(File v : visFiles){
            boolean found = false;
            for(File a : audioFiles){
                //check if the names match up; exclude the path to get to them or the file extention
                if(extractFilename(a).equals(extractFilename(v))){
                    found = true;
                    break;
                }
            }
            if(!found){
                System.out.println("Removing " + v.getName());
                v.delete();
            }
        }
    }

    private String extractFilename(File a){
        if(a.isDirectory()){
            return "";
        }
        String nameWithExtention = a.getName();
        if(nameWithExtention.lastIndexOf('.') < 0 || nameWithExtention.lastIndexOf('.') > nameWithExtention.length()){
            return "";
        }
        String filename = nameWithExtention.substring(0, nameWithExtention.lastIndexOf('.'));
        return filename;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    /**
     *Clears Check Box State when the back button is pressed
     */
    public void onBackPressed(){
        if (file == null) {}
        else {
            exportList = new ArrayList<String>();
            if(adapter != null && adapter.checkBoxState != null)
            Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
        }
        finish();
    }

    private void generateAdapterView(ArrayList<AudioItem> tempItemList, int sort){
        //
        ArrayList<AudioItem> cleanList = new ArrayList<AudioItem>();
        for (int a = 0; a < tempItemList.size(); a++){
            cleanList.add(tempItemList.get(a));
        }

        // Clear list
        audioItemList = new ArrayList<AudioItem>();
        audioItemList = sortAudioItem(cleanList, sort);

        // Temp array for Adapter
        AudioItem[] tempArr = new AudioItem[audioItemList.size()];
        for(int a = 0; a < audioItemList.size(); a++){
            tempArr[a] = audioItemList.get(a);

        }

        // Set Adapter view
        adapter = (new AudioFilesAdapter(this, tempArr));
        audioFileView.setAdapter(adapter);
    }

    private void deleteFiles(ArrayList<String> exportList){
        //int count = 0;

        for (int i = 0; i < exportList.size(); i++) {
            File file = new File(exportList.get(i));
//            boolean deleted = file.delete();
//            if (deleted){
            if (file.delete()) {
                String value = exportList.get(i).replace(currentDir + "/", "");
                for (int a = 0; a < tempItemList.size(); a++) {
                    if (tempItemList.get(a).getName().equals(value)){
                        tempItemList.remove(a);
                        a = tempItemList.size() + 2;
                    }
                }
                //tempItemList.remove(i - count);
                //System.out.println("========" + (i - count));
                //count++;
            }
        }
    }

    private ArrayList<AudioItem> sortAudioItem(ArrayList<AudioItem> nList, int sort) {
        //
        ArrayList<AudioItem> outputList = new ArrayList<AudioItem>();
        if (nList.size() > 0) {
            boolean flag = false;
            switch (sort) {
                case 0:
                case 2:
                case 4:
                    //false
                    break;
                case 1:
                case 3:
                case 5:
                default:
                    flag = true;
                    break;
            }

            int val = 0;
            int size = nList.size() - 1;

            if (sort == 0 || sort == 1) {
                String cmp = "";

                // As long as there are items...
                do {
                    //
                    size = nList.size() - 1;
                    cmp = nList.get(size).getName().toLowerCase();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp.compareTo(nList.get(x).getName().toLowerCase()) < 0) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getName();
                            }
                        } else {
                            if (flag) {
                                //A-Z
                                val = x;
                                cmp = nList.get(x).getName();
                            } else {
                                //Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);

            } else if (sort == 2 || sort == 3) {
                //
                Integer cmp = 0;

                // As long as there are items...
                do {
                    size = nList.size() - 1;
                    cmp = nList.get(size).getDuration();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp > nList.get(x).getDuration()) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getDuration();
                            }
                        } else {
                            if (flag) {
                                //A-Z
                                val = x;
                                cmp = nList.get(x).getDuration();
                            } else {
                                //Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);

            } else {
                //
                ArrayList<Date> tempList = new ArrayList<Date>();
                Date cmp = new Date();

                // As long as there are items
                do {
                    //
                    size = nList.size() - 1;
                    cmp = nList.get(size).getDate();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp.after(nList.get(x).getDate())) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getDate();
                            }
                        } else {
                            if (flag) {
                                // A-Z
                                val = x;
                                cmp = nList.get(x).getDate();
                            } else {
                                // Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);
            }

        } else {
            System.out.println("empty");
        }

        return outputList;
    }

    public void hideFragment(int view) {
        View fragment = findViewById(view);
        if (fragment.getVisibility() == View.VISIBLE) {
            fragment.setVisibility(View.GONE);
        }
    }

    public void showFragment(int view) {
        View fragment = findViewById(view);
        if (fragment.getVisibility() == View.GONE) {
            fragment.setVisibility(View.VISIBLE);
        }
    }

}
