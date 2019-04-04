package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sarabiaj on 1/24/2018.
 */

public class ZipProject {

    public static int ZIP_PROJECT_ID = 2;

    public static final int PROGRESS_REFRESH_RATE = 200; //200 ms refresh for progress dialog (arbitrary value)

    final ArrayList<File> mFiles;
    final File mDirectory;

    //Arraylist explicitly specified because of zip4j dependency
    public ZipProject(ArrayList<File> files) {
        mFiles = files;
        mDirectory = null;
    }

    public ZipProject(File directory) {
        mDirectory = directory;
        mFiles = null;
    }

    public void zip(final File outFile, final SimpleProgressCallback progressCallback) {
        Thread zipThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Trying to delete file if it exists,
                    // because if for some reason file is corrupted
                    // ZipFile will crash
                    if(outFile.exists()) {
                        outFile.delete();
                    }
                    ZipFile zipper = new ZipFile(outFile);
                    ZipParameters zp = new ZipParameters();
                    zipper.setRunInThread(true);
                    zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
                    //zip.addFiles(files, zp);
                    final ProgressMonitor pm = zipper.getProgressMonitor();
                    if (mDirectory != null) {
                        zipper.addFolder(mDirectory, zp);
                    } else if (mFiles != null) {
                        zipper.addFiles(mFiles, zp);
                    }
                    if (progressCallback != null) {
                        progressCallback.onStart(ZIP_PROJECT_ID);
                        while (pm.getState() == ProgressMonitor.STATE_BUSY) {
                            progressCallback.setCurrentFile(ZIP_PROJECT_ID, pm.getFileName());
                            progressCallback.setUploadProgress(ZIP_PROJECT_ID, pm.getPercentDone());
                            Thread.sleep(PROGRESS_REFRESH_RATE);
                        }
                    }
                } catch (ZipException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (progressCallback != null) {
                    progressCallback.onComplete(ZIP_PROJECT_ID);
                }
            }
        });
        zipThread.start();
    }
}
