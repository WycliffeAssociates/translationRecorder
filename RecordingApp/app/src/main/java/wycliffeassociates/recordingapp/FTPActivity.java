package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class FTPActivity extends Activity {
    private EditText FTPServer;
    private EditText Port;
    private EditText UserName;
    private EditText Password;
    private EditText Directory;
    private Button Ok;
    private Button Cancel;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftpdialog);

        Ok = (Button)findViewById(R.id.btnOK);

        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                FTPServer = (EditText) findViewById(R.id.FTPServer);
                String server = FTPServer.getText().toString();
                Port = (EditText) findViewById(R.id.FTPServer);
                int port = Integer.getInteger(Port.getText().toString());
                UserName = (EditText) findViewById(R.id.UserName);
                String user = UserName.getText().toString();
                Password = (EditText) findViewById(R.id.Password);
                String password = Password.getText().toString();
                Directory = (EditText) findViewById(R.id.fileDirectory);
                String direc = Directory.getText().toString();

                String filepath = AudioFiles.exportList.get(0);
                File uploadFile = new File(filepath);
                UploadTask task = new UploadTask(server, port, user, password, direc, uploadFile);
                try {
                    task.doInBackground();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Cancel = (Button)findViewById(R.id.btnCANCEL);

        Cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }


}
