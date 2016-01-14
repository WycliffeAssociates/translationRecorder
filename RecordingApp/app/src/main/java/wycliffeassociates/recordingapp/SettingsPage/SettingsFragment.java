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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.security.Key;
import java.util.Map;
import java.util.Set;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/17/2015.
 */
public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    Context context;

    public static final String KEY_PREF_DEFAULT_FOLDER = "pref_default_folder";
    public static final String KEY_PREF_FTP_SERVER = "pref_ftp_server";
    public static final String KEY_PREF_FTP_DIRECTORY = "pref_ftp_directory";
    public static final String KEY_PREF_FTP_USERNAME = "pref_ftp_username";
    public static final String KEY_PREF_FTP_PASSWORD = "pref_ftp_password";

    private SharedPreferences sharedPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        context = getActivity();
        sharedPref = getPreferenceScreen().getSharedPreferences();

        // Register listener(s)
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Initial summary update to display the right values
        for (String k : sharedPref.getAll().keySet()) {
            updateSummary(k);
        }

        // TODO: Refactor by taking this out of onCreate()
        Preference restore_default = findPreference("pref_restore_default");
        restore_default.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // This is not working!!! :(
//                System.out.println("Restoring settings");
//                SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//                System.out.println("Default Pref: " + defaultPrefs);
//                System.out.println("Current Pref: " + sharedPref);
//                context.getSharedPreferences("pref_ftp_password", 0).edit().clear().commit();
//                PreferenceManager.setDefaultValues(context, R.xml.preference, true);
//                updateSummary("pref_ftp_password");
                EditTextPreference listPreference = (EditTextPreference) findPreference("pref_ftp_password");
//                listPreference.va("hahaha");
                updateSummary("pref_ftp_password");
                return true;
            }
        });
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
        updateSummary(key);
    }

    public void updateSummary(String key) {
        try {
            String text  = getPreferenceScreen().getSharedPreferences().getString(key, "");
            findPreference(key).setSummary(text);
        } catch (ClassCastException err) {
            System.out.println("Error in updating " + key + ": " + err);
        }
    }

    public static void closeSoftKeyboard(Context c, IBinder windowToken) {
        InputMethodManager im = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}