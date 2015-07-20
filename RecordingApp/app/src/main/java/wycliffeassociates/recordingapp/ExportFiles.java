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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Class to export files between directories
 * @author Abi Gundy
 * @version 7/17/15
 *
 */
public class ExportFiles extends Activity
{
    /**
     * The file path to the original file
     */
  private String originalPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AudioRecorder" ;

    /**
     * The name of the file being moved
     */
  private String fileName = "test.wav";

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
    ArrayAdapter<String> arrayAdapter;

    /**
     * The list of items in the current directory
     */
    ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        setCurrentFolder(Environment.getExternalStorageDirectory().getAbsolutePath());
        System.out.println("reached here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_list);
        list = (ListView)findViewById(R.id.listViewExport);
        //add files to adapter to display to the user
        setFilesInDir(getCurrentDir());
        arrayAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, fileList);
        list.setAdapter(arrayAdapter);

        //on item list click -- play
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                if(position == 0) {
                    moveUpDir(getCurrentDir());
                    arrayAdapter.notifyDataSetChanged();
                }
                else{
                    moveDownDir(getCurrentDir(), getFilesInDir(getCurrentDir())[position - 1]);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(btnClick);
        findViewById(R.id.btnSave).setOnClickListener(btnClick);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnSave:{
                    saveFile();
                    break;
                }
                case R.id.btnCancel:
                    cancelExport();
                    break;
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
                    String[] files = new String[dirList.length + 1];
                    files[0] = "Go back to " + directory.getParent();
                    for (int i = 0; i < dirList.length; i++) {
                        files[i + 1] = dirList[i];
                    }
                    setFileList(files);
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
     * Setter for changing the original path
     * @param newPath the path to change it to
     */
    public void setOriginalPath(String newPath){originalPath = newPath;}

    /**
     * Getter for the original path
     * @return the original path
     */
    public String getOriginalPath() {
        return originalPath;
    }

    /**
     * Setter for changing the file name
     * @param fileName The new name for the file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter for the file name
     * @return the file name
     */
    public String getFileName() {
        return fileName;
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
     * Copies multiple source files to another file (will create new files if need be)
     * @param source The source paths & filenames
     * @param dest The destination paths & filenames
     */
    public void copyFilesUsingFileChannels(ArrayList<File> source, ArrayList<File> dest) {
        //TODO figure out how this will work
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            for(int i = 0; i < source.size(); i++) {
                inputChannel = new FileInputStream(source.get(i)).getChannel();
                outputChannel = new FileOutputStream(dest.get(i)).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            }
            inputChannel.close();
            outputChannel.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Copies the file from one location to another without parameters
     */
    public void saveFile(){
        File source = new File(getOriginalPath() + "/" + getFileName());
        File dest = new File(getCurrentDir() + "/" + getFileName());
        copyFileUsingFileChannels(source, dest);
        Toast.makeText(getApplicationContext(), "File Exported to " + dest, Toast.LENGTH_LONG).show();
        finish();
    }

    public void cancelExport(){
        finish();
    }
}
