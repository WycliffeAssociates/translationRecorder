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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import java.security.Key;
import java.util.Map;
import java.util.Set;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/17/2015.
 */
public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    Context context;

    public static final String KEY_PREF_LANG = "pref_lang";
    public static final String KEY_PREF_BOOK = "pref_book";
    public static final String KEY_PREF_CHAPTER = "pref_chapter";

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

        // Set custom text size
//        TextView pref_category_title = (TextView) getActivity().findViewById(R.id.title);
//        pref_category_title.setTextSize(16 * getResources().getDisplayMetrics().density);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        // Closes the soft keyboard manually. Still buggy.
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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