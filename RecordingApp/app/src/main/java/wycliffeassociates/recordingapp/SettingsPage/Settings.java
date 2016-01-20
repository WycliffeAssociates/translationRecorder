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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    private static final String KEY_PREF_FILENAME = "pref_filename";
    private static final String KEY_PREF_TAKE = "pref_take";

    MyAutoCompleteTextView setLangCode,setBookCode;

    /**
     * Request code for Android 5.0+
     */
    final int SET_SAVE_DIR = 21;

    /**
     * Request code for Android <5.0
     */
    final int SET_SAVE_DIR2 = 22;

    public static String generateFilename(Context c){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
        String langCode = pref.getString(KEY_PREF_LANG, "en");
        String bookCode = pref.getString(KEY_PREF_BOOK, "mat");
        String chapter = pref.getString(KEY_PREF_CHAPTER, "1");
        String chunk = pref.getString(KEY_PREF_CHUNK, "1");
        String take = pref.getString(KEY_PREF_TAKE, "1");
        String filename = langCode + "_" + bookCode + "_" + chapter + "-" + chunk + "_" + take;
        return filename;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    }

    public void resetPrefs() {

    }

    public void onBackPressed(View v) {
        Intent intent = new Intent(Settings.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }




}