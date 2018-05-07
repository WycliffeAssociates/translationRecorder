package login.fragments

import android.app.Fragment
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_create_profile.*
import login.utils.fragmentTransaction
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.wycliffeassociates.translationrecorder.login.R
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by sarabiaj on 3/10/2018.
 */

class FragmentCreateProfile : Fragment() {

    companion object {
        fun newInstance(uploadDir: File): FragmentCreateProfile {
            val args = Bundle()
            val fragment = FragmentCreateProfile()
            fragment.arguments = args
            fragment.uploadDir = uploadDir
            uploadDir.mkdirs()
            fragment.userAudio = File(uploadDir, UUID.randomUUID().toString())
            fragment.userAudio.createNewFile()
            fragment.retainInstance = true
            return fragment
        }
    }

    private lateinit var uploadDir: File
    private lateinit var userAudio: File
    private lateinit var hash: String

    private val mMediaRecorder = MediaRecorder()

    private fun configureRecorder() {
        //Initial State
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)

        //Initialized Sate
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        //DataSourceConfigured State
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder.setOutputFile(userAudio.absolutePath)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater?.inflate(R.layout.fragment_create_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnRecord.setOnClickListener {
            if (btnRecord.isActivated) {
                btnRecord.isActivated = false
                stopRecording()
            } else {
                btnRecord.isActivated = true
                startRecording()
            }
            fragmentTransaction(fragmentManager, FragmentRedoYes(), "redoyes", true, getHash())

        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            outState.putSerializable("user_audio", userAudio)
        }
    }

    private fun startRecording() {
        try {
            configureRecorder()
            mMediaRecorder.prepare()
            mMediaRecorder.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mMediaRecorder.stop()
    }

    private fun getHash(): String {
        return String(Hex.encodeHex(DigestUtils.md5(FileInputStream(userAudio))))
    }
}
