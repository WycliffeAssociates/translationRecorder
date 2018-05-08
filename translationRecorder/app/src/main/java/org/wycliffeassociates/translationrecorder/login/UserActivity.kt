package org.wycliffeassociates.translationrecorder.login

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import login.adapters.UserAdapter
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import kotlinx.android.synthetic.main.activity_user.*
/**
 * Created by sarabiaj on 3/9/2018.
 */

class UserActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)


        val orientation = resources.configuration.orientation
        var layoutManager = GridLayoutManager(this, 4) as RecyclerView.LayoutManager
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = GridLayoutManager(this, 3)
        }
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        val adapter = UserAdapter(this, userList())
        recycler.adapter = adapter
    }

    private fun userList(): ArrayList<Drawable> {
        val db = ProjectDatabaseHelper(this)
        var userList = arrayListOf<Drawable>()
        val users = db.allUsers
        for (user in users) {
            var identicon = generateIdenticon(user.hash)
            userList.add(identicon)
        }
        return userList
    }

    private fun generateIdenticon(hash: String): Drawable {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }

}

