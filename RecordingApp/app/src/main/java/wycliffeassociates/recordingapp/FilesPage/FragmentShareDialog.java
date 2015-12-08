package wycliffeassociates.recordingapp.FilesPage;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/8/2015.
 */
public class FragmentShareDialog extends DialogFragment implements View.OnClickListener {

    ImageButton sd_card, dir, bluetooth, wifi, amazon, other;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_options, null);

        sd_card = (ImageButton) view.findViewById(R.id.share_sd_card);
        dir = (ImageButton) view.findViewById(R.id.share_dir);
        bluetooth = (ImageButton) view.findViewById(R.id.share_bluetooth);
        wifi = (ImageButton) view.findViewById(R.id.share_wifi);
        amazon = (ImageButton) view.findViewById(R.id.share_amazon);
        other = (ImageButton) view.findViewById(R.id.share_other);

        sd_card.setOnClickListener(this);
        dir.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        wifi.setOnClickListener(this);
        amazon.setOnClickListener(this);
        other.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.share_sd_card) {
            exportToSDCard();
        }
        else if (view.getId() == R.id.share_dir){
            exportToDir();
        }
        else if (view.getId() == R.id.share_bluetooth){
            exportViaBluetooth();
        }
        else if (view.getId() == R.id.share_wifi){
            exportViaWifi();
        }
        else if (view.getId() == R.id.share_amazon){
            exportToAmazon();
        }
        else {
            Toast.makeText(getActivity(), "OTHER WAS CLICKED", Toast.LENGTH_LONG).show();
        }
    }

    public void exportToSDCard() {
        Toast.makeText(getActivity(), "SD CARD WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    public void exportToDir() {
        Toast.makeText(getActivity(), "DIRECTORY WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    public void exportViaBluetooth() {
        Toast.makeText(getActivity(), "BLUETOOTH WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    public void exportViaWifi() {
        Toast.makeText(getActivity(), "WIFI DIRECT WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }

    public void exportToAmazon() {
        Toast.makeText(getActivity(), "AMAOZON S3 WAS CLICKED", Toast.LENGTH_LONG).show();
        // Insert code to export here
    }


}