package com.door43.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.door43.login.legal.LegalDocumentActivity;
import com.door43.sysutils.BaseActivity;

/**
 * This activity checks if the user has accepted the terms of use before continuing to load the app
 */
public class TermsOfUseActivity extends BaseActivity {

    public static final int RESULT_DECLINED_TOU = RESULT_FIRST_USER;
    public static final int RESULT_BACKED_OUT_TOU = RESULT_FIRST_USER + 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            setContentView(R.layout.activity_terms);
            Button rejectBtn = (Button) findViewById(R.id.reject_terms_btn);
            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //returns to the profile activity with an empty intent (clears the profile)
                    setResult(RESULT_DECLINED_TOU, new Intent());
                    finish();
                }
            });
            Button acceptBtn = (Button) findViewById(R.id.accept_terms_btn);
            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

            Button licenseBtn = (Button) findViewById(R.id.license_btn);
            licenseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.license);
                }
            });
            Button guidelinesBtn = (Button) findViewById(R.id.translation_guidelines_btn);
            guidelinesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.translation_guidlines);
                }
            });
            Button faithBtn = (Button) findViewById(R.id.statement_of_faith_btn);
            faithBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLicenseDialog(R.string.statement_of_faith);
                }
            });
    }

    @Override
    public void onBackPressed() {
        //Need to distinguish this back press so that the profile is not lost and the app can start again
        //with asking for terms of use. A back press from login or profile creation should delete the profile however;
        //and therefore all three types of activities cannot all use RESULT_CANCELLED for a back press.
        //Order matters, calling super first will set the result to RESULT_CANCELLED
        setResult(RESULT_DECLINED_TOU, getIntent());
        super.onBackPressed();
        finish();
    }



    /**
     * Displays a license dialog with the given resource as the text
     *
     * @param stringResource the string resource to display in the dialog.
     */
    private void showLicenseDialog(int stringResource) {
        Intent intent = new Intent(this, LegalDocumentActivity.class);
        intent.putExtra(LegalDocumentActivity.ARG_RESOURCE, stringResource);
        startActivity(intent);
    }


}


