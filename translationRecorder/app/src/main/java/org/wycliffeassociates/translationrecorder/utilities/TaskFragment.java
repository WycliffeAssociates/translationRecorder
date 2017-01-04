package org.wycliffeassociates.translationrecorder.utilities;

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

/**
 * TaskFragment allows for the hosting of an arbitrary number of threads that need to block the UI and
 * report progress. The TaskFragment will persist through an activity orientation change, maintain a progress
 * dialog for each running task, and provide a callback to the activity on completion of the task using the OnTaskComplete
 * interface.
 */
public class TaskFragment extends Fragment implements OnTaskProgressListener {

    //provides a callback for the activity that initiated the task
    public interface OnTaskComplete {
        void onTaskComplete(int taskTag, int resultCode);
    }

    public static int STATUS_OK = 1;
    private static int STATUS_CANCEL = 0;
    public static int STATUS_ERROR = -1;

    volatile AtomicLong mIdGenerator = new AtomicLong(0);

    Handler mHandler;
    OnTaskComplete mActivity;
    volatile HashMap<Long, TaskHolder> mTaskHolder = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public synchronized void onAttach(Activity activity) throws IllegalArgumentException {
        super.onAttach(activity);
        if (activity instanceof OnTaskComplete) {
            mActivity = (OnTaskComplete) activity;
        } else {
            throw new IllegalArgumentException("Activity does not implement OnTaskComplete");
        }
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        } else {
            //Post to the handler so that dialogfragments will be attached to the activity prior to
            //these progress dialogs.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mTaskHolder) {
                        Set<Long> keys = mTaskHolder.keySet();
                        for (final Long id : keys) {
                            //duplicate code from configure progress dialog, however, it appears to be necessary for the dialog to display and set progress properly
                            TaskHolder task = mTaskHolder.get(id);
                            task.dismissDialog();
                            ProgressDialog pd = configureProgressDialog(task.mTitle, task.mMessage, task.getProgress(), task.isIndeterminate());
                            task.setProgressDialog(pd);
                            task.showProgress();
                            task.getProgressDialog().setProgress(task.getProgress());
                        }
                    }
                }
            });
        }

    }

    /**
     * Dismisses all progress dialogs and stores their current progress in order to recreate a new
     * dialog if a new activity is attached. We can trust that this (being a lifecycle method) will run
     * on the UI thread and therefore the activity reference will not be null, so posting to the UI Thread
     * here is safe.
     */
    @Override
    public synchronized void onDetach() {
        super.onDetach();
        ((Activity) mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    Set<Long> keys = mTaskHolder.keySet();
                    for (final Long id : keys) {
                        //duplicate code from configure progress dialog, however, it appears to be necessary for the dialog to display and set progress properly
                        mTaskHolder.get(id).dismissDialog();
                    }
                }
            }
        });
    }

    /**
     * Method to post a thread to run on the TaskFragment.
     *
     * @param task            An Runnable that implements the interfaces to communicate progress to the TaskFragment
     * @param progressTitle   The title of the progress dialog to be created for the provided task
     * @param progressMessage The message of the progress dialog to be created for the provided task
     * @param indeterminate   Whether the progress dialog should display progress or is indeterminate for the provided task
     */
    public synchronized void executeRunnable(final Task task, final String progressTitle, final String progressMessage, final boolean indeterminate) {
        synchronized (mTaskHolder) {
            final Long id = mIdGenerator.incrementAndGet();
            task.setOnTaskProgressListener(TaskFragment.this, id);
            final TaskHolder th = new TaskHolder(task, progressTitle, progressMessage);
            mTaskHolder.put(id, th);
            ProgressDialog pd = configureProgressDialog(progressTitle, progressMessage, 0, indeterminate);
            th.setProgressDialog(pd);
            th.setIsIndeterminate(indeterminate);
            th.showProgress();
            th.startTask();
        }
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

    private synchronized void endTask(final Long id, final int status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mTaskHolder) {
                    TaskHolder task = mTaskHolder.get(id);
                    task.dismissDialog();
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
    public void onTaskCancel(Long id) {
        endTask(id, STATUS_CANCEL);
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

        TaskHolder(Task task, String title, String message) {
            mTask = task;
            mTitle = title;
            mMessage = message;
            mThread = new Thread(task);
            mProgress = 0;
        }

        public int getTaskTag() {
            return mTask.getTag();
        }

        public void setProgressDialog(ProgressDialog pd) {
            mPd = pd;
        }

        public ProgressDialog getProgressDialog() {
            return mPd;
        }

        public int getProgress() {
            return mProgress;
        }

        public void setProgress(int progress) {
            mProgress = progress;
        }

        public void setIsIndeterminate(boolean indeterminate) {
            mIsIndeterminate = indeterminate;
        }

        public boolean isIndeterminate() {
            return mIsIndeterminate;
        }

        public void startTask() {
            mThread.start();
        }

        public void dismissDialog() {
            if (mPd != null && mPd.isShowing()) {
                mPd.dismiss();
            }
        }

        public void showProgress() {
            if (mPd != null && !mPd.isShowing()) {
                mPd.show();
            }
        }
    }

}
