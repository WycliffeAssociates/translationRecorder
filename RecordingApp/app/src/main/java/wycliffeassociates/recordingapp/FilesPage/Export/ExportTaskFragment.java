package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import wycliffeassociates.recordingapp.FilesPage.FragmentShareDialog;


/**
 * Created by sarabiaj on 2/19/2016.
 */
public class ExportTaskFragment extends Fragment implements FragmentShareDialog.Exporter, Export.UpdateProgress {

    private Activity mActivity;
    Export.UpdateProgress mProgressUpdateCallback;
    private Export mExp;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mActivity = activity;
        mProgressUpdateCallback = (Export.UpdateProgress)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mActivity = null;
        mProgressUpdateCallback = null;
    }

    @Override
    public void onExport(Export exp) {
        mExp = exp;
        mExp.export();
    }

    @Override
    public void onDestroy(){
        System.out.println("fragment was destroyed");
    }

    @Override
    public void showProgress(boolean mode) {
        mProgressUpdateCallback.showProgress(mode);
    }

    @Override
    public void incrementProgress(int progress) {
        mProgressUpdateCallback.incrementProgress(progress);
    }

    @Override
    public void dismissProgress() {
        mProgressUpdateCallback.dismissProgress();
    }

    @Override
    public void setZipping(boolean zipping){
        mProgressUpdateCallback.setZipping(zipping);
    }

    @Override
    public void setExporting(boolean exporting){
        mProgressUpdateCallback.setExporting(exporting);
    }
}
