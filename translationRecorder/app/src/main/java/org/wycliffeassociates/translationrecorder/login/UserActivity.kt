package org.wycliffeassociates.translationrecorder.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.wycliffeassociates.translationrecorder.R

/**
 * Created by sarabiaj on 3/9/2018.
 */

class UserActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

}