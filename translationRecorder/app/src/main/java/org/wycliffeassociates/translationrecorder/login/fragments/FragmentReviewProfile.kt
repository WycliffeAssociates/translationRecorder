package org.wycliffeassociates.translationrecorder.login.fragments


import android.app.Fragment
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.fragment_review_profile.*
import org.wycliffeassociates.translationrecorder.MainMenu
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.SettingsPage.Settings
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import org.wycliffeassociates.translationrecorder.login.interfaces.OnRedoListener
import org.wycliffeassociates.translationrecorder.project.components.User
import java.io.File

class FragmentReviewProfile : Fragment() {

    companion object {
        fun newInstance(audio: File, hash: String, redo: OnRedoListener): FragmentReviewProfile {
            val fragment = FragmentReviewProfile()
            fragment.audio = audio
            fragment.hash = hash
            fragment.onRedoListener = redo
            return fragment
        }
    }

    private lateinit var audio: File
    private lateinit var hash: String
    private lateinit var onRedoListener: OnRedoListener

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
            val mainActivityIntent = Intent(activity, MainMenu::class.java)
            activity.startActivity(mainActivityIntent)
            activity.finish()
        }
    }

    private fun generateIdenticon(hash: String): Drawable {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }
}
