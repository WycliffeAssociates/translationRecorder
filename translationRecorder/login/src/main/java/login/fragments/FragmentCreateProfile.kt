package login.fragments

import android.app.Fragment
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.fragment_create_profile.*
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
                btnRecord.visibility = View.INVISIBLE
            }
            else {
                btnRecord.isActivated = true
                startRecording()
            }
        }
        savedInstanceState?.let {
            userAudio = savedInstanceState.getSerializable("user_audio") as File
            if (userAudio.length() > 0) {
                var icon = generateIdenticon()
                identicon_view.setImageDrawable(icon)
                btnRecord.visibility = View.GONE
                identicon_view.postInvalidate()
            }
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
        var icon = generateIdenticon()
        updateIdenticonView(icon)
    }

    private fun generateIdenticon(): Drawable {
        hash = String(Hex.encodeHex(DigestUtils.md5(FileInputStream(userAudio))))
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }

    private fun updateIdenticonView(identicon: Drawable) {
        identicon_view.setImageDrawable(identicon)
        identicon_view.postInvalidate()
    }
}
