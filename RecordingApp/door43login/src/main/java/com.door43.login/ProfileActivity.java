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



public class ProfileActivity extends BaseActivity {
    public static final int RESULT_BACKED_OUT_TOS = -22;
    private final int TOU_REQUEST = 100;
    Profile profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            profile = Profile.fromJSON(new JSONObject(getIntent().getStringExtra("profile_json")));
        } catch (Exception e){
            profile = null;
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

        if (profile != null && TermsOfUseActivity.termsAccepted(profile, this)) {
            Intent result = new Intent();
            try {
                result.putExtra("profile_json", profile.toJSON().toString());
                setResult(RESULT_OK, result);
            } catch (JSONException e) {
                setResult(RESULT_CANCELED);
                e.printStackTrace();
            }
            finish();
        } else if(profile != null){
            Intent intent = new Intent(this, TermsOfUseActivity.class);
            try {
                intent.putExtra("profile_json", profile.toJSON().toString());
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
            if(resultCode == RESULT_OK){
                if(data.hasExtra("profile_json")) {
                    try {
                        profile = Profile.fromJSON(new JSONObject(data.getStringExtra("profile_json")));
                    } catch (Exception e) {
                        profile = null;
                    }
                } else {
                    profile = null;
                }
            } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED){
                profile = null;
                Intent result = new Intent();
                setResult(TermsOfUseActivity.RESULT_DECLINED, result);
                finish();
            } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOS && profile != null){
                Intent result = new Intent();
                try {
                    result.putExtra("profile_json", profile.toJSON().toString());
                    setResult(TermsOfUseActivity.RESULT_BACKED_OUT_TOS, result);
                    finish();
                } catch (JSONException e) {
                    profile = null;
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
