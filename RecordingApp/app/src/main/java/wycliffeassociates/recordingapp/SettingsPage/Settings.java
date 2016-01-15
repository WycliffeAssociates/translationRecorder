package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.FilesPage.ExportFiles;
import wycliffeassociates.recordingapp.MainMenu;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.SettingsPage.connectivity.LanguageNamesRequest;

/**
 *
 * The settings page -- for all persistent options/information.
 *
 */
public class Settings extends Activity {
    private Context c;
    private Button hardReset;
    private String sampleName;
    private TextView displayFileName, showSaveDirectory, action_bar_title;
    private EditText tReset, chapterReset;

    MyAutoCompleteTextView setLangCode,setBookCode;

    /**
     * Request code for Android 5.0+
     */
    final int SET_SAVE_DIR = 21;

    /**
     * Request code for Android <5.0
     */
    final int SET_SAVE_DIR2 = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        //initializing items that need to be printed to screen
//        displayFileName = (TextView)findViewById(R.id.defaultFileName);
//        showSaveDirectory = (TextView)findViewById(R.id.showSaveDirectory);
//        tReset = (EditText)findViewById(R.id.tReset);
//        chapterReset = (EditText)findViewById(R.id.setChapter);

        //display defaults in their fields
//        printFileName(pref);
//        printSaveDirectory(pref);
//        printCounter(pref);
//        printChapter(pref);

        //setting up listeners on all buttons
//        langCodeListener(pref);
//        bookCodeListener(pref);
//        counterListener(pref);
//        resetListener(pref);
//        chapterListener(pref);
//        saveDirectoryListener();
//        ftpListener(pref);
//        syncListener(pref);
    }

    public void resetPrefs() {

    }

    public void onBackPressed(View v) {
        Intent intent = new Intent(Settings.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri currentUri;
        PreferencesManager preferences = new PreferencesManager(this);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SET_SAVE_DIR) {//this may need to be parsed differently
                currentUri = resultData.getData();
                File temp = new File(currentUri.getPath());
                preferences.setPreferences("fileDirectory", temp.getAbsolutePath());
            }
            if (requestCode == SET_SAVE_DIR2) {
                printSaveDirectory(preferences);
            }
        }
    }

    public void pullDB(Boolean flag){

        if(flag){
            Intent intent = new Intent(c, LanguageNamesRequest.class);
            startActivityForResult(intent, 0);
        }else {
            try {
                String tableName = "langnames";
                SQLiteDatabase audiorecorder = openOrCreateDatabase(tableName, MODE_PRIVATE, null);

                String querySelect = "SELECT * FROM " + tableName;

                Cursor cursor = audiorecorder.rawQuery(querySelect, new String[]{});
                audiorecorder.close();
            } catch (Exception e) {
                Intent intent = new Intent(c, LanguageNamesRequest.class);
                startActivityForResult(intent, 0);

            }
        }
        pullLangNames();
    }

    public void pullLangNames(){

        try {
            //SELECT
            String tableName = "langnames";
            SQLiteDatabase audioRecorder = openOrCreateDatabase(tableName, MODE_PRIVATE, null);

            String querySelect = "SELECT * FROM " + tableName;
            String queryCount = "SELECT COUNT(*) FROM " + tableName;

            //un-synchronized
            Cursor cursor = audioRecorder.rawQuery(querySelect, new String[]{});


            ArrayList<Language> languageList = new ArrayList<>();
            String[] listHolder;
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isLast()) {
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

            audioRecorder.close();

            listHolder = new String[languageList.size()];
            for (int a = 0; a < listHolder.length; a++) {
                listHolder[a] = (languageList.get(a)).getCode() + " - " +
                        (languageList.get(a)).getName();
                //System.out.println(listHolder[a]);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listHolder);
            setLangCode.setAdapter(adapter);
        }catch(RuntimeException e){
            e.printStackTrace();
            //No existing database
        }
    }

    /**
     * Prints the file name to the appropriate textview
     * @param pref the preference manager that holds the save file name
     */
    private void printFileName(PreferencesManager pref){
        sampleName = (String) pref.getPreferences("fileName");
        displayFileName.setText(sampleName + "-" + pref.getPreferences("fileCounter"));
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
     * Prints the current chapter on the chapter button
     * @param pref the preference manager that holds the current chapter
     */
    private void printChapter(PreferencesManager pref){
        String chapter = pref.getPreferences("chapter").toString();
        chapterReset.setText(chapter);
    }

    /**
     * Prints the current language code in the field
     * @param pref the preference manager that holds the current code
     */
    private void printLanguage(PreferencesManager pref){
        String counter = pref.getPreferences("targetLanguage").toString();
        setLangCode.setText(counter);
    }

    /**
     * Prints the current book code in the field
     * @param pref the preference manager that holds the current code
     */
    private void printBook(PreferencesManager pref){
        String counter = pref.getPreferences("book").toString();
        setBookCode.setText(counter);
    }

    /**
     * Updates the fileName based on the
     * @param pref the preference manager that holds the target language
     */
    private void updateFileName(PreferencesManager pref){
        String name = pref.getPreferences("targetLanguage") + "-" +
                pref.getPreferences("book") + "-" + pref.getPreferences("chapter");
        pref.setPreferences("fileName", name);
        printFileName(pref);
    }


    /**
     * Sets a listener on the langCode textView
     * @param pref the preference manager
     */
    private void langCodeListener(final PreferencesManager pref) {
        setLangCode = (MyAutoCompleteTextView)findViewById(R.id.setLangCode);
        setLangCode.setText(pref.getPreferences("targetLanguage").toString());//default

        //update
        pullDB(false);
        setLangCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setLangCode.showDropDown();
            }
        });

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
                String[] temp = s.toString().split(" - ");//we only want the language code
                String newVal = temp[0];


                newVal = newVal.replaceAll("![A-Za-z0-9]", "");//remove any strange characters

                pref.setPreferences("targetLanguage", newVal);//add selecet language code to preferences
                updateFileName(pref);
            }
        });
    }

    /**
     * Sets up the bookCode Textview with a listener
     * @param pref preferences manager
     */
    private void bookCodeListener(final PreferencesManager pref) {
        String[] bookArray = getResources().getStringArray(R.array.bookCodes);//resource of 3 letter Bible book codes

        setBookCode = (MyAutoCompleteTextView)findViewById(R.id.setBookCode);
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,bookArray);
        setBookCode.setAdapter(bookAdapter);
        setBookCode.setText(pref.getPreferences("book").toString());

        setBookCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setBookCode.showDropDown();
            }
        });

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
                newVal = newVal.replaceAll("![A-Za-z0-9]", "");//get rid of strange characters
                pref.setPreferences("book", newVal);//save selection to preferences
                updateFileName(pref);
                if(s.length() == 0){
                    setBookCode.showDropDown();
                }
            }
        });
    }

    /**
     * Sets a listener on the counter editText
     * @param pref the preference manager
     */
    private void counterListener(final PreferencesManager pref) {
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
                int counter = (int)pref.getPreferences("fileCounter");//set default
                try {//should never have an error since input will always be a number
                    counter = Integer.parseInt(newVal);
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
                }
                pref.setPreferences("fileCounter", counter);//save to preferences
                printFileName(pref);//update file name
            }
        });
    }

    /**
     * Sets a listener on the chapter editText
     * @param pref the preference manager
     */
    private void chapterListener(final PreferencesManager pref) {
        chapterReset = (EditText)findViewById(R.id.setChapter);
        chapterReset.addTextChangedListener(new TextWatcher() {
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
                int chapter = (int)pref.getPreferences("chapter");//set default
                try {//should never have an error since input will always be a number
                    chapter = Integer.parseInt(newVal);
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
                }
                pref.setPreferences("chapter", chapter);//save to preferences
                updateFileName(pref);
                printFileName(pref);//update file name
            }
        });
    }

    /**
     * Sets a listener on the reset button
     * @param pref the preference manager
     */
    private void resetListener(final PreferencesManager pref) {
        hardReset = (Button)findViewById(R.id.btnRestoreDefault);
        hardReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.resetPreferences("all");//resets preferences

                //update all fields based on changes
                printSaveDirectory(pref);
                printFileName(pref);
                printCounter(pref);
                printLanguage(pref);
                printBook(pref);
                printChapter(pref);
            }
        });
    }

    /**
     * Setting a listener on the sync button to sync with the langcodes on the server
     * @param pref the preferences manager
     */
    private void syncListener(final PreferencesManager pref){
        Button sync = (Button)findViewById(R.id.serverSync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pullDB(true);//tries to pull db from url
            }
        });
    }

    /**
     * Sets up a save directory listener that will call the ExportFiles.class activity
     * so that the user can choose out of all available directories where files
     * should be saved.
     */
    private void saveDirectoryListener() {
        TextView saveDir = (TextView)findViewById(R.id.showSaveDirectory);
        saveDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* toDO: need to change some things in Preference manager before getting this
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent intent = new Intent();
                    intent.setAction(intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, SET_SAVE_DIR);
                }*/
                Intent intent = new Intent(c, ExportFiles.class);
                startActivityForResult(intent, SET_SAVE_DIR2);
            }
        });
    }

    /**
     * Sets a listener on the ftp button and pulls up a dialog for FTP defaults to be set
     * the user's inputs are then saved by the preference manager as defaults
     * @param pref The preference manager
     */
    private void ftpListener(final PreferencesManager pref) {
        ImageButton setFtp = (ImageButton)findViewById(R.id.setFtp);
        setFtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting up the dialog
                final Dialog ftp = new Dialog(c);
                ftp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ftp.setCanceledOnTouchOutside(true);
                ftp.getWindow().setBackgroundDrawableResource(R.color.transparent);
                ftp.setContentView(R.layout.ftp_dialog);

                //getting the appropriate fields and putting defaults into textfields
                final EditText server = (EditText) ftp.findViewById(R.id.ftpServer);
                server.setText(pref.getPreferences("ftpServer").toString());
                final EditText userName = (EditText) ftp.findViewById(R.id.userName);
                userName.setText(pref.getPreferences("ftpUserName").toString());
                final EditText port = (EditText) ftp.findViewById(R.id.ftpPort);
                port.setText(pref.getPreferences("ftpPort").toString());
                final EditText password = (EditText) ftp.findViewById(R.id.ftpPassword);
                password.setEnabled(false);
                final EditText secureFtp = (EditText) ftp.findViewById(R.id.secureFtp);
                secureFtp.setText(pref.getPreferences("ftp").toString());
                final EditText directory = (EditText) ftp.findViewById(R.id.ftpDirectory);
                directory.setText(pref.getPreferences("ftpDirectory").toString());

                //on the click of the "OK" button, the preference manager saves the input in each field
                ImageButton ok = (ImageButton) ftp.findViewById(R.id.ftpOk);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        pref.setPreferences("ftpServer", server.getText().toString());
                        pref.setPreferences("ftpUserName", userName.getText().toString());
                        pref.setPreferences("ftpPort", port.getText().toString());
                        pref.setPreferences("ftp", secureFtp.getText().toString());
                        pref.setPreferences("ftpDirectory", directory.getText().toString());
                        ftp.dismiss();
                    }
                });

                ftp.show();
            }
        });
    }

}