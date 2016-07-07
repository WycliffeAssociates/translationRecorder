package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Fragment;
import android.os.Environment;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;
import wycliffeassociates.recordingapp.ProjectManager.Project;

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
        void setCurrentFile(String currentFile);
    }

//    ArrayList<String> mExportList;
//    String mZipPath = null;
//    int mNumFilesToExport = 0;
//    String mCurrentDir;
    File mProjectToExport;
    Fragment mCtx;
    volatile boolean mZipDone = false;
    ProgressUpdateCallback mProgressCallback;
    Project mProject;
    File mZipFile;

//    /**
//     * Initializes the basic shared data all export operations use
//     * @param fileItemList List of audio items contained on the Files page, used to determine checked items
//     * @param adapter
//     * @param currentDir Directory containing the files
//     */
//    public Export(ArrayList<FileItem> fileItemList, AudioFilesAdapter adapter, String currentDir){
//        populateExportList(fileItemList, adapter, currentDir);
//        mNumFilesToExport = mExportList.size();
//        mCurrentDir = currentDir;
//    }

    public Export(File directoryToExport, Project project){
        mProjectToExport = directoryToExport;
        mProject = project;
    }

    public void setFragmentContext(Fragment f){
        mCtx = f;
        mProgressCallback = (ProgressUpdateCallback)f;
    }

    /**
     * Guarantees that all Export objects will have an export method
     */
    public abstract void export();

//    public static boolean shouldZip(List<String> exportList){
//        if(exportList.size() > 1){
//            return true;
//        } else if(exportList.size() > 0){
//            File file = new File(exportList.get(0));
//            if(file.isDirectory()){
//                return true;
//            }
//        }
//        return false;
//    }

    public void cleanUp(){
        mZipFile.delete();
    }

    public Project getProject(){
        return mProject;
    }

    /**
     * Zips files if more than one file is selected
     */
    //TODO: Zip file appears to just use the name of the first file, what should this change to?
    protected void zipFiles(Export export){
        //files should only be zipped if more than one are selected
//        if (shouldZip(mExportList)) {
//            String toExport[] = new String[mExportList.size()];
//            String thisPath = mExportList.get(0);
//            for (int i = 0; i < mExportList.size(); i++) {
//                toExport[i] = mExportList.get(i);
//            }
            // This could cause problems if the directory list contains matches
            //mZipPath = thisPath.replaceAll("(\\.)([A-Za-z0-9]{3}$|[A-Za-z0-9]{4}$)", ".zip");
            Project project = export.getProject();
            String zipName = project.getTargetLanguage() + "_" + project.getSource() + "_" + project.getSlug();
            if(project.isOBS()){
                zipName += project.getProject();
            }
            zipName += ".zip";
            mZipFile = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/" + zipName);
            zip(mProjectToExport, mZipFile, export);
        //}
    }

//    /**
//     * Generates an arraylist of files (filepath strings) to export, returns if successful
//     * @param fileItemList list of File objects referring to files to be exported
//     * @param adapter AudioFilesAdapter containing information about whether the item was selected
//     * @param currentDir String of the path of the current directory
//     * @return Whether or not there are files to export
//     */
//    protected boolean populateExportList(ArrayList<FileItem> fileItemList,
//                                    AudioFilesAdapter adapter, String currentDir){
//        mExportList = new ArrayList<>();
//        if ((fileItemList.size() == 0)) {
//            System.out.println("No items to export");
//            return false;
//        } else {
//            for (int i = 0; i < adapter.getCheckBoxState().length; i++) {
//                if (adapter.getCheckBoxState()[i]) {
//                    mExportList.add(currentDir + "/" + fileItemList.get(i).getName());
//                }
//            }
//        }
//        if(mNumFilesToExport > 0){
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * Zips files into a single folder
     * @param projectToZip Directory containing the project to zip
     * @param zipFile The location of the zip file that the project is being saved to
     * @throws IOException
     */
    private void zip(final File projectToZip, final File zipFile, final Export export){
        mZipDone = false;
        mProgressCallback.showProgress(ProgressUpdateCallback.ZIP);
        mProgressCallback.setZipping(true);
        Thread zipThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZipFile zipper = new ZipFile(zipFile);
                    ZipParameters zp = new ZipParameters();
                    zipper.setRunInThread(true);
                    zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
                    //zip.addFiles(files, zp);
                    final ProgressMonitor pm = zipper.getProgressMonitor();

                    zipper.addFolder(projectToZip, zp);

                    mCtx.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressCallback.setCurrentFile(pm.getFileName());
                        }
                    });
                    while(pm.getState() == ProgressMonitor.STATE_BUSY){
                        mProgressCallback.setUploadProgress(pm.getPercentDone());
                    }

                } catch (ZipException e) {
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
