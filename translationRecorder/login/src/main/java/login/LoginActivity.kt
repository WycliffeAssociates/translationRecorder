package login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import login.fragments.FragmentCreateProfile
import login.utils.fragmentTransaction
import org.wycliffeassociates.translationrecorder.login.R
import java.io.File

/**
 * Created by sarabiaj on 3/9/2018.
 */

class LoginActivity : AppCompatActivity() {

    private lateinit var mFragmentCreateProfile: FragmentCreateProfile

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (savedInstanceState == null) {
            initializeFragments()
            addFragments()
        }

    }

    override fun onStart() {
        super.onStart()
        startActivity(Intent(this, UserActivity::class.java))
    }

    private fun initializeFragments() {
        mFragmentCreateProfile = FragmentCreateProfile.newInstance(File(externalCacheDir, "profiles"))
    }

    private fun addFragments() {
        fragmentTransaction(fragmentManager, mFragmentCreateProfile, "create_profile", false, "")
    }

}
