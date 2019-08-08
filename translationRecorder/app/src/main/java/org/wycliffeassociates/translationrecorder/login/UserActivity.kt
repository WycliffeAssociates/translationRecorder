package org.wycliffeassociates.translationrecorder.login

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixplicity.sharp.Sharp
import jdenticon.Jdenticon
import kotlinx.android.synthetic.main.activity_user.*
import login.adapters.UserAdapter
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper
import org.wycliffeassociates.translationrecorder.project.components.User

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

    private fun userList(): List<Pair<User, Drawable>> {
        val db = (application as TranslationRecorderApp).database
        var userList = arrayListOf<Pair<User, Drawable>>()
        val newEmptyUser = User(0, null, null)
        userList.add(Pair(newEmptyUser, resources.getDrawable(R.drawable.ic_person_add_black_48dp)))
        val users = db.allUsers
        for (user in users) {
            var identicon = generateIdenticon(user.hash)
            userList.add(Pair(user, identicon))
        }
        return userList
    }

    private fun generateIdenticon(hash: String): Drawable {
        val svg = Jdenticon.toSvg(hash, 512, 0f)
        return Sharp.loadString(svg).drawable
    }

}

