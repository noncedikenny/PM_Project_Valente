package it.progmob.passwordmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(private val passwords: ArrayList<Password>) :
    RecyclerView.Adapter<PasswordAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteNameView: TextView = itemView.findViewById(R.id.siteName)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val passwordView: TextView = itemView.findViewById(R.id.password)
        val positionView: TextView = itemView.findViewById(R.id.number)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PasswordAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordAdapter.ViewHolder, position: Int) {
        val password = passwords[position]
        holder.siteNameView.text = "Site Name: " + password.siteName
        holder.usernameView.text = "Username: " + password.username
        holder.passwordView.text = "Password: " + password.password
        holder.positionView.text = (position + 1).toString()
    }

    override fun getItemCount() = passwords.size

}