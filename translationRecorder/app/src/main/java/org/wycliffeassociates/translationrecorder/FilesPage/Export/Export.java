package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.StringUtils;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public abstract class Export implements SimpleProgressCallback {

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

        void setProgressTitle(String title);
    }

    File mDirectoryToZip;
    ArrayList<File> mFilesToZip;
    Fragment mCtx;
    Handler mHandler;
    volatile boolean mZipDone = false;
    ProgressUpdateCallback mProgressCallback;
    Project mProject;
    File mZipFile;
    public static final int PROGRESS_REFRESH_RATE = 200; //200 ms refresh for progress dialog (arbitrary value)

    public Export(File directoryToExport, Project project) {
        mDirectoryToZip = directoryToExport;
        mFilesToZip = null;
        mProject = project;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public Export(ArrayList<File> filesToExport, Project project) {
        mDirectoryToZip = null;
        mFilesToZip = filesToExport;
        mProject = project;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setFragmentContext(Fragment f) {
        mCtx = f;
        mProgressCallback = (ProgressUpdateCallback) f;
    }

    /**
     * Guarantees that all Export objects will have an export method
     */
    public void export() {
        initialize();
    }

    protected void initialize() {
        if (mDirectoryToZip != null) {
            zip(mDirectoryToZip);
        } else {
            zip(mFilesToZip);
        }
    }

    /**
     * Handles the step of the upload following the zipping of files
     * This may mean starting an activity to ask the user where to save,
     * or it may just mean calling upload.
     */
    protected abstract void handleUserInput();


    public void cleanUp() {
        mZipFile.delete();
    }

    public Project getProject() {
        return mProject;
    }

    /**
     * Zips files if more than one file is selected
     */
    //TODO: Zip file appears to just use the name of the first file, what should this change to?
    protected File outputFile() {
        Project project = mProject;
        String zipName = StringUtils.join(
                new String[]{
                        project.getTargetLanguageSlug(),
                        project.getAnthologySlug(),
                        project.getVersionSlug(),
                        project.getBookSlug(),
                },
                "_"
        );
        zipName += ".zip";
        File root = new File(mCtx.getActivity().getExternalCacheDir(), "upload");
        root.mkdirs();
        mZipFile = new File(root, zipName);
        return mZipFile;
    }

    /**
     * Zips files into a single folder
     *
     * @param directoryToZip Directory containing the project to zip
     * @throws IOException
     */
    private void zip(File directoryToZip) {
        ZipProject zp = new ZipProject(directoryToZip);
        zp.zip(outputFile(), this);
    }

    private void zip(ArrayList<File> filesToZip) {
        ZipProject zp = new ZipProject(filesToZip);
        zp.zip(outputFile(), this);
    }

    public void onStart(final int id) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {


                mZipDone = false;
                mProgressCallback.setZipping(true);
                mProgressCallback.showProgress(ProgressUpdateCallback.ZIP);

                if (id == TranslationExchangeDiff.DIFF_ID) {
                    mProgressCallback.setProgressTitle("Step 1/2: Generating manifest file");
                } else if(id == ZipProject.ZIP_PROJECT_ID) {
                    mProgressCallback.setProgressTitle("Step 2/2: Packaging files to export");
                }
            }
        });
    }

    public void setCurrentFile(int id, final String currentFile) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                mProgressCallback.setCurrentFile(currentFile);
            }
        });
    }

    public void setUploadProgress(int id, final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                mProgressCallback.setUploadProgress(progress);
            }
        });
    }

    public void onComplete(final int id) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {


                mZipDone = true;
                mProgressCallback.setZipping(false);
                mProgressCallback.dismissProgress();
                if (id == ZipProject.ZIP_PROJECT_ID) {
                    handleUserInput();
                }
            }
        });
    }
}
