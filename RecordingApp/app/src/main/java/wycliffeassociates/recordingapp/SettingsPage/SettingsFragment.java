package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/17/2015.
 */
public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    Context context;

    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";
    public static final String KEY_PREF_CHUNK = "pref_chunk";
    private static final String KEY_PREF_FILENAME = "pref_filename";
    private static final String KEY_PREF_TAKE = "pref_take";
//    sharedPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();

        context = getActivity();

        // Below is the code to clear the SharedPreferences. Use it wisely.
        // sharedPref.edit().clear().commit();

        // Register listener(s)
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Initial summary update to display the right values
        for (String k : sharedPref.getAll().keySet()) {
            System.out.println("UPDATING SUMMARY FOR: " + k);
            updateSummaryText(sharedPref, k);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get rid of the extra padding in the settings page body (where it loads this fragment)
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        updateSummaryText(sharedPref, key);
        if(key.compareTo(KEY_PREF_TAKE) == 0){
            //sharedPref.edit().putString(KEY_PREF_TAKE, "1").commit();
        }
        String chunkString = sharedPref.getString(Settings.KEY_PREF_CHUNK, "1");
        Matcher matcher = Pattern.compile("\\d+").matcher(chunkString);
        matcher.find();
        sharedPref.edit().putString(Settings.KEY_PREF_CHUNK, matcher.group()).commit();

        Settings.updateFilename(getActivity());
    }

    public void updateSummaryText(SharedPreferences sharedPref, String key) {
        try {
            String text  = sharedPref.getString(key, "");
            findPreference(key).setSummary(text);
        } catch (ClassCastException err) {
            System.out.println("IGNORING SUMMARY UPDATE FOR " + key);
        }
    }
}