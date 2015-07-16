package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;

import java.io.File;
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
  private String originalPath;

    /**
     * The name of the file being moved
     */
  private String fileName;

    /**
     * The directory currently being navigated through
     */
  private String currentDir;

    /**
     * A list of files to show for the current directory
     */
  private ArrayList<String> fileList;

    /**
     * Constructor with parameters
     * @param oldPath The path to the file
     * @param name The name of the file
     */
    ExportFiles(String oldPath, String name){
        originalPath = oldPath;
        fileName = name;
        currentDir = oldPath;
        fileList = new ArrayList<String>();
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view a list of files
        setContentView(R.layout.export_list);

        //THe list we want to view
        ListView exportList = (ListView)findViewById(R.id.listViewExport);
    }

    /**
     * Test function
     */
    public void getDir()
    {
        //the list of all files in the directory
        fileList = new ArrayList<String>();

        File directory = new File(getOriginalPath());
        if(directory.isDirectory())
        {
            System.out.println("Parent path" + directory.getParent());
            String path[] = directory.getParent().split("/");
            System.out.println("prev parent " + path[path.length-1]);
            System.out.println("move up to " + moveUpDir(directory.getParent()));
            String files[] = getFilesInDir(moveUpDir(directory.getParent()));
            for(int i = 0; i < files.length; i++) {
            fileList.add(files[i]);
            System.out.println("Items in the directory: " + files[i]);
            }
            System.out.println("Move down to " + moveDownDir(moveUpDir(directory.getParent()),files[1]));

        }
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
        File directory = new File(currentDirectory);
        ArrayList<String> files = new ArrayList<String>();
        if(directory.isDirectory()) {
            String[] dirList = directory.list();
            for(int i = 0; i < dirList.length; i++)
                files.add(dirList[i]);
        }
        setFileList(files);
    }

    /**
     * Finds the next directory up and returns path to it
     * @param currentDirectory The current directory
     * @return The directory one folder up
     */
    public String moveUpDir(String currentDirectory)
    {
        File directory = new File(currentDirectory);
        String path[] = directory.getParent().split("/");
        String finalPath = "";
        for(int i = 0; i < path.length-1; i++)
            finalPath = finalPath + path[i] + "/";
        finalPath += path[path.length-1];
        return finalPath;
    }

    /**
     * This function gets a new path name after moving down one directory
     * @param currentDirectory The current directory
     * @param nextFolder the folder to add to the path
     * @return
     */
    public String moveDownDir(String currentDirectory, String nextFolder){
        return currentDirectory + "/" + nextFolder;
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
    public void setFileList(ArrayList<String> list) {
        this.fileList = list;
    }
}
