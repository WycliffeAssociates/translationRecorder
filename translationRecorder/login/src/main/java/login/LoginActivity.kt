package login

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import login.fragments.FragmentCreateProfile
import org.wycliffeassociates.translationrecorder.login.R
import java.io.File

/**
 * Created by sarabiaj on 3/9/2018.
 */

class LoginActivity : AppCompatActivity() {

    private var mFragmentCreateProfile: FragmentCreateProfile? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initializeFragments()
        addFragments()
    }

    private fun initializeFragments() {
        mFragmentCreateProfile = FragmentCreateProfile.newInstance(File(externalCacheDir, "profiles"))
    }

    private fun addFragments() {
        val fm = fragmentManager
        fm.beginTransaction()
                .add(R.id.fragment_container, mFragmentCreateProfile)
                .commit()

    }


}
