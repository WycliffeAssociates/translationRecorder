package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;

import org.apache.commons.io.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Created by Joe on 3/31/2016.
 */
public class SelectSourceDirectory extends Activity {

    private int SRC_LOC = 42;

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        intent.setType("audio/*");
//        intent.putExtra(Intent.EXTRA_TITLE, "hi");
        startActivityForResult(intent, SRC_LOC);
    }


    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            Uri treeUri = resultData.getData();

            getApplicationContext().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            DocumentFile selectedDirectory = DocumentFile.fromTreeUri(this, treeUri);
            Uri dir = selectedDirectory.getUri();
            String uristring = dir.toString();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            pref.edit().putString(Settings.KEY_PREF_SRC_LOC, uristring).commit();
        }
        this.finish();
    }
}
