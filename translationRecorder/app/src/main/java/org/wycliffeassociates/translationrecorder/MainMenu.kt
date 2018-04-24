package org.wycliffeassociates.translationrecorder

import android.app.Activity
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

import com.door43.tools.reporting.GithubReporter
import com.door43.tools.reporting.GlobalExceptionHandler
import com.door43.tools.reporting.Logger

import org.apache.commons.io.FileUtils
import org.wycliffeassociates.translationrecorder.ProjectManager.activities.ActivityProjectManager
import org.wycliffeassociates.translationrecorder.Recording.RecordingActivity
import org.wycliffeassociates.translationrecorder.Reporting.BugReportDialog
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.data.model.ProjectPatternMatcher
import org.wycliffeassociates.translationrecorder.data.model.ProjectSlugs
import org.wycliffeassociates.translationrecorder.data.repository.BookRepository
import org.wycliffeassociates.translationrecorder.data.repository.LanguageRepository
import org.wycliffeassociates.translationrecorder.data.repository.ProjectRepository
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import org.wycliffeassociates.translationrecorder.persistence.mapping.ProjectMapper
import org.wycliffeassociates.translationrecorder.persistence.repository.RoomDb
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.BookDao
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.LanguageDao
import org.wycliffeassociates.translationrecorder.persistence.repository.dao.ProjectDao
import org.wycliffeassociates.translationrecorder.project.ProjectWizardActivity
import org.wycliffeassociates.translationrecorder.project.TakeInfo

import java.io.File
import java.io.IOException
import java.util.ArrayList

class MainMenu : Activity() {

    private var btnRecord: RelativeLayout? = null
    private var btnFiles: ImageButton? = null
    private var pref: SharedPreferences? = null

    private var mNumProjects = 0

    private var mDb: ProjectDatabaseHelper? = null
    private lateinit var projectDb: ProjectRepository
    private lateinit var bookDb: BookRepository
    private lateinit var languageDb: LanguageRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mDb = ProjectDatabaseHelper(this)
        val rdb = RoomDb.getInstance(applicationContext)
        if (rdb != null) {
            projectDb = rdb.projectDao()
            bookDb = rdb.bookDao()
            languageDb = rdb.languageDao()
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        AudioInfo.SCREEN_WIDTH = metrics.widthPixels

        println("internal files dir is " + this.cacheDir)
        println("External files dir is " + Environment.getExternalStorageDirectory())

        initApp()
    }

    override fun onResume() {
        super.onResume()

        mNumProjects = projectDb!!.getProjects().size

        btnRecord = findViewById<View>(R.id.new_record) as RelativeLayout
        btnRecord!!.setOnClickListener {
            if (mNumProjects <= 0 || emptyPreferences()) {
                setupNewProject()
            } else {
                startRecordingScreen()
            }
        }

        btnFiles = findViewById<View>(R.id.files) as ImageButton
        btnFiles!!.setOnClickListener { v ->
            val intent = Intent(v.context, ActivityProjectManager::class.java)
            startActivityForResult(intent, 0)
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left)
        }

        initViews()
    }

    private fun emptyPreferences(): Boolean {
        return if (pref!!.getInt(Settings.KEY_RECENT_PROJECT_ID, -1) == -1) {
            true
        } else false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            PROJECT_WIZARD_REQUEST -> run {
                if (resultCode == Activity.RESULT_OK) {
                    val project = data.getParcelableExtra<Parcelable>(Project.PROJECT_EXTRA)
                    if (addProjectToDatabase(project)) {
                        loadProject(project)
                        val intent = RecordingActivity.getNewRecordingIntent(
                                this,
                                project,
                                ChunkPlugin.DEFAULT_CHAPTER,
                                ChunkPlugin.DEFAULT_UNIT
                        )
                        startActivity(intent)
                    } else {
                        onResume()
                    }
                } else {
                    onResume()
                }
            }
        }
    }

    private fun setupNewProject() {
        startActivityForResult(Intent(baseContext, ProjectWizardActivity::class.java), PROJECT_WIZARD_REQUEST)
    }

    private fun startRecordingScreen() {
        val project = Project.getProjectFromPreferences(this)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val chapter = pref.getInt(Settings.KEY_PREF_CHAPTER, ChunkPlugin.DEFAULT_CHAPTER)
        val unit = pref.getInt(Settings.KEY_PREF_CHUNK, ChunkPlugin.DEFAULT_UNIT)
        val intent = RecordingActivity.getNewRecordingIntent(
                this,
                project,
                chapter,
                unit
        )
        startActivity(intent)
    }

    private fun addProjectToDatabase(project: Project): Boolean {
        if (projectDb!!.getById(project.id!!)) {
            ProjectWizardActivity.displayProjectExists(this)
            return false
        } else {
            mDb!!.addProject(project)
            return true
        }
    }


    private fun loadProject(project: Project) {
        pref!!.edit().putString("resume", "resume").commit()

        if (mDb!!.projectExists(project)) {
            pref!!.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, mDb!!.getProjectId(project)).commit()
        } else {
            Logger.e(this.toString(), "Project $project doesn't exist in the database")
        }
    }

    fun report(message: String) {
        val t = Thread(Runnable { reportCrash(message) })
        t.start()
    }

    private fun reportCrash(message: String) {
        val dir = File(externalCacheDir, STACKTRACE_DIR)
        val stacktraces = GlobalExceptionHandler.getStacktraces(dir)
        val githubTokenIdentifier = resources.getString(R.string.github_token)
        val githubUrl = resources.getString(R.string.github_bug_report_repo)

        // TRICKY: make sure the github_oauth2 token has been set
        if (githubTokenIdentifier != null) {
            val reporter = GithubReporter(this, githubUrl, githubTokenIdentifier)
            if (stacktraces.size > 0) {
                // upload most recent stacktrace
                reporter.reportCrash(message, File(stacktraces[0]), Logger.getLogFile())
                // empty the log
                try {
                    FileUtils.write(Logger.getLogFile(), "")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                archiveStackTraces()
            }
        }
    }

    fun archiveStackTraces() {
        val dir = File(externalCacheDir, STACKTRACE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val archive = File(dir, "Archive")
        if (!archive.exists()) {
            archive.mkdirs()
        }
        val stacktraces = GlobalExceptionHandler.getStacktraces(dir)
        // delete stacktraces
        for (filePath in stacktraces) {
            val traceFile = File(filePath)
            if (traceFile.exists()) {
                val move = File(archive, traceFile.name)
                traceFile.renameTo(move)
            }
        }
    }

    private fun initViews() {
        val projectId = pref!!.getInt(Settings.KEY_RECENT_PROJECT_ID, -1)
        val languageView = findViewById<View>(R.id.language_view) as TextView
        val bookView = findViewById<View>(R.id.book_view) as TextView
        if (projectId != -1) {
            val project = projectDb.getById(projectId)
            val language = project.language.name
            languageView.setText(language)

            val book = project.getBookName()
            bookView.setText(book)
        } else {
            languageView.text = ""
            bookView.text = ""
        }
    }

    private fun initApp() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref!!.edit().putString("version", BuildConfig.VERSION_NAME).commit()

        //set up Visualization folder
        Utils.VISUALIZATION_DIR = File(externalCacheDir, "Visualization")
        Utils.VISUALIZATION_DIR.mkdirs()

        //if the current directory is already set, then don't overwrite it
        if (pref!!.getString("current_directory", null) == null) {
            pref!!.edit().putString("current_directory",
                    Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit()
        }
        pref!!.edit().putString("root_directory", Environment.getExternalStoragePublicDirectory("TranslationRecorder").toString()).commit()

        //configure logger
        val dir = File(externalCacheDir, STACKTRACE_DIR)
        dir.mkdirs()

        GlobalExceptionHandler.register(dir)
        val minLogLevel = Integer.parseInt(pref!!.getString(KEY_PREF_LOGGING_LEVEL, PREF_DEFAULT_LOGGING_LEVEL))
        configureLogger(minLogLevel, dir)

        //check if we crashed
        val stacktraces = GlobalExceptionHandler.getStacktraces(dir)
        if (stacktraces.size > 0) {
            val fm = fragmentManager
            val brd = BugReportDialog()
            brd.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            brd.show(fm, "Bug Report Dialog")
        }

        removeUnusedVisualizationFiles()
    }

    private fun removeUnusedVisualizationFiles() {
        val visFilesLocation = Utils.VISUALIZATION_DIR
        val visFiles = visFilesLocation.listFiles() ?: return
        val rootPath = File(Environment.getExternalStorageDirectory(), "TranslationRecorder").absolutePath
        val projects = projectDb!!.getAllProjects()
        val patterns = ArrayList<ProjectPatternMatcher>()
        for (project in projects) {
            patterns.add(project.getPatternMatcher())
        }
        for (v in visFiles) {
            var matched = false
            var takeInfo: TakeInfo? = null
            //no idea what project the vis file is, so try all known anthology regexes until one works
            for (ppm in patterns) {
                if (ppm.match(v)) {
                    matched = true
                    takeInfo = ppm.takeInfo
                    break
                }
            }
            if (!matched) {
                v.delete()
                continue
            }
            val found = false
            val (language, version, _, book) = takeInfo!!.projectSlugs
            val path = rootPath + "/" + language + "/" + version + "/" + book + "/" + String.format("%02d", takeInfo.chapter)
            val visFileWithoutExtension = v.name.split(".vis$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val name = visFileWithoutExtension + "_t" + String.format("%02d", takeInfo.take) + ".wav"
            val searchName = File(path, name)
            if (searchName != null && searchName.exists()) {
                //check if the names match up; exclude the path to get to them or the file extention
                if (extractFilename(searchName) == extractFilename(v)) {
                    continue
                }
            }
            if (!found) {
                println("Removing " + v.name)
                v.delete()
            }
        }
    }

    private fun extractFilename(a: File): String {
        if (a.isDirectory) {
            return ""
        }
        val nameWithExtention = a.name
        if (nameWithExtention.lastIndexOf('.') < 0 || nameWithExtention.lastIndexOf('.') > nameWithExtention.length) {
            return ""
        }
        val filename = nameWithExtention.substring(0, nameWithExtention.lastIndexOf('.'))
        return filename
    }


    fun configureLogger(minLogLevel: Int, logDir: File) {
        val logFile = File(logDir, "log.txt")
        try {
            logFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Logger.configure(logFile, Logger.Level.getLevel(minLogLevel))
        if (logFile.exists()) {
            Logger.w(this.toString(), "SUCCESS: Log file initialized.")
        } else {
            Logger.e(this.toString(), "ERROR: could not initialize log file.")
        }
    }

    companion object {

        val KEY_PREF_LOGGING_LEVEL = "logging_level"
        val PREF_DEFAULT_LOGGING_LEVEL = "1"
        val STACKTRACE_DIR = "stacktrace"

        val FIRST_REQUEST = 1
        val LANGUAGE_REQUEST = FIRST_REQUEST
        val PROJECT_REQUEST = FIRST_REQUEST + 1
        val BOOK_REQUEST = FIRST_REQUEST + 2
        val MODE_REQUEST = FIRST_REQUEST + 3
        val SOURCE_TEXT_REQUEST = FIRST_REQUEST + 4
        val SOURCE_REQUEST = FIRST_REQUEST + 5
        val PROJECT_WIZARD_REQUEST = FIRST_REQUEST + 6
    }
}
