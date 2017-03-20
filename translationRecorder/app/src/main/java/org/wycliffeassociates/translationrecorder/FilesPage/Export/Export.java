package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import org.wycliffeassociates.translationrecorder.ProjectManager.Project;

import java.io.File;
import java.io.IOException;

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

    File mProjectToExport;
    Fragment mCtx;
    Handler mHandler;
    volatile boolean mZipDone = false;
    ProgressUpdateCallback mProgressCallback;
    Project mProject;
    File mZipFile;
    public static final int PROGRESS_REFRESH_RATE = 200; //200 ms refresh for progress dialog (arbitrary value)

    public Export(File directoryToExport, Project project){
        mProjectToExport = directoryToExport;
        mProject = project;
        mHandler = new Handler(Looper.getMainLooper());
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
            Project project = export.getProject();
            String zipName = project.getTargetLanguage() + "_" + project.getVersion() + "_" + project.getBookSlug();
            if(project.isOBS()){
                zipName += project.getAnthology();
            }
            zipName += ".zip";
            mZipFile = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/" + zipName);
            zip(mProjectToExport, mZipFile, export);
        //}
    }

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

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressCallback.setCurrentFile(pm.getFileName());
                        }
                    });
                    while(pm.getState() == ProgressMonitor.STATE_BUSY){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressCallback.setUploadProgress(pm.getPercentDone());
                            }
                        });
                        Thread.sleep(PROGRESS_REFRESH_RATE);
                    }

                } catch (ZipException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressCallback.dismissProgress();
                        export.handleUserInput();
                    }
                });
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressCallback.setZipping(false);
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
