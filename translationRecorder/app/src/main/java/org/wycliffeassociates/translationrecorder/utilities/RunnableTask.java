package org.wycliffeassociates.translationrecorder.utilities;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public interface RunnableTask extends Runnable {
    void onTaskProgressUpdateDelegator(int progress);

    void onTaskCompleteDelegator();

    void onTaskCancelDelegator();

    void onTaskErrorDelegator();
}
