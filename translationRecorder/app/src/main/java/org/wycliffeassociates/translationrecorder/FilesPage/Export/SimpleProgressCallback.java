package org.wycliffeassociates.translationrecorder.FilesPage.Export;

/**
 * Created by sarabiaj on 1/24/2018.
 */

public interface SimpleProgressCallback {
    void onStart(int id);
    void setCurrentFile(int id, String currentFile);
    void setUploadProgress(int id, int progress);
    void onComplete(int id);
}
