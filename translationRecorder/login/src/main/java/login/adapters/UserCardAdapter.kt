package login.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.user_list_item.view.*
import login.models.UserCard
import org.wycliffeassociates.translationrecorder.login.R

/**
 * Created by Dilip Maharjan on 05/01/2018
 */
class UserCardAdapter(private val context: Context, private val users: ArrayList<UserCard>) : RecyclerView.Adapter<UserCardAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder?.identicon?.setImageResource(user.identicon)
        holder?.playIcon?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
        holder?.identicon.setOnClickListener(View.OnClickListener {
            Toast.makeText(it.context, "Identicon $position", Toast.LENGTH_LONG).show()
        })
        holder?.playIcon.setOnClickListener(View.OnClickListener {
            Toast.makeText(it.context, "PlayIcon", Toast.LENGTH_LONG).show()
        })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val identicon = view.identicon
        val playIcon = view.playIcon
    }

}