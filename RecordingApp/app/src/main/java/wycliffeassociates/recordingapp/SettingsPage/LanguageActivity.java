package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.AdapterView;

import wycliffeassociates.recordingapp.R;


/**
 * Created by sarabiaj on 2/23/2016.
 */
public class LanguageActivity extends Activity implements LanguageListFragment.OnItemClickListener {

    LanguageListFragment mLanguageListFragment;
    public final String TAG_LANGUAGE_LIST = "language_list_tag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selector);
    }

    @Override
    public void onItemClick(Language targetLanguage) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("Language set to " + pref.getString(Settings.KEY_PREF_LANG, targetLanguage.getCode()));
        System.out.println("Setting language to " + targetLanguage.getCode());
        pref.edit().putString(Settings.KEY_PREF_LANG, targetLanguage.getCode()).commit();
        System.out.println("Language now " + pref.getString(Settings.KEY_PREF_LANG, targetLanguage.getCode()));
        Settings.updateFilename(this);
        this.finish();
    }
}
