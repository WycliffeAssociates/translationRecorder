package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Fragment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public abstract class Export {

    ArrayList<String> mExportList;
    String mZipPath = null;
    Fragment mCtx;
    int mNumFilesToExport = 0;
    String mCurrentDir;

    /**
     * Initializes the basic shared data all export operations use
     * @param audioItemList List of audio items contained on the Files page, used to determine checked items
     * @param adapter
     * @param currentDir Directory containing the files
     * @param ctx Context of the fragment which contained the export options
     */
    public Export(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir, Fragment ctx){
        mCtx = ctx;
        populateExportList(audioItemList, adapter, currentDir);
        mNumFilesToExport = mExportList.size();
        mCurrentDir = currentDir;
        if(mNumFilesToExport > 1){
            zipFiles();
        }
    }

    /**
     * Guarantees that all Export objects will have an export method
     */
    public abstract void export();

    public void cleanUp(){
        if(mZipPath != null){
            File file = new File(mZipPath);
            file.delete();
        }
    }

    /**
     * Zips files if more than one file is selected
     */
    //TODO: Zip file appears to just use the name of the first file, what should this change to?
    protected void zipFiles(){
        //files should only be zipped if more than one are selected
        if (mNumFilesToExport > 1) {
            String toExport[] = new String[mExportList.size()];
            String thisPath = mExportList.get(0);
            for (int i = 0; i < mExportList.size(); i++) {
                toExport[i] = mExportList.get(i);
            }
            try {
                // This could cause problems if the directory list contains matches
                mZipPath = thisPath.replaceAll("(\\.)([A-Za-z0-9]{3}$|[A-Za-z0-9]{4}$)", ".zip");
                zip(toExport, mZipPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates an arraylist of files (filepath strings) to export, returns if successful
     * @param audioItemList list of File objects referring to files to be exported
     * @param adapter AudioFilesAdapter containing information about whether the item was selected
     * @param currentDir String of the path of the current directory
     * @return Whether or not there are files to export
     */
    protected boolean populateExportList(ArrayList<AudioItem> audioItemList,
                                    AudioFilesAdapter adapter, String currentDir){
        mExportList = new ArrayList<>();
        if ((audioItemList.size() == 0)) {
            System.out.println("No items to export");
            return false;
        } else {
            for (int i = 0; i < adapter.getCheckBoxState().length; i++) {
                if (adapter.getCheckBoxState()[i]) {
                    mExportList.add(currentDir + "/" + audioItemList.get(i).getName());
                }
            }
        }
        if(mNumFilesToExport > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Zips files into a single folder
     * @param files A String array of the paths to the files to be zipped
     * @param zipFile The location of the zip file as a String
     * @throws IOException
     */
    private void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[1024];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, 1024);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }

}
