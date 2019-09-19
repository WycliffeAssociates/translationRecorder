package org.wycliffeassociates.translationrecorder;

import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.door43.tools.reporting.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.login.UserActivity;
import org.wycliffeassociates.translationrecorder.permissions.PermissionActivity;
import org.wycliffeassociates.translationrecorder.project.ParseJSON;
import org.wycliffeassociates.translationrecorder.project.ProjectPlugin;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.User;

import java.io.*;
import java.util.List;

/**
 * Created by sarabiaj on 5/5/2016.
 */
public class SplashScreen extends PermissionActivity {

    private static int SPLASH_TIME_OUT = 3000;
    protected ProgressBar mProgressBar;
    private ProjectDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setMinimumHeight(8);

        db = ((TranslationRecorderApp)getApplication()).getDatabase();
    }

    @Override
    protected void onPermissionsAccepted() {
        Thread initDb = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initializePlugins();
                    initDatabase();
                    int profile = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this).getInt(Settings.KEY_PROFILE, -1);
                    if(profile == -1) {
                        Intent intent = new Intent(SplashScreen.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        startActivity(new Intent(SplashScreen.this, MainMenu.class));
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        initDb.start();
    }

    private void initDatabase(){
        ParseJSON parse = new ParseJSON(this);
        try {
            //Book[] books = parse.pullBooks();
            Language[] languages = parse.pullLangNames();
            //db.addBooks(books);
            db.addLanguages(languages);
            System.out.println("Proof: en is " + db.getLanguageName("en"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        importProfiles();
        deleteDanglingProfiles();
    }

    private void initializePlugins() throws IOException {
        AssetManager am = getAssets();
        String[] assetPlugins = am.list("Plugins/Anthologies");
        File pluginsDir = new File(getCacheDir(), "Plugins");
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        File anthologiesDir = new File(pluginsDir, "Anthologies");
        if (!anthologiesDir.exists()) {
            anthologiesDir.mkdirs();
        }
        copyPluginsFromAssets(am, pluginsDir, anthologiesDir, assetPlugins);

        String[] plugins = anthologiesDir.list();
        if (plugins != null && plugins.length > 0) {
            for (String s : plugins) {
                File plugin = new File(anthologiesDir, s);
                //if (!plugin.exists()) {
                    importPlugin(am, pluginsDir, anthologiesDir, s);
                //}
            }
        }
    }

    private void importPlugin(AssetManager am, File pluginsDir, File anthologiesDir, String plugin) throws IOException {
        File pluginPath = new File(anthologiesDir, plugin);
        ProjectPlugin projectPlugin = new ProjectPlugin(pluginsDir, pluginPath);
        copyPluginContentFromAssets(am, pluginsDir, "Anthologies", plugin);
        copyPluginContentFromAssets(am, pluginsDir, "Books", projectPlugin.getBooksPath());
        copyPluginContentFromAssets(am, pluginsDir, "Versions", projectPlugin.getVersionsPath());
        copyPluginContentFromAssets(am, pluginsDir, "Jars", projectPlugin.getJarPath());


        //copyPluginContentFromAssets(am, pluginPath, "Chunks", projectPlugin.getChunksPath());
        projectPlugin.importProjectPlugin(pluginsDir, db);
    }

    private void copyPluginsFromAssets(AssetManager am, File pluginsDir, File anthologiesDir, String[] plugins) throws IOException {
        if(plugins != null && plugins.length > 0) {
            for(String plugin : plugins) {
                copyPluginContentFromAssets(am, pluginsDir, "Anthologies", plugin);
                File pluginPath = new File(anthologiesDir, plugin);
                ProjectPlugin projectPlugin = new ProjectPlugin(pluginsDir, pluginPath);
                copyPluginContentFromAssets(am, pluginsDir, "Books", projectPlugin.getBooksPath());
                copyPluginContentFromAssets(am, pluginsDir, "Versions", projectPlugin.getVersionsPath());
                copyPluginContentFromAssets(am, pluginsDir, "Jars", projectPlugin.getJarPath());
            }
        }
    }

    private void copyPluginContentFromAssets(AssetManager am, File outputRoot, String prefix, String pluginName) {
        File outputDir = new File(outputRoot, prefix);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (InputStream is = am.open("Plugins/" + prefix + "/" + pluginName);
             FileOutputStream fos = new FileOutputStream(new File(outputDir, pluginName));
        ) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } catch (IOException e) {
            Logger.e(this.toString(), "Exception copying " + pluginName + " from assets", e);
        }
    }

    private void importProfiles() {
        File profilesDir = new File(
                Environment.getExternalStorageDirectory(),
                getResources().getString(R.string.folder_name) + "/Profiles/"
        );
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }

        for (File profile: profilesDir.listFiles()) {
            String hash = getHash(profile);
            String mimeType = null;

            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(profile.getAbsolutePath());
                mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (Exception e) {
                Log.i("PROFILE", "File is not a media file");
            }

            if(hash != null && mimeType != null && mimeType.equals("audio/mp4")) {
                db.addUser(new User(profile, hash));
            }
        }
    }

    private void deleteDanglingProfiles() {
        List<User> profiles = db.getAllUsers();
        for (User profile: profiles) {
            File file = profile.getAudio();
            if(!file.exists()) {
                db.deleteUser(profile.getHash());
            }
        }
    }

    private String getHash(File file) {
        try {
            return new String(Hex.encodeHex(DigestUtils.md5(new FileInputStream(file))));
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
