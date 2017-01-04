package org.wycliffeassociates.translationrecorder.ProjectManager;

/**
 * Created by sarabiaj on 8/26/2016.
 */
public interface UpdateProgressCallback {
    void onCompileCompleted(int[] modifiedIndices);
    void setIsCompiling();
    void setCompilingProgress(int progress);
}
