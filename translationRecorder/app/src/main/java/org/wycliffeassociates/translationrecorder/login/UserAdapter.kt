package login.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.user_list_item.view.*
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.login.LoginActivity


/**
 * Created by Dilip Maharjan on 05/01/2018
 */
class UserAdapter(private val context: Context, private val users: ArrayList<Drawable>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return users.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        if (position == 0) {
            holder?.newUserTxt?.visibility = View.VISIBLE
            holder?.identicon?.setImageDrawable(user)
            holder?.playIcon?.visibility = View.INVISIBLE
            holder?.identicon.rootView.setOnClickListener {
                it.context.startActivity(Intent(it.context, LoginActivity::class.java))
                (it.context as Activity).finish()
            }
        } else {
            holder?.identicon?.setImageDrawable(user)
            holder?.identicon.setOnClickListener(View.OnClickListener {
                Toast.makeText(it.context, "Identicon $position", Toast.LENGTH_LONG).show()
            })
            holder?.playIcon.setOnClickListener(View.OnClickListener {
                it.isActivated = !it.isActivated
                Toast.makeText(it.context, "PlayIcon", Toast.LENGTH_LONG).show()
            })
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val identicon = view.identicon
        val playIcon = view.playIcon
        val newUserTxt = view.new_user_txt
    }

}