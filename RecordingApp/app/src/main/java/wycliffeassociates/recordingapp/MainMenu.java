package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainMenu extends Activity{

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
                Intent intent = new Intent(v.getContext(), RecordingScreen.class);
                startActivityForResult(intent, 0);
            }
        });

        btnFiles = (ImageButton) findViewById(R.id.files);
        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AudioFiles.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Settings.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
            }
        });
    }



}
