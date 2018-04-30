package org.wycliffeassociates.translationrecorder

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ProgressBar
import com.door43.login.ProfileActivity
import com.door43.login.TermsOfUseActivity
import com.door43.login.core.Profile
import com.door43.tools.reporting.Logger
import org.json.JSONException
import org.wycliffeassociates.translationrecorder.R.id.progress_bar
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.data.repository.LanguageRepository
import org.wycliffeassociates.translationrecorder.persistence.repository.RoomDb
import org.wycliffeassociates.translationrecorder.project.ParseJSON
import org.wycliffeassociates.translationrecorder.project.ProjectPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by sarabiaj on 5/5/2016.
 */
class SplashScreen : Activity() {

    private lateinit var languageDb: LanguageRepository
    private lateinit var roomDb: RoomDb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        roomDb = RoomDb.getInstance(this)
        languageDb = roomDb.languageRepo

        progress_bar as ProgressBar
        progress_bar.max = 100
        progress_bar.isIndeterminate = true
        progress_bar.minimumHeight = 8
    }

    override fun onResume() {
        super.onResume()
        val profile = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PROFILE, "")
        var termsOfUseAccepted = false
        if (profile.isNotBlank()) {
            termsOfUseAccepted = TermsOfUseActivity.termsAccepted(profile, this)
        }
        if (profile.isNotBlank() || !termsOfUseAccepted) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(Profile.PROFILE_KEY, profile)
            startActivityForResult(intent, 42)
        } else {
            val initDb = Thread(Runnable {
                try {
                    initializePlugins()
                    initDatabase()
                    startActivity(Intent(this@SplashScreen, MainMenu::class.java))
                    finish()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })
            initDb.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        //save the profile in preferences
        if (requestCode == 42) {
            if (resultCode == Activity.RESULT_OK) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit()
                //user backed out of profile page; empty the profile and finish
            } else if (resultCode == Activity.RESULT_CANCELED) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit()
                finish()
                //user backed out of the TOU, keep the profile with unaccepeted TOU and finish. App will resume to TOU page
            } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOU) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit()
                finish()
                //user declined TOU, clear profile and return to profile page
            } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED_TOU) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit()
            }
        }
    }

    private fun initDatabase() {
        val parse = ParseJSON(this)
        try {
            //Book[] books = parse.pullBooks();
            val languages = parse.pullLangNames()
            //db.addBooks(books);
            languageDb.insertAll(languages.toList())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun initializePlugins() {
        val am = assets
        val assetPlugins = am.list("Plugins/Anthologies")
        val pluginsDir = File(cacheDir, "Plugins")
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
        }
        val anthologiesDir = File(pluginsDir, "Anthologies")
        if (!anthologiesDir.exists()) {
            anthologiesDir.mkdirs()
        }
        copyPluginsFromAssets(am, pluginsDir, anthologiesDir, assetPlugins)

        val plugins = anthologiesDir.list()
        if (plugins != null && plugins.isNotEmpty()) {
            for (s in plugins) {
                val plugin = File(anthologiesDir, s)
                //if (!plugin.exists()) {
                importPlugin(am, pluginsDir, anthologiesDir, s)
                //}
            }
        }
    }

    @Throws(IOException::class)
    private fun importPlugin(am: AssetManager, pluginsDir: File, anthologiesDir: File, plugin: String) {
        val pluginPath = File(anthologiesDir, plugin)
        val projectPlugin = ProjectPlugin(pluginsDir, pluginPath)
        copyPluginContentFromAssets(am, pluginsDir, "Anthologies", plugin)
        copyPluginContentFromAssets(am, pluginsDir, "Books", projectPlugin.booksPath)
        copyPluginContentFromAssets(am, pluginsDir, "Versions", projectPlugin.versionsPath)
        copyPluginContentFromAssets(am, pluginsDir, "Jars", projectPlugin.jarPath)

        projectPlugin.importProjectPlugin(this, pluginsDir)
    }

    @Throws(IOException::class)
    private fun copyPluginsFromAssets(am: AssetManager, pluginsDir: File, anthologiesDir: File, plugins: Array<String>?) {
        if (plugins != null && plugins.isNotEmpty()) {
            for (plugin in plugins) {
                copyPluginContentFromAssets(am, pluginsDir, "Anthologies", plugin)
                val pluginPath = File(anthologiesDir, plugin)
                val projectPlugin = ProjectPlugin(pluginsDir, pluginPath)
                copyPluginContentFromAssets(am, pluginsDir, "Books", projectPlugin.booksPath)
                copyPluginContentFromAssets(am, pluginsDir, "Versions", projectPlugin.versionsPath)
                copyPluginContentFromAssets(am, pluginsDir, "Jars", projectPlugin.jarPath)
            }
        }
    }

    private fun copyPluginContentFromAssets(am: AssetManager, outputRoot: File, prefix: String, pluginName: String) {
        val outputDir = File(outputRoot, prefix)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        try {
            am.open("Plugins/$prefix/$pluginName").use { input ->
                FileOutputStream(File(outputDir, pluginName)).use { fos ->
                    input.copyTo(fos)
                }
            }
        } catch (e: IOException) {
            Logger.e(this.toString(), "Exception copying $pluginName from assets", e)
        }

    }
}
