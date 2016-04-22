package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.FilesPage.Export.AppExport;
import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.FilesPage.Export.FolderExport;
import wycliffeassociates.recordingapp.FilesPage.Export.S3Export;
import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class FragmentShareDialog extends DialogFragment implements View.OnClickListener {

    public interface ExportDelegator {
        void delegateExport(Export exp);
    }

    ImageButton sd_card, dir, bluetooth, wifi, amazon, app;
    private String mCurrentDir;
    private AudioFilesAdapter mAdapter;
    private ArrayList<FileItem> mFileItemList;
    ExportDelegator mExportDelegator;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mExportDelegator = (ExportDelegator)activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mExportDelegator = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_options, null);

        sd_card = (ImageButton) view.findViewById(R.id.share_sd_card);
        dir = (ImageButton) view.findViewById(R.id.share_dir);
        amazon = (ImageButton) view.findViewById(R.id.share_amazon);
        app = (ImageButton) view.findViewById(R.id.share_app);

        sd_card.setOnClickListener(this);
        dir.setOnClickListener(this);
        amazon.setOnClickListener(this);
        app.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Export exp;
        switch(view.getId()) {
            //sd and dir should fall through, they both use FolderExport
            case R.id.share_sd_card:
            case R.id.share_dir:
                exp = new FolderExport(mFileItemList, mAdapter, mCurrentDir);
                break;
            case R.id.share_amazon:
                exp = new S3Export(mFileItemList, mAdapter, mCurrentDir);
                break;
            case R.id.share_app:
                exp = new AppExport(mFileItemList, mAdapter, mCurrentDir);
                break;
            default:
                Toast.makeText(getActivity(), "Feature Coming Soon", Toast.LENGTH_LONG).show();
                return;
        }
        //callback using the selected exporter
        mExportDelegator.delegateExport(exp);
        this.dismiss();
    }

    //=================
    // CUSTOM FUNCTIONS
    //=================

    public void setFilesForExporting(ArrayList<FileItem> fileItemList, AudioFilesAdapter adapter, String currentDir){
        mCurrentDir = currentDir;
        mAdapter = adapter;
        mFileItemList = fileItemList;
    }
}