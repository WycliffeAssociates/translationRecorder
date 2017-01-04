package org.wycliffeassociates.translationrecorder.utilities;

/**
 * Created by sarabiaj on 9/23/2016.
 */

/**
 * Base class for Tasks to run on the TaskFragment
 * Task allows for creation of Runnables that can communicate to the TaskFragment over the RunnableTask interface
 */
public abstract class Task implements RunnableTask {

    public static int FIRST_TASK = 1;

    public OnTaskProgressListener mCallback;
    Long mId;
    int mTag;

    public Task(int taskTag) {
        mTag = taskTag;
    }

    public void setOnTaskProgressListener(OnTaskProgressListener progressListener) {
        mCallback = progressListener;
        mId = new Long(0);
    }

    public void setOnTaskProgressListener(OnTaskProgressListener progressListener, Long id) {
        mCallback = progressListener;
        mId = id;
    }

    @Override
    public void onTaskProgressUpdateDelegator(int progress) {
        if (mCallback != null) {
            mCallback.onTaskProgressUpdate(mId, progress);
        }
    }

    @Override
    public void onTaskCompleteDelegator() {
        if (mCallback != null) {
            mCallback.onTaskComplete(mId);
        }
    }

    @Override
    public void onTaskCancelDelegator() {
        if (mCallback != null) {
            mCallback.onTaskCancel(mId);
        }
    }

    @Override
    public void onTaskErrorDelegator() {
        if (mCallback != null) {
            mCallback.onTaskError(mId);
        }
    }

    public int getTag() {
        return mTag;
    }
}