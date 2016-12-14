package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by leongv on 12/17/2015.
 */
public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    LanguageSelector mParent;
    SharedPreferences mSharedPreferences;

    interface LanguageSelector{
        void sourceLanguageSelected();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        mParent = (LanguageSelector) getActivity();
        // Below is the code to clear the SharedPreferences. Use it wisely.
        // mSharedPreferences.edit().clear().commit();

        // Register listener(s)
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        Preference sourceLanguageButton = (Preference)findPreference(Settings.KEY_PREF_GLOBAL_LANG_SRC);
        sourceLanguageButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mParent.sourceLanguageSelected();
                return true;
            }
        });

        Preference addTemporaryLanguageButton = (Preference)findPreference(Settings.KEY_PREF_ADD_LANGUAGE);
        addTemporaryLanguageButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AddTargetLanguageDialog add = new AddTargetLanguageDialog();
                    add.show(getFragmentManager(), "add");
                    return false;
                }
            }
        );

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
        for (String k : mSharedPreferences.getAll().keySet()) {
            updateSummaryText(mSharedPreferences, k);
        }
    }

    @Override
    public void onPause() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences mSharedPreferences, String key) {
        updateSummaryText(mSharedPreferences, key);
    }

    private void updateSummariesSetViaActivities(SharedPreferences mSharedPreferences){
        String uristring = mSharedPreferences.getString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, "");
        Uri dir = Uri.parse(uristring);
        if(dir != null) {
            uristring = dir.getLastPathSegment();
            //This removes "primary:", though maybe this is helpful in identifying between sd card and internal storage.
            //uristring = uristring.substring(uristring.indexOf(":")+1, uristring.length());
            findPreference(Settings.KEY_PREF_GLOBAL_SOURCE_LOC).setSummary(uristring);
        } else {
            findPreference(Settings.KEY_PREF_GLOBAL_SOURCE_LOC).setSummary(mSharedPreferences.getString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, ""));
        }
    }

    public void updateSummaryText(SharedPreferences mSharedPreferences, String key) {
        try {
            updateSummariesSetViaActivities(mSharedPreferences);
            String text  = mSharedPreferences.getString(key, "");
            if(findPreference(key) != null) {
                findPreference(key).setSummary(text);
            }
        } catch (ClassCastException err) {
            System.out.println("IGNORING SUMMARY UPDATE FOR " + key);
        }
    }
}