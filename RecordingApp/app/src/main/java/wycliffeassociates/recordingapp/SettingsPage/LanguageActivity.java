package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
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

    }
}
