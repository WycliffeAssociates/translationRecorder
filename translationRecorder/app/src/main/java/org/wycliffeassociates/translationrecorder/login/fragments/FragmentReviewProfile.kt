package org.wycliffeassociates.translationrecorder.login.fragments


import android.app.Fragment
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.AppCompatImageButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.fragment_review_profile.*
import org.wycliffeassociates.translationrecorder.AudioVisualization.WavVisualizer
import org.wycliffeassociates.translationrecorder.MainMenu
import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp
import org.wycliffeassociates.translationrecorder.Playback.overlays.WaveformLayer
import org.wycliffeassociates.translationrecorder.Playback.player.WavPlayer
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.WavFileLoader
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import org.wycliffeassociates.translationrecorder.login.UserActivity
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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater?.inflate(R.layout.fragment_review_profile, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val icon = generateIdenticon(hash)
        icon_hash as ImageView
        icon_hash.setImageDrawable(icon)
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
            val user = User(audio, hash)
            val db = ProjectDatabaseHelper(activity)
            db.addUser(user)
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            pref.edit().putString(Settings.KEY_PROFILE, user.toString()).apply()
            val mainActivityIntent = Intent(activity, UserActivity::class.java)
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
            if (!mPlayer.isPlaying) {
                mPlayer.play()
            }
        }
    }

    private fun generateIdenticon(hash: String): Drawable {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }

    private fun initializeRenderer() {
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
        mPlayer = WavPlayer(uncompressed, CutOp(), LinkedList<WavCue>())
    }
}
