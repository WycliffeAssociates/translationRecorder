package org.wycliffeassociates.translationrecorder.Recording;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by sarabiaj on 3/23/2017.
 */

public class PermissionsDeniedActivity extends Activity {

    /**
     * This Activity is for the app permissions functionality of the Lenovo Tab 2.
     * App permissions for tablets with Marshmallow or higher will likely find this setting under the app's info page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Record Audio Permission Denied");
        d.setMessage("Could not start recording, check your Android security settings and enable permissions to Record Audio.");
        d.setPositiveButton("Click here to go to Security Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(intent);
            }
        });
        AlertDialog dialog = d.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        d.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
