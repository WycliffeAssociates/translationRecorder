package org.wycliffeassociates.translationrecorder.Recording

import android.app.DialogFragment
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

import com.door43.tools.reporting.Logger

import org.wycliffeassociates.translationrecorder.AudioVisualization.ActiveRecordingRenderer
import org.wycliffeassociates.translationrecorder.FilesPage.ExitDialog
import org.wycliffeassociates.translationrecorder.Playback.PlaybackActivity
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingControls
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingFileBar
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingWaveform
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentSourceAudio
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentVolumeBar
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin
import org.wycliffeassociates.translationrecorder.data.model.Project
import org.wycliffeassociates.translationrecorder.wav.WavFile
import org.wycliffeassociates.translationrecorder.wav.WavMetadata

import java.util.HashMap

import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin.DEFAULT_CHAPTER
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin.DEFAULT_UNIT
import org.wycliffeassociates.translationrecorder.data.repository.BookRepository
import org.wycliffeassociates.translationrecorder.data.repository.LanguageRepository
import org.wycliffeassociates.translationrecorder.data.repository.ProjectRepository
import org.wycliffeassociates.translationrecorder.persistence.repository.RoomDb
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils
import java.io.Serializable

/**
 * Created by sarabiaj on 2/20/2017.
 */

class RecordingActivity : AppCompatActivity(),
        FragmentRecordingControls.RecordingControlCallback,
        InsertTaskFragment.Insert,
        FragmentRecordingFileBar.OnUnitChangedListener,
        ExitDialog.DeleteFileCallback
{

    private var mProject: Project? = null
    private var mInitialChapter: Int = 0
    private var mInitialUnit: Int = 0
    private var mLoadedWav: WavFile? = null
    private var mInsertLocation: Int = 0
    private var mInsertMode: Boolean = false
    private var isChunkMode: Boolean = false
    private var mInsertTaskFragment: InsertTaskFragment? = null
    private var mInserting: Boolean = false
    private var mProgressDialog: ProgressDialog? = null

    private lateinit var roomDb: RoomDb
    private lateinit var takeDb: TakeRepository

    //Fragments
    private var mFragmentHolder: HashMap<Int, Fragment>? = null
    private var mFragmentRecordingFileBar: FragmentRecordingFileBar? = null
    private var mFragmentVolumeBar: FragmentVolumeBar? = null
    private var mFragmentRecordingControls: FragmentRecordingControls? = null
    private var mFragmentSourceAudio: FragmentSourceAudio? = null
    private var mFragmentRecordingWaveform: FragmentRecordingWaveform? = null

    private var mRecordingRenderer: ActiveRecordingRenderer? = null

    private var isRecording = false
    private var onlyVolumeTest = true
    private var mNewRecording: WavFile? = null
    private var isPausedRecording: Boolean = false
    private var isSaved: Boolean = false
    private var hasStartedRecording = false

    private var mContributor: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_recording_screen)
        initialize(intent)
        initializeTaskFragment(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        onlyVolumeTest = true
        val volumeTestIntent = Intent(this, WavRecorder::class.java)
        volumeTestIntent.putExtra(WavRecorder.KEY_VOLUME_TEST, onlyVolumeTest)
        startService(volumeTestIntent)
        mRecordingRenderer!!.listenForRecording(onlyVolumeTest)
    }

    public override fun onPause() {
        Logger.w(this.toString(), "Recording screen onPauseRecording")
        super.onPause()
        if (isRecording) {
            isRecording = false
            stopService(Intent(this, WavRecorder::class.java))
            RecordingQueues.stopQueues(this)
        } else if (isPausedRecording) {
            RecordingQueues.stopQueues(this)
        } else if (!hasStartedRecording) {
            stopService(Intent(this, WavRecorder::class.java))
            RecordingQueues.stopVolumeTest()
        }
        finish()
    }

    override fun onBackPressed() {
        Logger.w(this.toString(), "User pressed back")
        if (!isSaved && hasStartedRecording) {
            val exitDialog = ExitDialog.Build(
                    this,
                    DialogFragment.STYLE_NORMAL,
                    false,
                    false,
                    mNewRecording!!.file
            )
            exitDialog.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDeleteRecording() {
        isRecording = false
        isPausedRecording = false
        stopService(Intent(this, WavRecorder::class.java))
        RecordingQueues.stopQueues(this)
        RecordingQueues.clearQueues()
        mNewRecording!!.file.delete()
        //originally called from a backpress, so finish by calling super
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initialize(intent: Intent) {
        roomDb = RoomDb.getInstance(this)


        initializeFromSettings()
        parseIntent(intent)
        getCurrentUser()
        initializeFragments()
        attachFragments()
        mRecordingRenderer = ActiveRecordingRenderer(
                mFragmentRecordingControls,
                mFragmentVolumeBar,
                mFragmentRecordingWaveform
        )
    }

    private fun initializeFromSettings() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        mInitialChapter = pref.getInt(Settings.KEY_PREF_CHAPTER, DEFAULT_CHAPTER)
        mInitialUnit = pref.getInt(Settings.KEY_PREF_CHUNK, DEFAULT_UNIT)
    }

    private fun getCurrentUser() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        mContributor = pref.getString(Settings.KEY_PROFILE, "")
    }

    private fun initializeFragments() {
        //initialize fragments
        mFragmentRecordingControls = FragmentRecordingControls.newInstance(
                if (mInsertMode)
                    FragmentRecordingControls.Mode.INSERT_MODE
                else
                    FragmentRecordingControls.Mode.RECORDING_MODE
        )
        mFragmentSourceAudio = FragmentSourceAudio.newInstance()
        mFragmentRecordingFileBar = FragmentRecordingFileBar.newInstance(
                mProject,
                mInitialChapter,
                mInitialUnit,
                if (mInsertMode)
                    FragmentRecordingControls.Mode.INSERT_MODE
                else
                    FragmentRecordingControls.Mode.RECORDING_MODE
        )

        mFragmentVolumeBar = FragmentVolumeBar.newInstance()
        mFragmentRecordingWaveform = FragmentRecordingWaveform.newInstance()

        //add fragments to map
        mFragmentHolder = HashMap()
        mFragmentHolder!![R.id.fragment_recording_controls_holder] = mFragmentRecordingControls

        mFragmentHolder!![R.id.fragment_source_audio_holder] = mFragmentSourceAudio
        mFragmentHolder!![R.id.fragment_recording_file_bar_holder] = mFragmentRecordingFileBar
        mFragmentHolder!![R.id.fragment_volume_bar_holder] = mFragmentVolumeBar
        mFragmentHolder!![R.id.fragment_recording_waveform_holder] = mFragmentRecordingWaveform
    }

    private fun attachFragments() {
        val fm = fragmentManager
        val ft = fm.beginTransaction()
        val entrySet = mFragmentHolder!!.entries
        for ((key, value) in entrySet) {
            ft.add(key, value)
        }
        ft.commit()
    }

    private fun parseIntent(intent: Intent) {
        mProject = intent.getSerializableExtra(KEY_PROJECT) as Project
        //if a chapter and unit does not come from an intent, fallback to the ones from settings
        mInitialChapter = intent.getIntExtra(KEY_CHAPTER, mInitialChapter)
        mInitialUnit = intent.getIntExtra(KEY_UNIT, mInitialUnit)
        if (intent.hasExtra(KEY_WAV_FILE)) {
            mLoadedWav = intent.getParcelableExtra(KEY_WAV_FILE)
        }
        if (intent.hasExtra(KEY_INSERT_LOCATION)) {
            mInsertLocation = intent.getIntExtra(KEY_INSERT_LOCATION, 0)
            mInsertMode = true
        }
        isChunkMode = mProject!!.getModeType() === ChunkPlugin.TYPE.MULTI
    }


    private fun initializeTaskFragment(savedInstanceState: Bundle?) {
        val fm = fragmentManager
        mInsertTaskFragment = fm.findFragmentByTag(TAG_INSERT_TASK_FRAGMENT) as InsertTaskFragment
        if (mInsertTaskFragment == null) {
            mInsertTaskFragment = InsertTaskFragment()
            fm.beginTransaction().add(mInsertTaskFragment, TAG_INSERT_TASK_FRAGMENT).commit()
            fm.executePendingTransactions()
        }
        if (savedInstanceState != null) {
            mInserting = savedInstanceState.getBoolean(STATE_INSERTING, false)
            if (mInserting) {
                displayProgressDialog()
            }
        }
    }

    private fun displayProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setTitle("Inserting recording")
        mProgressDialog!!.setMessage("Please wait...")
        mProgressDialog!!.isIndeterminate = true
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    override fun onStartRecording() {
        hasStartedRecording = true
        mFragmentSourceAudio!!.disableSourceAudio()
        mFragmentRecordingFileBar!!.disablePickers()
        onlyVolumeTest = false
        isRecording = true
        stopService(Intent(this, WavRecorder::class.java))
        if (!isPausedRecording) {
            RecordingQueues.stopVolumeTest()
            isSaved = false
            RecordingQueues.clearQueues()
            val startVerse = mFragmentRecordingFileBar!!.startVerse
            val endVerse = mFragmentRecordingFileBar!!.endVerse
            val file = ProjectFileUtils.createFile(
                    mProject,
                    mFragmentRecordingFileBar!!.chapter,
                    Integer.parseInt(startVerse),
                    Integer.parseInt(endVerse)
            )
            mNewRecording = WavFile(
                    file,
                    WavMetadata(
                            mProject,
                            mContributor,
                            mFragmentRecordingFileBar!!.chapter.toString(),
                            startVerse,
                            endVerse
                    )
            )
            startService(Intent(this, WavRecorder::class.java))
            startService(WavFileWriter.getIntent(this, mNewRecording))
            mRecordingRenderer!!.listenForRecording(false)
        } else {
            isPausedRecording = false
            startService(Intent(this, WavRecorder::class.java))
        }
    }

    override fun onPauseRecording() {
        isPausedRecording = true
        isRecording = false
        stopService(Intent(this, WavRecorder::class.java))
        RecordingQueues.pauseQueues()
        Logger.w(this.toString(), "Pausing recording")
    }

    override fun onStopRecording() {
        //Stop recording, load the recorded file, and draw
        stopService(Intent(this, WavRecorder::class.java))
        val start = System.currentTimeMillis()
        Logger.w(this.toString(), "Stopping recording")
        RecordingQueues.stopQueues(this)
        Logger.w(
                this.toString(),
                "SUCCESS: exited queues, took "
                        + (System.currentTimeMillis() - start)
                        + " to finish writing"
        )
        isRecording = false
        isPausedRecording = false
        addTakeToDb()
        mNewRecording!!.parseHeader()
        saveLocationToPreferences()
        if (mInsertMode) {
            finalizeInsert(mLoadedWav, mNewRecording, mInsertLocation)
        } else {
            val intent = PlaybackActivity.getPlaybackIntent(
                    this,
                    mNewRecording,
                    mProject,
                    mFragmentRecordingFileBar!!.chapter,
                    mFragmentRecordingFileBar!!.unit
            )
            startActivity(intent)
            this.finish()
        }
    }

    private fun saveLocationToPreferences() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.edit().putInt(Settings.KEY_PREF_CHAPTER, mFragmentRecordingFileBar!!.chapter).commit()
        pref.edit().putInt(Settings.KEY_PREF_CHUNK, mFragmentRecordingFileBar!!.unit).commit()
    }

    private fun addTakeToDb() {

        val ppm = mProject!!.getPatternMatcher()
        ppm.match(mNewRecording!!.file)

        db.addTake(
                ppm.takeInfo,
                mNewRecording!!.file.name,
                mNewRecording!!.metadata.modeSlug,
                mNewRecording!!.file.lastModified(),
                0
        )
    }

    private fun finalizeInsert(base: WavFile?, insertClip: WavFile, insertFrame: Int) {
        // need to reparse the sizes after recording
        // updates to the object aren't reflected due to parceling to the writing service
        mNewRecording!!.parseHeader()
        mLoadedWav!!.parseHeader()
        mInserting = true
        displayProgressDialog()
        writeInsert(base, insertClip, insertFrame)
    }

    override fun writeInsert(base: WavFile?, insertClip: WavFile, insertFrame: Int) {
        mInsertTaskFragment!!.writeInsert(base, insertClip, insertFrame)
    }

    fun insertCallback(result: WavFile) {
        mInserting = false
        try {
            mProgressDialog!!.dismiss()
        } catch (e: IllegalArgumentException) {
            Logger.e(this.toString(), "Tried to dismiss insert progress dialog", e)
        }

        val intent = PlaybackActivity.getPlaybackIntent(
                this,
                result,
                mProject,
                mFragmentRecordingFileBar!!.chapter,
                mFragmentRecordingFileBar!!.unit
        )
        startActivity(intent)
        this.finish()
    }

    override fun onUnitChanged(project: Project, fileName: String, chapter: Int) {
        mFragmentSourceAudio!!.resetSourceAudio(project, fileName, chapter)
    }

    companion object {

        val KEY_PROJECT = "key_project"
        val KEY_WAV_FILE = "key_wav_file"
        val KEY_CHAPTER = "key_chapter"
        val KEY_UNIT = "key_unit"
        val KEY_INSERT_LOCATION = "key_insert_location"
        private val TAG_INSERT_TASK_FRAGMENT = "insert_task_fragment"
        private val STATE_INSERTING = "state_inserting"

        fun getInsertIntent(
                ctx: Context,
                project: Project,
                wavFile: WavFile,
                chapter: Int,
                unit: Int,
                locationMs: Int
        ): Intent {
            Logger.w("RecordingActivity", "Creating Insert Intent")
            val intent = getRerecordIntent(ctx, project, wavFile, chapter, unit)
            intent.putExtra(KEY_INSERT_LOCATION, locationMs)
            return intent
        }

        fun getNewRecordingIntent(
                ctx: Context,
                project: Project,
                chapter: Int,
                unit: Int
        ): Intent {
            Logger.w("RecordingActivity", "Creating New Recording Intent")
            val intent = Intent(ctx, RecordingActivity::class.java)
            intent.putExtra(KEY_PROJECT, project as Serializable)
            intent.putExtra(KEY_CHAPTER, chapter)
            intent.putExtra(KEY_UNIT, unit)
            return intent
        }

        fun getRerecordIntent(
                ctx: Context,
                project: Project,
                wavFile: WavFile,
                chapter: Int,
                unit: Int
        ): Intent {
            Logger.w("RecordingActivity", "Creating Rerecord Intent")
            val intent = getNewRecordingIntent(ctx, project, chapter, unit)
            intent.putExtra(KEY_WAV_FILE, wavFile)
            return intent
        }
    }
}
