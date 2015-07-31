package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;


public class FTPActivity extends Activity {

    private EditText FTPServer;
    private EditText Port;
    private EditText UserName;
    private EditText Password;
    private EditText Directory;
    private Button Ok;
    private Button Cancel;

    String filepath, server, password, direc, user;
    int port;
    File uploadFile;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftpdialog);

        Ok = (Button)findViewById(R.id.btnOK);

        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTPServer = (EditText) findViewById(R.id.FTPServer);
                server = FTPServer.getText().toString();
                Port = (EditText) findViewById(R.id.Port);
                String port_string = Port.getText().toString();
                port = Integer.parseInt(port_string);
                UserName = (EditText) findViewById(R.id.UserName);
                user = UserName.getText().toString();
                Password = (EditText) findViewById(R.id.Password);
                password = Password.getText().toString();
                Directory = (EditText) findViewById(R.id.fileDirectory);
                direc = Directory.getText().toString();
                filepath = AudioFiles.exportList.get(0);
                uploadFile = new File(filepath);

                new UploadFile().execute(filepath, server, user, password);

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

    private class UploadFile extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            FTPClient client = new FTPClient();
            try {
                client.setUseEPSVwithIPv4(true);
                client.connect(server, port);
                client.login(user, password);
                client.setFileType(FTP.BINARY_FILE_TYPE);
                boolean result = client.storeFile("naruto.wav",new FileInputStream(uploadFile));
                return result;
            } catch (Exception e) {
                Log.d("FTP", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean sucess) {
            if (sucess)
                System.out.println("Success");
            else
                System.out.println("Error");
        }
    }
}
