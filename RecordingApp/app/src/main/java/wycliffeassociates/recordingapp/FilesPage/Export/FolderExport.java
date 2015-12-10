package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public class FolderExport extends Export{

    int mFileNum = 0;
    private Uri mCurrentUri;
    private String thisPath;


    public FolderExport(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir, Fragment ctx){
        super(audioItemList, adapter, currentDir, ctx);
    }

    public boolean export(){
        //export a zip
        if (mExportList.size() > 1) {
            createFile("application/zip", getNameFromPath(mZipPath));
        //export a single wav
        } else {
            createFile("audio/*", getNameFromPath(mExportList.get(0)));
        }
        return true;
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
        mCtx.startActivityForResult(intent, 43);
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
     * Iterates the file number that is being looked a
     * @return Returns true if iteration worked, false if the end has been reached
     */
    public boolean iteratePath(){
        if(mFileNum + 1 < mNumFilesToExport) {
            mFileNum++;
            return true;
        }
        if(mFileNum + 1 == mNumFilesToExport){
            mFileNum++;
            return false;
        }
        return false;
    }
}
