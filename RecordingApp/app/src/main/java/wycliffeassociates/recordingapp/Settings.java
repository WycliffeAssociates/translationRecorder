package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;

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

    final int SET_SAVE_DIR = 21;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings2);

        final PreferencesManager pref = new PreferencesManager(this);

        displayFileName = (TextView)findViewById(R.id.defaultFileName);
        showSaveDirectory = (TextView)findViewById(R.id.showSaveDirectory);
        tReset = (EditText)findViewById(R.id.tReset);

        printFileName(pref);
        printSaveDirectory(pref);
        printCounter(pref);

        //need to get an adapter for autocompletion eventually. Functional now
        setLangCode = (AutoCompleteTextView)findViewById(R.id.setLangCode);
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
                String newVal = s.toString();
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
                intent.setAction(intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, SET_SAVE_DIR);
            }
        });

        setFtp = (ImageButton)findViewById(R.id.setFtp);
        setFtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //toDo: open ftp dialog
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
