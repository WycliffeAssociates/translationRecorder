package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
import wycliffeassociates.recordingapp.R;

/**
 * Class to export files between directories
 * @author Abi Gundy
 * @version 7/20/15
 *
 */
public class ExportFiles extends Activity
{
    /**
     * The full paths + names of the files to be moved
     */
    private ArrayList<String> allMoving = new ArrayList<String>();

    /**
     * The directory currently being navigated through
     */
    private String currentDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * A list of files to show for the current directory
     */
    private ArrayList<String> fileList = new ArrayList<>();

    /**
     * A String that holds the name of the current folder being viewed
     */
    private String currentFolder = "";

    /**
     * The adapter that displays the list of files in the FMS
     */
   private ArrayAdapter<String> arrayAdapter;

    /**
     * The list of items in the current directory
     */
    private ListView list;

    /**
     * Displays up a directory
     */
    private TextView backBar;

    /**
     * preferences manager
     */
    private static PreferencesManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        pref = new PreferencesManager(this);
        setCurrentFolder(pref.getPreferences("fileDirectory").toString());
        setCurrentDir(pref.getPreferences("fileDirectory").toString());
        setContentView(R.layout.save_location_menu);

        backBar = (TextView)findViewById(R.id.backBar);
        updateBackBar(getCurrentDir());
        backBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                moveUpDir(getCurrentDir());
                arrayAdapter.notifyDataSetChanged();
            }
        });

        //add files to adapter to display to the user
        setFilesInDir(getCurrentDir());

        list = (ListView)findViewById(R.id.folderList);
        arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fileList);
        list.setAdapter(arrayAdapter);

        //on item list click move up or down directories
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                moveDownDir(getCurrentDir(), getFilesInDir(getCurrentDir())[position]);
                arrayAdapter.notifyDataSetChanged();
            }
        });

        //the buttons at the bottom of the screen
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
    }

    /**
     * The listener for the save & cancel clicks
     */
    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnSave: {
                    saveDirectory(pref);
                    break;
                }
            }
        }
    };

    /**
     * Gets all of the files in the current directory and lists them
     * @param currentDirectory The current directory
     * @return All of the files in the current directory
     */
    public String[] getFilesInDir(String currentDirectory){
        File directory = new File(currentDirectory);
        if(directory.isDirectory()) {
            return directory.list();
        }
        else
            return new String[0];
    }

    /**
     * resets file list for directory
     * @param currentDirectory The current directory
     */
    public void setFilesInDir(String currentDirectory){
        try {
            File directory = new File(currentDirectory);
            if (directory.isDirectory()) {
                String[] dirList = directory.list();
                if(dirList != null) {
                    String[] files = new String[dirList.length];
                    for (int i = 0; i < dirList.length; i++) {
                        files[i] = dirList[i];
                    }
                    setFileList(files);
                    updateBackBar(currentDirectory);
                }
            }
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves the current directory to the next directory up (parent dir)
     * @param currentDirectory The current directory
     */
    public void moveUpDir(String currentDirectory) {
        File directory = new File(currentDirectory);
        String path[] = directory.getParent().split("/");
        if (path.length > 1) {
            String finalPath = "";
            for (int i = 0; i < path.length - 1; i++)
                finalPath = finalPath + path[i] + "/";
            finalPath += path[path.length - 1];
            File test = new File(finalPath);
            if(test.isDirectory()) {
                try {
                    setCurrentFolder(finalPath);
                    setCurrentDir(finalPath);
                    setFilesInDir(finalPath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * This function sets the current directory path name after moving down one directory
     * @param currentDirectory The current directory
     * @param nextFolder the folder to add to the path
     */
    public void moveDownDir(String currentDirectory, String nextFolder){
        File dir = new File(currentDirectory + "/" + nextFolder);
        try {
            if (dir.isDirectory()&& dir.list() != null) {
                try {
                    setCurrentFolder(currentDirectory + "/" + nextFolder);
                    setCurrentDir(currentDirectory + "/" + nextFolder);
                    setFilesInDir(currentDirectory + "/" + nextFolder);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * The getter for the current directory
     * @return The current directory
     */
    public String getCurrentDir() {
        return currentDir;
    }

    /**
     * The setter for the current directory
     * @param currentDir The current directory
     */
    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * getter for the list of folders in the
     * current directory
     * @return The file list
     */
    public ArrayList<String> getFileList() {
        return fileList;
    }

    /**
     * The setter for the list of folders in the current directory
     * @param list a list of new files
     */
    public void setFileList(String[] list) {
        fileList.clear();
        for(int i = 0; i < list.length; i++)
            fileList.add(list[i]);
    }

    /**
     * Given the pathname to the folder gets the name of the
     * last folder in the path
     * @param path The complete pathname to the folder (includes folder)
     */
    public void setCurrentFolder(String path) {
        String[] folders = path.split("/");
        currentFolder = folders[folders.length-1];
    }

    /**
     * The Getter for the current folder
     * @return Gets the current folder name
     */
    public String getCurrentFolder(){
        return currentFolder;
    }

    /**
     * Copies a source file to another file (will create a new file if need be
     * @param source The source path & filename
     * @param dest The destination path & filename
     */
    public void copyFileUsingFileChannels(File source, File dest) {
        FileChannel inputChannel,outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Closes the file manager
     */
    public void cancelExport(){
        this.finish();
    }

    /**
     * A method to extract filenames from their paths
     * @param paths THe paths to the files
     * @return The simple filenames of the files
     */
    public ArrayList<String> getNamesFromPath(ArrayList<String> paths){
        ArrayList<String> names = new ArrayList<>();
        for(int i = 0; i < paths.size(); i++){
            String[] temp = paths.get(i).split("/");
            names.add(temp[temp.length-1]);
        }
        return names;
    }

    /**
     * Gets an array of all of the files to move over
     * @return An ArrayList<String> of files to move
     */
    public ArrayList<String> getAllMoving(){
        return allMoving;
    }

    /**
     * Sets the new default save directory to the selected directory
     * @param prefs The preference manager
     */
    public void saveDirectory(PreferencesManager prefs) {
        String dir = getCurrentDir()+ "/thisshouldbedeleted";
        File fDir = new File(dir);
            try{
               boolean created = fDir.mkdir();
               if(created) {
                   fDir.delete();
                   prefs.setPreferences("fileDirectory", getCurrentDir());
                   Intent resultIntent = new Intent();
                   setResult(Activity.RESULT_OK, resultIntent);
                   finish();
               }
                else  Toast.makeText(getApplicationContext(),
                       "Please select a directory that can be written to"
                       , Toast.LENGTH_LONG).show();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * Updates top bar to display the parent directory
     * @param newDir the parent directory
     */
    public void updateBackBar(String newDir){
        try {
            File directory = new File(newDir);
            if (directory.isDirectory()) {
                backBar.setText( "Go back to " + directory.getParent());
            }
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
    }
}