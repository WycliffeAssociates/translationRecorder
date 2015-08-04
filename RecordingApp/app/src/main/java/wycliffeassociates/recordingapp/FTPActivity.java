package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class FTPActivity extends Activity {

    private EditText FTPServer;
    private EditText Port;
    private EditText UserName;
    private EditText Password;
    private EditText Directory;
    private ImageButton Ok;
    CopyStreamAdapter streamListener;

    PreferencesManager pref;

    String filepath, server, password, direc, user, destinationfilename;
    int port;
    File uploadFile;
    int increment = 0;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftpdialog);
        pref = new PreferencesManager(this);

        Ok = (ImageButton)findViewById(R.id.btnOkay);

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
                new UploadFile().execute();
            }
        });

    }

    private class UploadFile extends AsyncTask<String, Integer, Boolean> {
        public ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(FTPActivity.this);
            progressDialog.setMessage("File(s) uploading to Server");
            progressDialog.setIndeterminate(false);
            progressDialog.setTitle("UPLOADING FILE(S)");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(AudioFiles.exportList.size());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            FTPSClient client = new FTPSClient();
            boolean result = false;
            try {
                client.setUseEPSVwithIPv4(true);
                client.connect(server, port); // connect to server
                client.login(user, password); // login to server
                client.setFileType(FTP.BINARY_FILE_TYPE); // set file type
                for (int i = 0; i < AudioFiles.exportList.size(); i ++) {
                    filepath = AudioFiles.exportList.get(i);
                    uploadFile = new File(filepath);
                    destinationfilename = filepath.replace((String) pref.getPreferences("fileDirectory") + "/", "");
                    result = client.storeFile(destinationfilename, new FileInputStream(uploadFile)); // store file on server
                    publishProgress(i+1);
                    System.out.println("Status Value-->" + result);
                }
                client.logout(); // logout of the server
                client.disconnect(); // disconnect from the server
                return result;
            } catch (Exception e) {
                Log.d("FTP", e.toString());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean sucess) {
            progressDialog.setProgress(AudioFiles.exportList.size());
            progressDialog.dismiss();
            if (sucess) {
                System.out.println("Success");
            }
            else {
                System.out.println("Error");
            }
            finish();
        }
    }
}
