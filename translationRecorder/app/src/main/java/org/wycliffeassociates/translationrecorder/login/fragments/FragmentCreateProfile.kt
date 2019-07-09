package org.wycliffeassociates.translationrecorder.login.fragments

import android.app.Fragment
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_create_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.wycliffeassociates.translationrecorder.AudioVisualization.ActiveRecordingRenderer
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.Recording.RecordingQueues
import org.wycliffeassociates.translationrecorder.Recording.WavFileWriter
import org.wycliffeassociates.translationrecorder.Recording.WavRecorder
import org.wycliffeassociates.translationrecorder.Recording.fragments.FragmentRecordingWaveform
import org.wycliffeassociates.translationrecorder.login.interfaces.OnProfileCreatedListener
import org.wycliffeassociates.translationrecorder.login.utils.convertWavToMp4
import org.wycliffeassociates.translationrecorder.wav.WavFile
import java.io.File
import java.util.*

/**
 * Created by sarabiaj on 3/10/2018.
 */

class FragmentCreateProfile : Fragment() {

    companion object {
        fun newInstance(uploadDir: File, profileCreatedCallback: OnProfileCreatedListener): FragmentCreateProfile {
            val args = Bundle()
            val fragment = FragmentCreateProfile()
            fragment.arguments = args
            fragment.uploadDir = uploadDir
            uploadDir.mkdirs()
            fragment.userAudio = File(uploadDir, UUID.randomUUID().toString())
            fragment.userAudio.createNewFile()
            fragment.retainInstance = true
            fragment.profileCreatedCallback = profileCreatedCallback
            return fragment
        }
    }

    private lateinit var uploadDir: File
    private lateinit var userAudio: File
    private lateinit var hash: String
    private var profileCreatedCallback: OnProfileCreatedListener? = null

    private lateinit var mRecording: File
    private lateinit var mRecordingWaveform: FragmentRecordingWaveform
    private lateinit var mRenderer: ActiveRecordingRenderer

    private var isRecording = false
    private var mNewRecording: WavFile? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater?.inflate(R.layout.fragment_create_profile, container, false)
    }

    override fun onDestroy() {
        profileCreatedCallback = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecordingWaveform = FragmentRecordingWaveform.newInstance()
        btnRecord as Button
        btnRecord.setOnClickListener {
            (btnRecord.background as Animatable).start()
            if (btnRecord.isActivated.not()) {
                btnRecord.isActivated = true
                startRecording()
            }
        }
        fragmentManager.beginTransaction().add(R.id.waveform_view, mRecordingWaveform).commit()
        mRenderer = ActiveRecordingRenderer(null, null, mRecordingWaveform)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            outState.putSerializable("user_audio", userAudio)
        }
    }

    private fun startRecording() {
        if (!isRecording) {
            isRecording = true
            activity.stopService(Intent(activity, WavRecorder::class.java))
            RecordingQueues.clearQueues()
            mRecording = File.createTempFile(UUID.randomUUID().toString(), ".raw")
            mNewRecording = WavFile(
                    mRecording,
                    null
            )
            activity.startService(Intent(activity, WavRecorder::class.java))
            activity.startService(WavFileWriter.getIntent(activity, mNewRecording))
            mRenderer.listenForRecording(false)
            Handler(Looper.getMainLooper()).postDelayed({ stopRecording() }, 3000)
        }
    }

    private fun stopRecording() {
        //Stop recording, load the recorded file, and draw
        activity.stopService(Intent(activity, WavRecorder::class.java))
        RecordingQueues.pauseQueues()
        RecordingQueues.stopQueues(activity)
        isRecording = false
        btnRecord.isActivated = true
        convertAudio()
    }

    private fun convertAudio() {
        GlobalScope.launch(Dispatchers.Main) {
            if (userAudio.exists()) {
                userAudio.delete()
                userAudio.createNewFile()
            }
            hash = convertWavToMp4(mRecording, userAudio)
            profileCreatedCallback?.onProfileCreated(mNewRecording!!, userAudio, hash)
        }
    }
}
