package org.wycliffeassociates.translationrecorder.project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;

/**
 * Created by Joe on 3/31/2016.
 */
public class SelectSourceDirectory extends Activity {

    private int SRC_LOC = 42;
    private int REQUEST_DIRECTORY = 43;
    public final static String SOURCE_LOCATION = "result_path";
    public final static String SDK_LEVEL = "sdk_level";

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        //if(Build.VERSION.SDK_INT >= 21) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");

        startActivityForResult(intent, SRC_LOC);
//        } else {
//            final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);
//
//            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
//                    .newDirectoryName("DirChooserSample")
//                    .allowReadOnlyDirectory(true)
//                    .allowNewDirectoryNameModification(true)
//                    .build();
//
//            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
//
//            // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
//            startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
//        }
    }


    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Intent intent = new Intent();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SRC_LOC) {
                Uri uri = resultData.getData();


                getApplicationContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String uristring = uri.toString();
                //check magic number of file to see if it matches "aoh!" in ascii. If so, the file is likely a tR file.
                //safer than just assuming based on the extension
                try (InputStream is = this.getContentResolver().openInputStream(uri)) {
                    byte[] magicNumber = new byte[4];
                    is.read(magicNumber);
                    String header = new String(magicNumber, StandardCharsets.US_ASCII);
                    //aoc was an accident in a previous version
                    if(header.equals("aoh!") || header.equals("aoc!")){
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                        pref.edit().putString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, uristring).commit();
                        pref.edit().putInt(Settings.KEY_SDK_LEVEL, Build.VERSION.SDK_INT).commit();
                        intent.putExtra(SOURCE_LOCATION, uristring);
                        intent.putExtra(SDK_LEVEL, Build.VERSION.SDK_INT);
                    }
                } catch (FileNotFoundException e) {

                } catch (IOException e) {

                }
            }
        }
//         else if (requestCode == REQUEST_DIRECTORY){
//            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
//                String dirString = resultData.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//                pref.edit().putString(Settings.KEY_PREF_GLOBAL_SOURCE_LOC, dirString).commit();
//                pref.edit().putInt(Settings.KEY_SDK_LEVEL, Build.VERSION.SDK_INT).commit();
//                System.out.println(pref.getInt(Settings.KEY_SDK_LEVEL, 21));
//
//                intent.putExtra(SOURCE_LOCATION, dirString);
//                intent.putExtra(SDK_LEVEL, Build.VERSION.SDK_INT);
//            }
//        }
        setResult(resultCode, intent);
        this.finish();
    }
}
