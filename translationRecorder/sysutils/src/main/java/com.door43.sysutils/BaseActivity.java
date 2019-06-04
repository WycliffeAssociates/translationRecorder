package com.door43.sysutils;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This should be extended by all activities in the app so that we can perform verification on
 * activities such as recovery from crashes.
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onResume() {
        super.onResume();
//
//        if(this instanceof TermsOfUseActivity == false
//                && this instanceof SplashScreenActivity == false
//                && this instanceof CrashReporterActivity == false) {
//            // check if we crashed or if we need to reload
//            File dir = new File(AppContext.getPublicDirectory(), AppContext.context().STACKTRACE_DIR);
//            String[] crashFiles = GlobalExceptionHandler.getStacktraces(dir);
//            if (crashFiles.length > 0) {
//                // restart
//                Intent intent = new Intent(this, SplashScreenActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }
    }
}
