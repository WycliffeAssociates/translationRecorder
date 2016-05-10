package com.door43.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.EditText;

import com.door43.sysutils.AppContext;
import com.door43.login.R;
import com.door43.login.TermsOfUseActivity;
import com.door43.login.core.Profile;
import com.door43.widgets.dialogs.CustomAlertDialog;
import com.door43.sysutils.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * How to Use:
 * Start this activity for result
 * Pass in a profile as a string extra with Profile.PROFILE_KEY. If no extra is provided, or the provided
 * string throws a JSON exception, the profile is set to null.
 *
 * Providing a profile will check if the terms of use have been accepted. If yes, the activity will return
 * with RESULT_OK and the serialized profile as an extra via Profile.PROFILE_KEY. If no, the terms of use
 * activity will be called.
 *
 * Terms of Use can be checked using the static method: TermsOfUseActivity.termsAccepted(String)
 *
 * Returns:
 * RESULT_OK: profile has accepted terms of use, profile included as extra via Profile.PROFILE_KEY
 * RESULT_CANCELED: user backed out of the profile page
 * TermsOfUseActivity.RESULT_BACKED_OUT_TOU: user backed out of Terms of Use activity. Profile included as extra, profile terms of use not accepted.
 * TermsOfUseActivity.RESULT_DECLINED_TOU: user declined Terms of Use
 */

public class ProfileActivity extends BaseActivity {
    private final int TOU_REQUEST = 100;
    Profile mProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra(Profile.PROFILE_KEY)){
            try{
                mProfile = Profile.fromJSON(new JSONObject(getIntent().getStringExtra(Profile.PROFILE_KEY)));
            } catch (Exception e){
                mProfile = null;
            }
        } else {
            mProfile = null;
        }

        setContentView(R.layout.activity_profile);

        View loginDoor43 = findViewById(R.id.login_door43);
        View registerDoor43 = findViewById(R.id.register_door43);
        View registerOffline = findViewById(R.id.register_offline);

        loginDoor43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LoginDoor43Activity.class);
                startActivityForResult(intent, TOU_REQUEST);
            }
        });
        registerDoor43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RegisterDoor43Activity.class);
                startActivityForResult(intent, TOU_REQUEST);
            }
        });
        registerOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RegisterOfflineActivity.class);
                startActivityForResult(intent, TOU_REQUEST);
            }
        });
        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //if a profile exists and the terms of service have been accepted, return the profile
        if (mProfile != null && TermsOfUseActivity.termsAccepted(mProfile, this)) {
            Intent result = new Intent();
            try {
                result.putExtra(Profile.PROFILE_KEY, mProfile.getSerializedProfile());
                setResult(RESULT_OK, result);
            } catch (JSONException e) {
                setResult(RESULT_CANCELED);
                e.printStackTrace();
            }
            finish();
        //if a profile exists but the terms haven't been accepted, proceed to terms of use
        } else if(mProfile != null){
            Intent intent = new Intent(this, TermsOfUseActivity.class);
            try {
                intent.putExtra(Profile.PROFILE_KEY, mProfile.getSerializedProfile());
                startActivityForResult(intent, TOU_REQUEST);
            } catch (JSONException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == TOU_REQUEST){
            //TOU accepted; get the profile from the intent
            if(resultCode == RESULT_OK){
                if(data.hasExtra(Profile.PROFILE_KEY)) {
                    try {
                        mProfile = Profile.fromJSON(new JSONObject(data.getStringExtra(Profile.PROFILE_KEY)));
                    } catch (Exception e) {
                        mProfile = null;
                    }
                } else {
                    mProfile = null;
                }
            //TOU declined, return a declined result and empty intent
            } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED_TOU){
                mProfile = null;
                Intent result = new Intent();
                setResult(TermsOfUseActivity.RESULT_DECLINED_TOU, result);
                finish();
            //backed out of TOU page; save the profile and close the app
            } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOU && mProfile != null){
                Intent result = new Intent();
                try {
                    result.putExtra(Profile.PROFILE_KEY, mProfile.getSerializedProfile());
                    setResult(TermsOfUseActivity.RESULT_BACKED_OUT_TOU, result);
                    finish();
                } catch (JSONException e) {
                    mProfile = null;
                }
            }
        }
    }

    /**
     * Displays the privacy notice
     * @param listener if set the dialog will become a confirmation dialog
     */
    public static void showPrivacyNotice(Activity context, View.OnClickListener listener) {
        CustomAlertDialog privacy = CustomAlertDialog.Create(context)
                .setTitle(R.string.privacy_notice)
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage(R.string.publishing_privacy_notice);

        if(listener != null) {
            privacy.setPositiveButton(R.string.label_continue, listener);
            privacy.setNegativeButton(R.string.title_cancel, null);
        } else {
            privacy.setPositiveButton(R.string.dismiss, null);
        }
        privacy.show("privacy-notice");
    }
}
