package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public class FolderExport extends Export{

    public FolderExport(ArrayList<AudioItem> audioItemList,
                        AudioFilesAdapter adapter, String currentDir, Fragment ctx){
        super(audioItemList, adapter, currentDir, ctx);
    }

    /**
     * Exports to a folder or SD card by starting a wrapper activity around the Storage Access Framework
     */
    public void export(){
        Intent i = new Intent(mCtx.getActivity(), StorageAccess.class);
        System.out.println("size of export list is " + mExportList.size());
        i.putStringArrayListExtra("exportList", mExportList);
        i.putExtra("zipPath", mZipPath);
        mCtx.startActivity(i);
    }

    public static class StorageAccess extends Activity{

        private Uri mCurrentUri;
        private String mThisPath;
        private String mZipPath;
        private int mTotalFiles = 0;
        private int mFileNum = 0;
        private ArrayList<String> mExportList;
        private int mNumFilesToExport;
        private final int SAVE_FILE = 43;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Intent intent = getIntent();
            mExportList = intent.getStringArrayListExtra("exportList");
            mZipPath = intent.getStringExtra("zipPath");

            mThisPath = mExportList.get(0);
            mNumFilesToExport = mExportList.size();
            //export a zip
            if (mExportList.size() > 1) {
                createFile("application/zip", getNameFromPath(mZipPath));
                //export a single wav
            } else {
                createFile("audio/*", getNameFromPath(mExportList.get(0)));
            }
        }

        /**
         * Closes the activity on a back press to return back to the files page
         */
        @Override
        public void onBackPressed(){
            this.finish();
        }

        /**
         * Receives the user selected location to save to as a Uri
         * @param requestCode should be set to SAVE_FILE to continue with the export
         * @param resultCode equals RESULT_OK if chosing a location completed
         * @param resultData contains the Uri to save to
         */
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent resultData) {
            mCurrentUri = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SAVE_FILE) {
                    mCurrentUri = resultData.getData();
                    if(null!= mZipPath){
                        savefile(mCurrentUri, mZipPath);
                        mZipPath = null;//reset
                    }//
                    else
                        savefile(mCurrentUri, mThisPath);
                }

                if(requestCode == 3){//delete zip file, needs to be done after upload
                    mZipPath = null;//set null for next time
                }
            } else {
                finish();
            }
        }

        /**
         * Copies a file from a path to a uri
         * @param destUri The destination of the file
         * @param path The original path to the file
         */
        public void savefile(Uri destUri, String path)
        {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                String sourceFilename = path;
                ParcelFileDescriptor destFilename = getContentResolver().openFileDescriptor(destUri, "w");
                FileInputStream fis = new FileInputStream(sourceFilename);
                bis = new BufferedInputStream(fis);
                Logger.w(this.toString(), "Source file is " + sourceFilename);
                FileOutputStream fos = new FileOutputStream(destFilename.getFileDescriptor());
                bos = new BufferedOutputStream(fos);
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

                //not very well abstracted, but if we are working with non-zip-files
                //keep saving files
                if(!path.contains(".zip")) {
                    iteratePath();
                    if (mFileNum < mTotalFiles) {
                        mThisPath = mExportList.get(mFileNum);
                        createFile("audio/*", getNameFromPath(mThisPath));
                    }
                }
                else//we just transferred a zip file, the old file needs to be deleted
                {
                    File toDelete = new File(path);
                    try {
                        toDelete.getCanonicalFile().delete();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
                this.finish();
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
            startActivityForResult(intent, SAVE_FILE);
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
}
