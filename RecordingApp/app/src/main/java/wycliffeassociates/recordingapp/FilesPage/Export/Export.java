package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Fragment;
import android.app.ProgressDialog;

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
    volatile boolean mZipDone = false;

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
    protected void zipFiles(Export export){
        //files should only be zipped if more than one are selected
        if (mNumFilesToExport > 1) {
            String toExport[] = new String[mExportList.size()];
            String thisPath = mExportList.get(0);
            for (int i = 0; i < mExportList.size(); i++) {
                toExport[i] = mExportList.get(i);
            }
            // This could cause problems if the directory list contains matches
            mZipPath = thisPath.replaceAll("(\\.)([A-Za-z0-9]{3}$|[A-Za-z0-9]{4}$)", ".zip");
            zip(toExport, mZipPath, export);
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
    private void zip(final String[] files, final String zipFile, final Export export){
        mZipDone = false;
        final ProgressDialog pd = new ProgressDialog(mCtx.getActivity());
        pd.setTitle("Packaging files to export.");
        pd.setMessage("Please wait...");
        pd.setProgress(0);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
        Thread zipThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedInputStream origin = null;
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ZipOutputStream out = new ZipOutputStream(bos);

                    byte data[] = new byte[1024];
                    long sizeOfFiles = 0;
                    for(String s : files){
                        File f = new File(s);
                        sizeOfFiles+=f.getTotalSpace();
                    }
                    int percent = (int)(sizeOfFiles/(8.f)/100.f);
                    int percentCount = percent;
                    for (int i = 0; i < files.length; i++) {
                        FileInputStream fi = new FileInputStream(files[i]);
                        origin = new BufferedInputStream(fi, 1024);
                        ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, 1024)) != -1) {
                            out.write(data, 0, count);
                            percentCount -= 1024;
                            if(percentCount<=0){
                                mCtx.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.incrementProgressBy(1);
                                    }
                                });
                                percentCount = percent;
                            }
                        }
                        origin.close();
                        fi.close();
                    }
                    out.flush();
                    out.close();
                    bos.flush();
                    bos.close();
                    fos.flush();
                    fos.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
                mCtx.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        export.handleUserInput();
                    }
                });
                mZipDone = true;
            }
        });
        zipThread.start();
    }

    /**
     * Handles the step of the upload following the zipping of files
     * This may mean starting an activity to ask the user where to save,
     * or it may just mean calling upload.
     */
    protected abstract void handleUserInput();
}
