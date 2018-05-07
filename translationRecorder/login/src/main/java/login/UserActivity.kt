package login

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_user.*
import login.adapters.UserCardAdapter
import login.models.UserCard
import org.wycliffeassociates.translationrecorder.login.R

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
        val adapter = UserCardAdapter(this, userList())
        recycler.adapter = adapter
    }

    private fun userList(): ArrayList<UserCard> {
        return arrayListOf(
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp),
                UserCard(R.drawable.ic_person_add_black_48dp)
        )
    }

}