package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;
import wycliffeassociates.recordingapp.FilesPage.Export.AppExport;
import wycliffeassociates.recordingapp.FilesPage.Export.Export;
import wycliffeassociates.recordingapp.FilesPage.Export.ExportTaskFragment;
import wycliffeassociates.recordingapp.FilesPage.Export.FolderExport;
import wycliffeassociates.recordingapp.FilesPage.Export.FtpExport;
import wycliffeassociates.recordingapp.FilesPage.Export.S3Export;
import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class FragmentShareDialog extends DialogFragment implements View.OnClickListener {

    public interface Exporter{
        void onExport(Export exp);
    }

    ImageButton sd_card, dir, bluetooth, wifi, amazon, app;
    private String mCurrentDir;
    private AudioFilesAdapter mAdapter;
    private ArrayList<AudioItem> mAudioItemList;
    Exporter mExporterCallback;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mExporterCallback = (Exporter)activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mExporterCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_options, null);

        sd_card = (ImageButton) view.findViewById(R.id.share_sd_card);
        dir = (ImageButton) view.findViewById(R.id.share_dir);
        bluetooth = (ImageButton) view.findViewById(R.id.share_bluetooth);
        wifi = (ImageButton) view.findViewById(R.id.share_wifi);
        amazon = (ImageButton) view.findViewById(R.id.share_amazon);
        app = (ImageButton) view.findViewById(R.id.share_app);

        sd_card.setOnClickListener(this);
        dir.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        wifi.setOnClickListener(this);
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
                exp = new FolderExport(mAudioItemList, mAdapter, mCurrentDir);
                break;
            case R.id.share_amazon:
                exp = new S3Export(mAudioItemList, mAdapter, mCurrentDir);
                break;
            case R.id.share_app:
                exp = new AppExport(mAudioItemList, mAdapter, mCurrentDir);
                break;
            //Fall through to default for unimplemented exporting options
            case R.id.share_bluetooth:
            case R.id.share_wifi:
            default:
                Toast.makeText(getActivity(), "Feature Coming Soon", Toast.LENGTH_LONG).show();
                return;
        }
        //callback using the selected exporter
        mExporterCallback.onExport(exp);
        this.dismiss();
    }

    //=================
    // CUSTOM FUNCTIONS
    //=================

    public void setFilesForExporting(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir){
        mCurrentDir = currentDir;
        mAdapter = adapter;
        mAudioItemList = audioItemList;
    }

//    public void exportViaWifi() {
//        FtpExport fe = new FtpExport(mAudioItemList, mAdapter, mCurrentDir, this);
//        fe.export();
//        //fe.cleanUp();
//        Toast.makeText(getActivity(), "WIFI DIRECT WAS CLICKED", Toast.LENGTH_LONG).show();
//        // Insert code to export here
//    }
}