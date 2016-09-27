package wycliffeassociates.recordingapp.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public class TaskFragment extends Fragment implements OnTaskProgressListener {

    public interface OnTaskComplete{
        void onTaskComplete(int taskTag, int resultCode);
    }

    public static int STATUS_OK = 1;
    private static int STATUS_CANCELLED = 0;
    public static int STATUS_ERROR = -1;

    AtomicLong idGenerator = new AtomicLong(0);

    Handler mHandler;
    OnTaskComplete mActivity;
    ConcurrentHashMap<Long, Thread> mThreads = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, ProgressDialog> mProgressDialogs = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, Integer> mProgress = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, String> mProgressTitle = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, String> mProgressMessage = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, Task> mTasks = new ConcurrentHashMap<>();
    //ConcurrentHashMap<Long, TaskHolder> mTaskHolder = new ConcurrentHashMap<>();

//    private class TaskHolder {
//        Task mTask;
//        ProgressDialog mPd;
//        int mProgress;
//        String mTitle;
//        String mMessage;
//
//        TaskHolder(Task task, String title, String message){
//            mTask = task;
//            mTitle = title;
//            mMessage = message;
//        }
//
//        public int getTaskTag(){
//            return mTask.getTag();
//        }
//
//        public void setProgressDialog(ProgressDialog pd){
//            mPd = pd;
//        }
//
//        public int getProgress(){
//            return mProgress;
//        }
//
//        public void setProgress(int progress){
//            mProgress = progress;
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public synchronized void onAttach(Activity activity) throws IllegalArgumentException{
        super.onAttach(activity);
        if (activity instanceof OnTaskComplete){
            mActivity = (OnTaskComplete) activity;
        } else {
            throw new IllegalArgumentException("Activity does not implement OnTaskProgressListener");
        }
        if(mHandler == null){
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialogs = new ConcurrentHashMap<>();
                Set<Long> keys = mProgress.keySet();
                for (final Long id : keys) {
                    final ProgressDialog pd = configureProgressDialog(mProgressTitle.get(id), mProgressMessage.get(id), mProgress.get(id));
                    //duplicate code from configure progress dialog, however, it appears to be necessary for the dialog to display and set progress properly
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pd.setProgress(mProgress.get(id));
                            pd.show();
                        }
                    });
                    mProgressDialogs.put(id, pd);
                }
            }
        });

    }

    /**
     * Dismisses all progress dialogs and stores their current progress in order to recreate a new
     * dialog if a new activity is attached
     */
    @Override
    public synchronized void onDetach() {
        super.onDetach();
        mProgress = new ConcurrentHashMap<>();
        Set<Long> keys = mProgressDialogs.keySet();
        for (final Long id : keys) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog pd = mProgressDialogs.get(id);
                    mProgress.put(id, pd.getProgress());
                    pd.dismiss();
                    pd = null;
                }
            });
        }
    }

    public synchronized void executeRunnable(Task task, final String progressTitle, final String progressMessage) {
        final Long id = idGenerator.incrementAndGet();
        task.setOnTaskProgressListener(this, id);
        mThreads.put(id, new Thread(task));
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ProgressDialog pd = configureProgressDialog(progressTitle, progressMessage, 0);
                mProgress.put(id, 0);
                mProgressDialogs.put(id, pd);
                mProgressDialogs.get(id).show();
            }
        });
        mProgressTitle.put(id, progressTitle);
        mProgressMessage.put(id, progressMessage);
        mTasks.put(id, task);
        mThreads.get(id).start();
    }

    private ProgressDialog configureProgressDialog(String title, String message, int progress) {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(100);
        pd.setTitle(title);
        pd.setMessage(message);
        pd.setProgress(progress);
        pd.setCancelable(false);
        return pd;
    }


    @Override
    public synchronized void onTaskProgressUpdate(final Long id, final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialogs.get(id).setProgress(progress);
            }
        });
    }

    private synchronized void endTask(final Long id, final int status){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialogs.get(id).dismiss();
                mProgressDialogs.remove(id);
                mActivity.onTaskComplete(mTasks.get(id).getTag(), status);
                mTasks.remove(id);
            }
        });
        mThreads.remove(id);
        mProgressMessage.remove(id);
        mProgressTitle.remove(id);
        mProgress.remove(id);
    }

    @Override
    public synchronized void onTaskComplete(final Long id) {
        endTask(id, STATUS_OK);
    }

    @Override
    public synchronized void onTaskCancelled(Long id) {
        endTask(id, STATUS_CANCELLED);
    }

    @Override
    public synchronized void onTaskError(Long id) {
        endTask(id, STATUS_ERROR);
    }
}
