package com.door43.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;

import com.door43.login.core.Profile;
import com.door43.sysutils.BaseActivity;
import com.door43.login.legal.LegalDocumentActivity;
import com.door43.widgets.widgets.ViewUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This activity checks if the user has accepted the terms of use before continuing to load the app
 */
public class TermsOfUseActivity extends BaseActivity {
    public static final int RESULT_DECLINED_TOU = -11;
    public static final int RESULT_BACKED_OUT_TOU = -22;
    Profile mProfile = null;
    int mTermsVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mProfile = Profile.fromJSON(new JSONObject(getIntent().getStringExtra(Profile.PROFILE_KEY)));
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        mTermsVersion = getResources().getInteger(R.integer.terms_of_use_version);

        if(mProfile == null) {
            finish();
            return;
        }

        if (mTermsVersion == mProfile.getTermsOfUseLastAccepted()) {
            // skip terms if already accepted
            startMainActivity();
        } else {
            setContentView(R.layout.activity_terms);
            Button rejectBtn = (Button)findViewById(R.id.reject_terms_btn);
            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //returns to the profile activity with an empty intent (clears the profile)
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            });
            Button acceptBtn = (Button)findViewById(R.id.accept_terms_btn);
            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProfile.setTermsOfUseLastAccepted(mTermsVersion);
                    startMainActivity();
                }
            });
            Button licenseBtn = (Button)findViewById(R.id.license_btn);
            licenseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.license);
                }
            });
            Button guidelinesBtn = (Button)findViewById(R.id.translation_guidelines_btn);
            guidelinesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.translation_guidlines);
                }
            });
            Button faithBtn = (Button)findViewById(R.id.statement_of_faith_btn);
            faithBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.statement_of_faith);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        //Need to distinguish this back press so that the profile is not lost and the app can start again
        //with asking for terms of use. A back press from login or profile creation should delete the profile however;
        //and therefore all three types of activities cannot all use RESULT_CANCELLED for a back press.
        //Order matters, calling super first will set the result to RESULT_CANCELLED
        setResult(RESULT_BACKED_OUT_TOU, getIntent());
        super.onBackPressed();
        finish();
    }

    /**
     * Continues to the splash screen where local resources will be loaded
     */
    private void startMainActivity() {
        if(mProfile != null && mProfile.getTermsOfUseLastAccepted() == mTermsVersion){
            try {
                Intent result = new Intent();
                result.putExtra(Profile.PROFILE_KEY, mProfile.getSerializedProfile());
                setResult(RESULT_OK, result);
            } catch (JSONException e) {
                setResult(RESULT_CANCELED);
            }
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    /**
     * Displays a license dialog with the given resource as the text
     * @param stringResource the string resource to display in the dialog.
     */
    private void showLicenseDialog(int stringResource) {
        Intent intent = new Intent(this, LegalDocumentActivity.class);
        intent.putExtra(LegalDocumentActivity.ARG_RESOURCE, stringResource);
        startActivity(intent);
    }

    /**
     * Checks if the terms of use have been accepted
     * @param profile the user profile to check
     * @param ctx a context that can access the terms of use resource value
     * @return true if the terms are accepted, false otherwise
     */
    public static boolean termsAccepted(Profile profile, Context ctx){
        return profile.getTermsOfUseLastAccepted() == ctx.getResources().getInteger(R.integer.terms_of_use_version);
    }

    /**
     * Checks if the terms of use have been accepted
     * @param profile a json string containing the user profile to check
     * @param ctx a context that can access the terms of use resource value
     * @return true if the terms are accepted, false otherwise
     * @throws throws if there was an exception in Profile.fromJson()
     */
    public static boolean termsAccepted(String profile, Context ctx) throws Exception {
        Profile p = Profile.fromJSON(new JSONObject(profile));
        return p.getTermsOfUseLastAccepted() == ctx.getResources().getInteger(R.integer.terms_of_use_version);
    }
}
