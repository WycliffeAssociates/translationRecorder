package wycliffeassociates.recordingapp.ProjectManager.tasks;

import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.utilities.Task;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class ExportTask extends Task implements Export.ProgressUpdateCallback {

    Export mExport;

    public ExportTask(int taskTag, Export export){
        super(taskTag);
        mExport = export;
    }

    @Override
    public void showProgress(boolean mode) {

    }

    @Override
    public void incrementProgress(int progress) {

    }

    @Override
    public void setUploadProgress(int progress) {
        onTaskProgressUpdateDelegator(progress);
    }

    @Override
    public void dismissProgress() {
        onTaskCompleteDelegator();
    }

    @Override
    public void setZipping(boolean zipping) {

    }

    @Override
    public void setExporting(boolean exporting) {

    }

    @Override
    public void setCurrentFile(String currentFile) {

    }

    @Override
    public void run() {
        mExport.export();
    }
}
