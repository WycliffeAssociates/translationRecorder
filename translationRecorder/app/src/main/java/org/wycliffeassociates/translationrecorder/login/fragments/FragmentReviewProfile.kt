package org.wycliffeassociates.translationrecorder.login.fragments


import android.app.Fragment
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.media.AudioTrack
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.fragment_review_profile.*
import org.wycliffeassociates.translationrecorder.*
import org.wycliffeassociates.translationrecorder.AudioVisualization.WavVisualizer
import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp
import org.wycliffeassociates.translationrecorder.Playback.overlays.WaveformLayer
import org.wycliffeassociates.translationrecorder.Playback.player.WavPlayer
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import org.wycliffeassociates.translationrecorder.login.interfaces.OnRedoListener
import org.wycliffeassociates.translationrecorder.project.components.User
import org.wycliffeassociates.translationrecorder.wav.WavCue
import org.wycliffeassociates.translationrecorder.wav.WavFile
import java.io.File
import java.util.*

class FragmentReviewProfile : Fragment(), WaveformLayer.WaveformDrawDelegator {
    override fun onDrawWaveform(canvas: Canvas?, paint: Paint?) {
        if(mLayoutInitialized) {
            paint?.let {
                canvas?.drawLines(wavVis.getMinimap(canvas.height, canvas.width, mPlayer.absoluteDurationInFrames), paint)
            }
        }
    }

    companion object {
        fun newInstance(wav: WavFile, audio: File, hash: String, redo: OnRedoListener): FragmentReviewProfile {
            val fragment = FragmentReviewProfile()
            fragment.wav = wav
            fragment.audio = audio
            fragment.hash = hash
            fragment.onRedoListener = redo
            return fragment
        }
    }

    private lateinit var wav: WavFile
    private lateinit var audio: File
    private lateinit var hash: String
    private lateinit var onRedoListener: OnRedoListener
    private lateinit var mWaveformLayer: WaveformLayer
    private lateinit var wavVis: WavVisualizer
    private lateinit var mPlayer: WavPlayer
    private var mLayoutInitialized = false
    private lateinit var audioTrack: AudioTrack
    private var trackBufferSize: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater?.inflate(R.layout.fragment_review_profile, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icon_hash as ImageView
        renderIdenticon(hash, icon_hash)
        btnRedo as Button
        btnRedo.setOnClickListener {
            onRedoListener?.let {
                audio.delete()
                audio.createNewFile()
                onRedoListener.onRedo()
            }
        }
        btnYes as Button
        btnYes.setOnClickListener {
            val profilesDir = File(Environment.getExternalStorageDirectory(), "BTTRecorder/Profiles/")
            if (profilesDir.exists().not()) {
                profilesDir.mkdirs()
            }
            val newAudio = File(profilesDir.absolutePath + "/" + audio.name)
            audio.renameTo(newAudio)
            audio = newAudio
            val user = User(audio, hash)
            val db = (activity.application as TranslationRecorderApp).database
            db.addUser(user)
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            pref.edit().putInt(Settings.KEY_PROFILE, user.id).apply()
            val mainActivityIntent = Intent(activity, MainMenu::class.java)
            activity.startActivity(mainActivityIntent)
            activity.finish()
        }
        waveform_frame as FrameLayout
        mWaveformLayer = WaveformLayer.newInstance(activity, this)
        waveform_frame.addView(mWaveformLayer)
        lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            initializeRenderer()
            mLayoutInitialized = true
            mWaveformLayer.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        }
        mWaveformLayer.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        btn_play as AppCompatImageButton
        btn_play.setOnClickListener {
            showPauseButton()
            mPlayer.play()
        }
        btn_pause as AppCompatImageButton
        btn_pause.setOnClickListener {
            showPlayButton()
            mPlayer.pause()
        }
        audioTrack = (activity.application as TranslationRecorderApp).audioTrack
        trackBufferSize = (activity.application as TranslationRecorderApp).trackBufferSize
    }

    private fun renderIdenticon(hash: String, view: ImageView) {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        Sharp.loadString(svg).into(view)
    }

    private fun initializeRenderer() {
        showPlayButton()
        wav.overwriteHeaderData()
        val wavFileLoader = WavFileLoader(wav, activity)
        val numThreads = 4
        val uncompressed = wavFileLoader.mapAndGetAudioBuffer()
        wavVis = WavVisualizer(
                uncompressed,
                null,
                numThreads,
                waveform_frame.width,
                waveform_frame.height,
                waveform_frame.width,
                CutOp()
        )
        mPlayer = WavPlayer(audioTrack, trackBufferSize, uncompressed, CutOp(), LinkedList<WavCue>())
        mPlayer.setOnCompleteListener {
            showPlayButton()
        }
    }

    private fun showPauseButton() {
        Utils.swapViews(arrayOf(btn_pause), arrayOf(btn_play))
    }

    private fun showPlayButton() {
        Utils.swapViews(arrayOf(btn_play), arrayOf(btn_pause))
    }
}
