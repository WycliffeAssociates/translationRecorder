package login.fragments


import android.app.Fragment
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.fragment_redo_yes.*
import org.wycliffeassociates.translationrecorder.login.R

class FragmentRedoYes : Fragment(), View.OnClickListener {

    private val TAG = "FRAGMENTREDOYES"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_redo_yes, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle: Bundle? = this.arguments
        if (bundle != null) {
            var iconHash = bundle.getString("data")
            val icon = generateIdenticon(iconHash)
            icon_hash.setImageDrawable(icon)
        }
        btnRedo.setOnClickListener(this)
        btnYes.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnRedo -> activity.onBackPressed()
            R.id.btnYes -> "test"
        }
    }

    private fun generateIdenticon(hash: String): Drawable {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }
}
