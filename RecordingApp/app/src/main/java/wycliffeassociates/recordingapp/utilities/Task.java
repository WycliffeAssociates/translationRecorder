package wycliffeassociates.recordingapp.utilities;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public abstract class Task implements RunnableTask {

    public OnTaskProgressListener mCallback;
    int mId = 0;

    public Task(){}

    public void setOnTaskProgressListener(OnTaskProgressListener progressListener){
        mCallback = progressListener;
        mId = 0;
    }

    public void setOnTaskProgressListener(OnTaskProgressListener progressListener, int id){
        mCallback = progressListener;
        mId = id;
    }

    @Override
    public void onTaskPreExecuteDelegator() {
        if(mCallback != null) {
            mCallback.onTaskPreExecute(mId);
        }
    }

    @Override
    public void onTaskProgressUpdateDelegator(int progress) {
        if(mCallback != null) {
            mCallback.onTaskProgressUpdate(mId, progress);
        }
    }

    @Override
    public void onTaskCompleteDelegator() {
        if(mCallback != null) {
            mCallback.onTaskComplete(mId);
        }
    }

    @Override
    public void onTaskCancelledDelegator() {
        if(mCallback != null) {
            mCallback.onTaskCancelled(mId);
        }
    }

    @Override
    public void onTaskPostExecuteDelegator() {
        if(mCallback != null) {
            mCallback.onTaskPostExecute(mId);
        }
    }
}