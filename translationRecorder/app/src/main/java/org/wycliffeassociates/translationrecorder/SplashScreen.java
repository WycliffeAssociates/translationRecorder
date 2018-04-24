package org.wycliffeassociates.translationrecorder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import com.door43.login.ProfileActivity;
import com.door43.login.TermsOfUseActivity;
import com.door43.login.core.Profile;
import com.door43.tools.reporting.Logger;

import org.json.JSONException;
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.data.model.Language;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ParseJSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sarabiaj on 5/5/2016.
 */
public class SplashScreen extends Activity {

    private static int SPLASH_TIME_OUT = 3000;
    protected ProgressBar mProgressBar;
    private ProjectDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        db = new ProjectDatabaseHelper(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setMinimumHeight(8);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String profile = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PROFILE, "");
        boolean termsOfUseAccepted = false;
        if (profile.compareTo("") != 0) {
            try {
                termsOfUseAccepted = TermsOfUseActivity.termsAccepted(profile, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (profile.compareTo("") == 0 || !termsOfUseAccepted) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(Profile.PROFILE_KEY, profile);
            startActivityForResult(intent, 42);
        } else {
            Thread initDb = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        initializePlugins();
                        initDatabase();
                        startActivity(new Intent(SplashScreen.this, MainMenu.class));
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            initDb.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //save the profile in preferences
        if (requestCode == 42) {
            if (resultCode == RESULT_OK) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit();
                //user backed out of profile page; empty the profile and finish
            } else if (resultCode == RESULT_CANCELED) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit();
                finish();
                //user backed out of the TOU, keep the profile with unaccepeted TOU and finish. App will resume to TOU page
            } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOU) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit();
                finish();
                //user declined TOU, clear profile and return to profile page
            } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED_TOU) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit();
            }
        }
    }

    private void initDatabase() {
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
        projectPlugin.importProjectPlugin(this, pluginsDir);
    }

    private void copyPluginsFromAssets(AssetManager am, File pluginsDir, File anthologiesDir, String[] plugins) throws IOException {
        if (plugins != null && plugins.length > 0) {
            for (String plugin : plugins) {
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
}
