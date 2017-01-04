package org.wycliffeassociates.translationrecorder.utilities;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public interface OnTaskProgressListener {
    void onTaskProgressUpdate(Long id, int progress);

    void onTaskComplete(Long id);

    void onTaskCancel(Long id);

    void onTaskError(Long id);
}
