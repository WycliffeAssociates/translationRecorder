package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.model.Language;

/**
 *
 * The settings page -- for all persistent options/information.
 *
 */
public class Settings extends Activity {
    private Button hardReset;
    private ImageButton setSaveDirectory, setFtp;
    private String sampleName;
    private TextView displayFileName, showSaveDirectory;
    private EditText tReset,setBookCode;
    AutoCompleteTextView setLangCode;

    private Context c;

    final int SET_SAVE_DIR = 21;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings2);

        c = this;
        final PreferencesManager pref = new PreferencesManager(c);

        displayFileName = (TextView)findViewById(R.id.defaultFileName);
        showSaveDirectory = (TextView)findViewById(R.id.showSaveDirectory);
        tReset = (EditText)findViewById(R.id.tReset);

        printFileName(pref);
        printSaveDirectory(pref);
        printCounter(pref);

        //need to get an adapter for autocompletion eventually. Functional now
        setLangCode = (AutoCompleteTextView)findViewById(R.id.setLangCode);
        //setLangCode.setText(pref.getPreferences("targetLanguage").toString());


        //SELECT
        String tableName = "langnames";
        SQLiteDatabase audiorecorder = openOrCreateDatabase(tableName, MODE_PRIVATE, null);

        String querySelect = "SELECT * FROM " + tableName;
        String queryCount = "SELECT COUNT(*) FROM " + tableName;

        //unsynchronized
        Cursor cursor = audiorecorder.rawQuery(querySelect, new String[]{});
        //Cursor cursor = audiorecorder.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        //        KEY_MOVES,KEY_TIME}, null, null, null, null, KEY_MOVES + " ASC");

        System.out.println("==========");
        //System.out.println("Spen : " + cursor.getCount());

               /* if(cursor!=null) {
                    cursor.moveToFirst();
                    while(!cursor.isLast()) {
                        System.out.println(value.getName() + "," + value.getCode() + "," + value.getCountryCode()
                                + "," + value.getLanguageDirection() + "," + i + "\n")
                        cursor.moveToNext();
                    }
                }*/
        ArrayList<Language> languageList = new ArrayList<Language>();
        String[] listHolder;
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                        /*System.out.println(cursor.getString(0));
                        System.out.println(cursor.getInt(1));*/
                //create language

                Boolean gw = false;
                if (cursor.getInt(0) == 0) {
                    gw = true;
                }

                String ld = cursor.getString(1);
                String lc = cursor.getString(2);
                String ln = cursor.getString(3);
                String cc = cursor.getString(4); //temp
                int pk = cursor.getInt(5);

                Language temp = new Language(gw, ld, lc, ln, cc, pk);
                languageList.add(temp);
                        /*System.out.println(cursor.getInt(0) + ", " + cursor.getString(1) + ", " +
                                cursor.getString(2) + ", " + cursor.getString(3) + ", " +
                                cursor.getString(4) + ", " + cursor.getInt(5));*/
                cursor.moveToNext();
            }
        }

        audiorecorder.close();

        listHolder = new String[languageList.size()];
        for (int a = 0; a < listHolder.length; a++) {
            listHolder[a] = (languageList.get(a)).getCode() + " - " +
                    (languageList.get(a)).getName();
            //System.out.println(listHolder[a]);
        }

        //CREATE
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listHolder);
        setLangCode.setAdapter(adapter);
        setLangCode.setText(pref.getPreferences("targetLanguage").toString());
        setLangCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] temp = s.toString().split(" - ");

                String newVal = temp[0];
                newVal = newVal.replaceAll("![A-Za-z0-9]", "");
                pref.setPreferences("targetLanguage", newVal);
                updateFileName(pref);
            }
        });

        setBookCode = (EditText)findViewById(R.id.setBookCode);
        setBookCode.setText(pref.getPreferences("book").toString());
        setBookCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newVal = s.toString();
                newVal = newVal.replaceAll("![A-Za-z0-9]", "");
                pref.setPreferences("book", newVal);
                updateFileName(pref);
            }
        });

        tReset = (EditText)findViewById(R.id.tReset);
        tReset.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newVal = s.toString();
                int counter = (int)pref.getPreferences("fileCounter");
                try {
                    counter = Integer.parseInt(newVal);
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
                }
                pref.setPreferences("fileCounter", counter);
                printFileName(pref);
            }
        });

        hardReset = (Button)findViewById(R.id.btnRestoreDefault);
        hardReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.resetPreferences("all");

                printSaveDirectory(pref);
                printFileName(pref);
                printCounter(pref);
            }
        });

        //todo: use export files option rather than open document tree (API level 21)
        setSaveDirectory = (ImageButton)findViewById(R.id.setSaveDirectory);
        setSaveDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = (String) pref.getPreferences("fileDirectory");

                Intent intent = new Intent();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                     intent.setAction(intent.ACTION_OPEN_DOCUMENT_TREE);
                else{
                    
                }
                startActivityForResult(intent, SET_SAVE_DIR);
            }
        });

        setFtp = (ImageButton)findViewById(R.id.setFtp);
        setFtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //toDo: edit theme for gravity & edittext items & get preferences to show
                final Dialog ftp = new Dialog(c,android.R.style.Theme_Translucent_NoTitleBar);
                ftp.setContentView(R.layout.ftp_dialog);

               final EditText server = (EditText)ftp.findViewById(R.id.ftpServer);
                server.setText(pref.getPreferences("ftpServer").toString());
               final EditText userName = (EditText)ftp.findViewById(R.id.userName);
                System.out.println("ABI: username = " + pref.getPreferences("ftpUserName").toString());
               final EditText port = (EditText)ftp.findViewById(R.id.ftpPort);
                port.setText(pref.getPreferences("ftpPort").toString());
               final EditText password = (EditText)ftp.findViewById(R.id.ftpPassword);
                password.setEnabled(false);
               final EditText secureftp = (EditText)ftp.findViewById(R.id.secureFtp);
                secureftp.setText(pref.getPreferences("ftp").toString());
               final EditText directory = (EditText)ftp.findViewById(R.id.ftpDirectory);
                directory.setText(pref.getPreferences("ftpDirectory").toString());

                ImageButton ok = (ImageButton)ftp.findViewById(R.id.ftpOk);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        pref.setPreferences("ftpServer", server.getText().toString());
                        pref.setPreferences("ftpUserName", userName.getText().toString());
                        pref.setPreferences("ftpPort", port.getText().toString());
                        pref.setPreferences("ftp", secureftp.getText().toString());
                        pref.setPreferences("ftpDirectory", directory.getText().toString());
                        ftp.hide();
                    }
                });

                ftp.show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SET_SAVE_DIR) {
                currentUri = resultData.getData();
                File temp = new File(currentUri.getPath());
                PreferencesManager pref = new PreferencesManager(this);

                //:( -- get file path
                pref.setPreferences("fileDirectory", temp.getAbsolutePath().toString());
                printSaveDirectory(pref);
            }
        }
    }

    /**
     * Prints the file name to the appropriate textview
     * @param pref the preference manager that holds the save file name
     */
    private void printFileName(PreferencesManager pref){
        sampleName = (String) pref.getPreferences("fileName");
        displayFileName.setText(sampleName+ "-" + pref.getPreferences("fileCounter"));
    }

    /**
     * Prints the save directory to the appropriate textview
     * @param pref The preference manager that holds the save directory
     */
    private void printSaveDirectory(PreferencesManager pref){
        String saveDirectory = (String) pref.getPreferences("fileDirectory");
        showSaveDirectory.setText(saveDirectory);
    }

    /**
     * Prints the current counter on the counter button
     * @param pref the preference manager that holds the current counter
     */
    private void printCounter(PreferencesManager pref){
        String counter = pref.getPreferences("fileCounter").toString();
        tReset.setText(counter);
    }

    /**
     * Updates the fileName based on the
     * @param pref
     */
    private void updateFileName(PreferencesManager pref){
            String name = (String)pref.getPreferences("targetLanguage") + "-" +
                    (String)pref.getPreferences("book");
            pref.setPreferences("fileName", name);
            printFileName(pref);
    }
}
