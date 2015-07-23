package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class MainMenuListener extends Activity{

    private ImageButton btnRecord;
    private ImageButton btnFiles;
    private ImageButton btnSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnRecord = (ImageButton) findViewById(R.id.record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CanvasScreen.class);
                startActivityForResult(intent, 0);
            }
        });

        btnFiles = (ImageButton) findViewById(R.id.files);
        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AudioFiles.class);
                startActivityForResult(intent, 0);
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(v.getContext(), Settings.class);
                startActivityForResult(intent, 0);*/

                ArrayList<Uri> audioUris = new ArrayList<Uri>();
                audioUris.add(Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/test.wav"));
                audioUris.add(Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/thething.wav"));
                audioUris.add(Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/spen.wav"));

                Uri audioUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder/test.wav");

                ArrayList<String> stringL = new ArrayList<String>();
                stringL.add("yo");
                stringL.add("yo2");
                stringL.add("yo3");

                Intent sendIntent = new Intent();
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.setAction(Intent.ACTION_SEND);
                //sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                //sendIntent.setAction(Intent.EXTRA_ALLOW_MULTIPLE);
                //sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
                //sendIntent.putStringArrayListExtra(Intent.EXTRA_ALLOW_MULTIPLE, stringL);

                sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);
                //sendIntent.putParcelableArrayListExtra(Intent.EXTRA_TEXT, audioUris);
                //sendIntent.putStringArrayListExtra("test", stringL);

                //sendIntent.putExtra(Intent.EXTRA_TEXT, stringL);

                //sendIntent.setType("text/plain");
                sendIntent.setType("audio/*");
                startActivity(Intent.createChooser(sendIntent, "Export Audio"));
                //startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.diag_export)));

            }
        });
    }



}
