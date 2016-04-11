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

import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public abstract class Export {

    public interface ProgressUpdateCallback {
        boolean UPLOAD = false;
        boolean ZIP = true;

        void showProgress(boolean mode);
        void incrementProgress(int progress);
        void setUploadProgress(int progress);
        void dismissProgress();
        void setZipping(boolean zipping);
        void setExporting(boolean exporting);
    }

    ArrayList<String> mExportList;
    String mZipPath = null;
    Fragment mCtx;
    int mNumFilesToExport = 0;
    String mCurrentDir;
    volatile boolean mZipDone = false;
    ProgressUpdateCallback mProgressCallback;

    /**
     * Initializes the basic shared data all export operations use
     * @param fileItemList List of audio items contained on the Files page, used to determine checked items
     * @param adapter
     * @param currentDir Directory containing the files
     */
    public Export(ArrayList<FileItem> fileItemList, AudioFilesAdapter adapter, String currentDir){
        populateExportList(fileItemList, adapter, currentDir);
        mNumFilesToExport = mExportList.size();
        mCurrentDir = currentDir;
    }

    public void setFragmentContext(Fragment f){
        mCtx = f;
        mProgressCallback = (ProgressUpdateCallback)f;
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
            zip(toExport, mZipPath +".zip", export);
        }
    }

    /**
     * Generates an arraylist of files (filepath strings) to export, returns if successful
     * @param fileItemList list of File objects referring to files to be exported
     * @param adapter AudioFilesAdapter containing information about whether the item was selected
     * @param currentDir String of the path of the current directory
     * @return Whether or not there are files to export
     */
    protected boolean populateExportList(ArrayList<FileItem> fileItemList,
                                    AudioFilesAdapter adapter, String currentDir){
        mExportList = new ArrayList<>();
        if ((fileItemList.size() == 0)) {
            System.out.println("No items to export");
            return false;
        } else {
            for (int i = 0; i < adapter.getCheckBoxState().length; i++) {
                if (adapter.getCheckBoxState()[i]) {
                    mExportList.add(currentDir + "/" + fileItemList.get(i).getName());
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
    private void zip(final String[] filePaths, final String zipFile, final Export export){
        mZipDone = false;
        mProgressCallback.showProgress(ProgressUpdateCallback.ZIP);
        mProgressCallback.setZipping(true);
        Thread zipThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedInputStream origin = null;
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ZipOutputStream out = new ZipOutputStream(bos);

                    byte data[] = new byte[1024];
                    ArrayList<File> files = new ArrayList<>();
                    for(String file : filePaths){
                        files.add(new File(file));
                    }
                    for (int i = 0; i < files.size(); i++) {
                        String path = "";
                        File file = files.get(i);
                        if(file.isDirectory()){
                            path = file.getName() + "/";
                            File[] contains = file.listFiles();
                            for(File f : contains){
                                files.add(f);
                            }
                        } else {
                            FileInputStream fi = new FileInputStream(files.get(i));
                            origin = new BufferedInputStream(fi, 1024);
                            String name = files.get(i).getName();
                            ZipEntry entry = new ZipEntry(path + files.get(i).getName());
                            out.putNextEntry(entry);
                            int count;
                            while ((count = origin.read(data, 0, 1024)) != -1) {
                                out.write(data, 0, count);
                            }
                            final int progress = i;
                            //Increment progress by number of files done
                            final int size = files.size();
                            mCtx.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressCallback.incrementProgress((int) ((progress / (float) size) * 100));
                                }
                            });
                            origin.close();
                            fi.close();
                        }
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
                        mProgressCallback.dismissProgress();
                        export.handleUserInput();
                    }
                });
                mProgressCallback.setZipping(false);
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
