package wycliffeassociates.recordingapp.utilities;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public interface OnTaskProgressListener {
    void onTaskPreExecute(int id);
    void onTaskProgressUpdate(int id, int progress);
    void onTaskComplete(int id);
    void onTaskCancelled(int id);
    void onTaskPostExecute(int id);
}
