package wycliffeassociates.recordingapp.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public class TaskFragment extends Fragment implements OnTaskProgressListener {

    public interface OnTaskComplete {
        void onTaskComplete(int taskTag, int resultCode);
    }

    public static int STATUS_OK = 1;
    private static int STATUS_CANCELLED = 0;
    public static int STATUS_ERROR = -1;

    AtomicLong mIdGenerator = new AtomicLong(0);

    Handler mHandler;
    OnTaskComplete mActivity;
    HashMap<Long, TaskHolder> mTaskHolder = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) throws IllegalArgumentException {
        super.onAttach(activity);
        if (activity instanceof OnTaskComplete) {
            mActivity = (OnTaskComplete) activity;
        } else {
            throw new IllegalArgumentException("Activity does not implement OnTaskProgressListener");
        }
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mTaskHolder) {
                        Set<Long> keys = mTaskHolder.keySet();
                        for (final Long id : keys) {
                            //duplicate code from configure progress dialog, however, it appears to be necessary for the dialog to display and set progress properly
                            TaskHolder task = mTaskHolder.get(id);
                            ProgressDialog pd = configureProgressDialog(task.mTitle, task.mMessage, task.getProgress(), task.isIndeterminate());
                            task.setProgress(task.getProgress());
                            pd.show();
                            task.setProgressDialog(pd);
                        }
                    }
                }
            });
        }

    }

    /**
     * Dismisses all progress dialogs and stores their current progress in order to recreate a new
     * dialog if a new activity is attached
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    Set<Long> keys = mTaskHolder.keySet();
                    for (final Long id : keys) {
                        TaskHolder task = mTaskHolder.get(id);
                        ProgressDialog pd = task.getProgressDialog();
                        task.setProgress(pd.getProgress());
                        pd.dismiss();
                        pd = null;
                    }
                }
            }
        });
    }

    public void executeRunnable(final Task task, final String progressTitle, final String progressMessage, final boolean indeterminate) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    final Long id = mIdGenerator.incrementAndGet();
                    task.setOnTaskProgressListener(TaskFragment.this, id);
                    final TaskHolder th = new TaskHolder(task, progressTitle, progressMessage);
                    mTaskHolder.put(id, th);
                    ProgressDialog pd = configureProgressDialog(progressTitle, progressMessage, 0, indeterminate);
                    pd.show();
                    th.setProgressDialog(pd);
                    th.setIsIndeterminate(indeterminate);
                    th.startTask();
                }
            }
        });
    }

    private ProgressDialog configureProgressDialog(String title, String message, int progress, boolean indeterminate) {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(indeterminate);
        if (!indeterminate) {
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setProgress(progress);
            pd.setMax(100);
        }
        pd.setTitle(title);
        pd.setMessage(message);
        pd.setCancelable(false);
        return pd;
    }


    @Override
    public void onTaskProgressUpdate(final Long id, final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    TaskHolder task = mTaskHolder.get(id);
                    task.getProgressDialog().setProgress(progress);
                }
            }
        });
    }

    private void endTask(final Long id, final int status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    TaskHolder task = mTaskHolder.get(id);
                    task.getProgressDialog().dismiss();
                    mActivity.onTaskComplete(task.getTaskTag(), status);
                    mTaskHolder.remove(id);
                }
            }
        });
    }

    @Override
    public void onTaskComplete(final Long id) {
        endTask(id, STATUS_OK);
    }

    @Override
    public void onTaskCancelled(Long id) {
        endTask(id, STATUS_CANCELLED);
    }

    @Override
    public void onTaskError(Long id) {
        endTask(id, STATUS_ERROR);
    }

    private class TaskHolder {
        Task mTask;
        ProgressDialog mPd;
        int mProgress;
        String mTitle;
        String mMessage;
        Thread mThread;
        boolean mIsIndeterminate = true;

        TaskHolder(Task task, String title, String message){
            mTask = task;
            mTitle = title;
            mMessage = message;
            mThread = new Thread(task);
            mProgress = 0;
        }

        public int getTaskTag(){
            return mTask.getTag();
        }

        public void setProgressDialog(ProgressDialog pd){
            mPd = pd;
        }

        public ProgressDialog getProgressDialog(){
            return mPd;
        }

        public int getProgress(){
            return mProgress;
        }

        public void setProgress(int progress){
            mProgress = progress;
        }

        public void setIsIndeterminate(boolean indeterminate){
            mIsIndeterminate = indeterminate;
        }

        public boolean isIndeterminate(){
            return mIsIndeterminate;
        }

        public void startTask(){
            mThread.start();
        }
    }

}
