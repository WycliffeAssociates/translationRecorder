package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;


/**
 * Created by sarabiaj on 2/19/2016.
 */
public class ExportTaskFragment extends Fragment implements Export.ProgressUpdateCallback {

    Export.ProgressUpdateCallback mProgressUpdateCallback;
    private Export mExp;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mProgressUpdateCallback = (Export.ProgressUpdateCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mProgressUpdateCallback = null;
    }

    public void delegateExport(Export exp) {
        mExp = exp;
        mExp.export();
    }

    @Override
    public void showProgress(boolean mode) {
        mProgressUpdateCallback.showProgress(mode);
    }

    @Override
    public void setProgressTitle(String title) {
        mProgressUpdateCallback.setProgressTitle(title);
    }

    @Override
    public void incrementProgress(int progress) {
        mProgressUpdateCallback.incrementProgress(progress);
    }

    @Override
    public void setUploadProgress(int progress) {
        mProgressUpdateCallback.setUploadProgress(progress);
    }

    @Override
    public void dismissProgress() {
        mProgressUpdateCallback.dismissProgress();
    }

    @Override
    public void setZipping(boolean zipping) {
        mProgressUpdateCallback.setZipping(zipping);
    }

    @Override
    public void setExporting(boolean exporting) {
        mProgressUpdateCallback.setExporting(exporting);
    }

    @Override
    public void setCurrentFile(String currentFile) {
        mProgressUpdateCallback.setCurrentFile(currentFile);
    }
}
