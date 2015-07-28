package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_list);
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            allMoving = extras.getStringArrayList("exportList");
            totalFiles = allMoving.size();
            for (int i = 0; i < allMoving.size(); i++) {
                thisPath = allMoving.get(i);
                createFile("audio/*", getNameFromPath(thisPath));
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 43) {
                currentUri = resultData.getData();
                savefile(currentUri, allMoving.get(fileNum));
            }
        }
    }

    /**
     * Creates a file in folder selected by user
     * @param mimeType Typically going to be "audio/*" for this app
     * @param fileName The name of the file selected.
     */
    private void createFile(String mimeType, String fileName) {
        System.out.println("ABI: preparing intent");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT );
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        this.startActivityForResult(intent, 43);
        System.out.println("ABI: activity started");
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
            System.out.println("ABI: final path " + destinationFilename.getFileDescriptor());
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
            if(!iteratePath())
            {
                System.out.println("ABI: start activity)");
                Intent back = new Intent(this,AudioFiles.class);
                startActivity(back);
            }
        }

    }

    /**
     * A method to extract filename from the path
     * @param path THe paths to the files
     * @return The simple filename of the file
     */
    public String getNameFromPath(String path){
            String[] temp = path.split("/");
           return temp[temp.length-1];
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

}