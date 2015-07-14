package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainMenuListener extends Activity{

    private ImageButton btnRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnRecord = (ImageButton) findViewById(R.id.record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("YO");
                Intent intent = new Intent(v.getContext(), Record.class);
                startActivityForResult(intent, 0);
            }
        });
    }



}
