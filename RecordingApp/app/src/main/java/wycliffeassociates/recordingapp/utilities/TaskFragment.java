package wycliffeassociates.recordingapp.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Vector;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public class TaskFragment extends Fragment implements OnTaskProgressListener {

    List<Thread> mThreads = new Vector<>();
    List<ProgressDialog> mProgressDialogs = new Vector<>();
    List<Integer> mProgress = new Vector<>();
    List<String> mProgressTitle = new Vector<>();
    List<String> mProgressMessage = new Vector<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public synchronized void onAttach(Activity activity) {
        super.onAttach(activity);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialogs = new Vector<>();
                for(int i = 0; i < mProgress.size(); i++){
                    ProgressDialog pd = configureProgressDialog(mProgressTitle.get(i), mProgressMessage.get(i), mProgress.get(i));
                    pd.show();
                    mProgressDialogs.add(pd);
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
        mProgress = new Vector<>();
        for (ProgressDialog pd : mProgressDialogs){
            mProgress.add(pd.getProgress());
            pd.dismiss();
            pd = null;
        }
    }

    public synchronized void executeRunnable(Task task, String progressTitle, String progressMessage){
        final int id = mThreads.size();
        task.setOnTaskProgressListener(this, id);
        mThreads.add(new Thread(task));
        ProgressDialog pd = configureProgressDialog(progressTitle, progressMessage, 0);
        mProgress.add(0);
        mProgressDialogs.add(pd);
        mProgressDialogs.get(id).show();
        mProgressTitle.add(progressTitle);
        mProgressMessage.add(progressMessage);
        mThreads.get(id).start();
    }

    private ProgressDialog configureProgressDialog(String title, String message, int progress){
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(100);
        pd.setTitle(title);
        pd.setMessage(message);
        pd.setProgress(progress);
        pd.setCancelable(false);
        pd.show();
        return pd;
    }

    @Override
    public void onTaskPreExecute(int id) {

    }

    @Override
    public synchronized void onTaskProgressUpdate(int id, int progress) {
        mProgressDialogs.get(id).setProgress(progress);
    }

    @Override
    public synchronized void onTaskComplete(int id) {
        mProgressDialogs.get(id).dismiss();
        mProgressDialogs.remove(id);
        mThreads.remove(id);
        mProgressMessage.remove(id);
        mProgressTitle.remove(id);
        mProgress.remove(id);
    }

    @Override
    public void onTaskCancelled(int id) {

    }

    @Override
    public void onTaskPostExecute(int id) {

    }
}
