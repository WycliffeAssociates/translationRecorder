package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import com.door43.login.ProfileActivity;
import com.door43.login.TermsOfUseActivity;
import com.door43.login.core.Profile;

/**
 * Created by sarabiaj on 5/5/2016.
 */
public class SplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String profile = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PROFILE, "");
        if(profile.compareTo("") == 0 || !TermsOfUseActivity.termsAccepted(profile, this)) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("profile_json", profile);
            startActivityForResult(intent, 42);
        } else {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 42 && resultCode == RESULT_OK) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra("profile_json")).commit();
        } else if (resultCode == RESULT_CANCELED){
            finish();
        } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOS){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra("profile_json")).commit();
            finish();
        } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit();
        }
    }
}
