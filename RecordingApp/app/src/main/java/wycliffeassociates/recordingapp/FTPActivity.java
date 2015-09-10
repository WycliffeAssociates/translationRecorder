package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.*;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
    private EditText SecureFTP;
    private ImageButton Ok;

    PreferencesManager pref;

    String filepath, server, password, direc, user, destinationfilename;
    int port;
    File uploadFile;
    Context c;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftpdialog);
        c = this;
        pref = new PreferencesManager(this);

        FTPServer = (EditText) findViewById(R.id.FTPServer);
        FTPServer.setText(pref.getPreferences("ftpServer").toString());
        Port = (EditText) findViewById(R.id.Port);
        Port.setText(pref.getPreferences("ftpPort").toString());
        UserName = (EditText) findViewById(R.id.UserName);
        UserName.setText(pref.getPreferences("ftpUserName").toString());
        Password = (EditText) findViewById(R.id.Password);
        Directory = (EditText) findViewById(R.id.fileDirectory);
        Directory.setText(pref.getPreferences("ftpDirectory").toString());
        SecureFTP = (EditText)findViewById(R.id.editText10);
        SecureFTP.setText(pref.getPreferences("ftp").toString());

        Ok = (ImageButton)findViewById(R.id.btnOkay);

        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    server = FTPServer.getText().toString();
                    String port_string = Port.getText().toString();
                    port = Integer.parseInt(port_string);
                    user = UserName.getText().toString();
                    password = Password.getText().toString();
                    direc = Directory.getText().toString();
                    new UploadFile().execute("");
                }
                catch(Exception e){
                    Toast.makeText(c,"Failed, please check parameters",Toast.LENGTH_SHORT).show();
                }
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
            System.out.println("Here in do background");
            FTPClient client = new FTPClient();
            boolean result = false;
            try {
                client.setUseEPSVwithIPv4(true);
                client.connect(server, port); // connect to server
                client.login(user, password); // login to server
                if (client.isConnected()){
                    System.out.println("Connection Established Successfully");
                }
                else {
                    System.out.println("Could not establish a connection to the server");
                }
                client.setFileType(FTP.BINARY_FILE_TYPE); // set file type
                for (int i = 0; i < AudioFiles.exportList.size(); i ++) {
                    filepath = AudioFiles.exportList.get(i);
                    uploadFile = new File(filepath);
                    //(String) (pref.getPreferences("appName") + "/" + pref.getPreferences("deviceUUID") + "/") +
                    destinationfilename = filepath.replace((String) pref.getPreferences("fileDirectory") + "/", "");
                    FileInputStream file = new FileInputStream(uploadFile);
                    if(!uploadFile.exists()) {
                        System.out.println("Error File does not exist");
                    }
                    result = client.storeFile(destinationfilename, file); // store file on server
                    publishProgress(i+1);
                    System.out.println("Status Value-->" + result);
                    System.out.println(client.getReplyCode() + "Is the server reply code");
                }
                client.logout(); // logout of the server
                client.disconnect(); // disconnect from the server
                return result;
            } catch (Exception e) {
                System.out.println("Exception " + e);
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
