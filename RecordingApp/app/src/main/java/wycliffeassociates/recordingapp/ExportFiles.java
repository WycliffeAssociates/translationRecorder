package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Abi D on 7/15/2015.
 */
public class ExportFiles extends Activity
{
    /**
     * The file path to the original file
     */
  private String originalPath = Environment.getExternalStorageDirectory().getAbsolutePath() ;

    /**
     * The name of the file being moved
     */
  private String fileName = "";

    /**
     * The directory currently being navigated through
     */
  private String currentDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * A list of files to show for the current directory
     */
  private ArrayList<String> fileList = new ArrayList<String>();

    /**
     * A String that holds the name of the current folder being viewed
     */
    private String currentFolder = "";

    ArrayAdapter<String> arrayAdapter;
    /**
     * Empty constructor for ExportFiles

    ExportFiles(){
        originalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName = "";
        currentDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileList = new ArrayList<String>();
        setCurrentFolder(Environment.getExternalStorageDirectory().getAbsolutePath());
    }


     * Constructor with parameters
     //* @param oldPath The path to the file
     //* @param name The name of the file

    ExportFiles(String oldPath, String name){
        originalPath = oldPath;
        fileName = name;
        currentDir = oldPath;
        fileList = new ArrayList<String>();
        setCurrentFolder(oldPath);
    }
     */

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setCurrentFolder(Environment.getExternalStorageDirectory().getAbsolutePath());
        System.out.println("reached here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_list);
        ListView list = (ListView)findViewById(R.id.listViewExport);
        //add files to adapter to display to the user
        setFilesInDir(getCurrentDir());
        arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fileList);
        list.setAdapter(arrayAdapter);

        //on item list click -- play
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                switch (position) {
                    case 0:
                        moveUpDir(getCurrentDir());
                        arrayAdapter.notifyDataSetChanged();
                        break;
                    default:
                        moveDownDir(getCurrentDir(), getFilesInDir(getCurrentDir())[position - 1]);
                        arrayAdapter.notifyDataSetChanged();
                        break;
                }
                System.out.println("current directory is now " + getCurrentDir());
            }
        });
    }

    /**
     * Test function
     */
    public void getDir()
    {
       //nothing to show
    }

    /**
     * Gets all of the files in the current directory and lists them
     * @param currentDirectory The current directory
     * @return All of the files in the current directory
     */
    public String[] getFilesInDir(String currentDirectory){
        File directory = new File(currentDirectory);
        if(directory.isDirectory()) {
            String[] dirList = directory.list();
            return dirList;
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
}
