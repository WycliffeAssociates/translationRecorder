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
import wycliffeassociates.recordingapp.FilesPage.Export.FolderExport;
import wycliffeassociates.recordingapp.FilesPage.Export.FtpExport;
import wycliffeassociates.recordingapp.FilesPage.Export.S3Export;
import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class FragmentShareDialog extends DialogFragment implements View.OnClickListener {

    ImageButton sd_card, dir, bluetooth, wifi, amazon, app;
    private String mCurrentDir;
    private AudioFilesAdapter mAdapter;
    private ArrayList<AudioItem> mAudioItemList;
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
        if (view.getId() == R.id.share_sd_card) {
            exportToSdCard();
        } else if (view.getId() == R.id.share_dir){
            exportToDir();
        } else if (view.getId() == R.id.share_bluetooth){
            exportViaBluetooth();
        } else if (view.getId() == R.id.share_wifi){
            exportViaWifi();
        } else if (view.getId() == R.id.share_amazon) {
            exportToAmazon();
        } else if (view.getId() == R.id.share_app){
            exportToApp();
        } else {
            Toast.makeText(getActivity(), "OTHER WAS CLICKED", Toast.LENGTH_LONG).show();
        }
    }

    //=================
    // CUSTOM FUNCTIONS
    //=================

    public void setFilesForExporting(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir){
        mCurrentDir = currentDir;
        mAdapter = adapter;
        mAudioItemList = audioItemList;
    }

    public void exportToSdCard() {
        FolderExport se = new FolderExport(mAudioItemList, mAdapter, mCurrentDir, this);
        se.export();
        //se.cleanUp();
    }

    public void exportToDir() {
        FolderExport de = new FolderExport(mAudioItemList, mAdapter, mCurrentDir, this);
        de.export();
        //de.cleanUp();
    }

    public void exportViaBluetooth() {
        Toast.makeText(getActivity(), "BLUETOOTH WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    public void exportViaWifi() {
        FtpExport fe = new FtpExport(mAudioItemList, mAdapter, mCurrentDir, this);
        fe.export();
        //fe.cleanUp();
        Toast.makeText(getActivity(), "WIFI DIRECT WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    //TODO: Add s3 token
    public void exportToAmazon() {
        Toast.makeText(getActivity(), "AMAZON S3 WAS CLICKED", Toast.LENGTH_LONG).show();
        S3Export s3 = new S3Export(mAudioItemList, mAdapter, mCurrentDir, this);
        //s3.export();
        //s3.cleanUp();
    }

    public void exportToApp() {
        AppExport ae = new AppExport(mAudioItemList, mAdapter, mCurrentDir, this);
        ae.export();
        //ae.cleanUp();
    }


}