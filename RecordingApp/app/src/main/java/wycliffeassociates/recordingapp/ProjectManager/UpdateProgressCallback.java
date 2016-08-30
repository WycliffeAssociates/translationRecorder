package wycliffeassociates.recordingapp.ProjectManager;

import android.os.Handler;

import java.util.List;

/**
 * Created by sarabiaj on 8/26/2016.
 */
public interface UpdateProgressCallback {
    void onCompileCompleted(int[] modifiedIndices);
    void setIsCompiling();
    void setCompilingProgress(int progress);
}
