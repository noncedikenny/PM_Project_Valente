package it.progmob.passwordmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(private val passwords: List<Password>) :
    RecyclerView.Adapter<PasswordAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteNameView: TextView = itemView.findViewById(R.id.siteNamePassword)
        val usernameView: TextView = itemView.findViewById(R.id.usernamePassword)
        val passwordView: TextView = itemView.findViewById(R.id.password)
        val positionView: TextView = itemView.findViewById(R.id.numberPassword)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PasswordAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.password_item_layout, parent, false)
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

class PinAdapter(private val pins: List<Pin>) :
    RecyclerView.Adapter<PinAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionView: TextView = itemView.findViewById(R.id.descriptionPin)
        val pinView: TextView = itemView.findViewById(R.id.pin)
        val positionView: TextView = itemView.findViewById(R.id.numberPin)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PinAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pin_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinAdapter.ViewHolder, position: Int) {
        val pin = pins[position]
        holder.descriptionView.text = "Description: " + pin.description
        holder.pinView.text = "Pin: " + pin.password
        holder.positionView.text = (position + 1).toString()
    }

    override fun getItemCount() = pins.size

}

class CCAdapter(private val cards: List<CreditCard>) :
    RecyclerView.Adapter<CCAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardNumberView: TextView = itemView.findViewById(R.id.cardNumberCC)
        val securityCodeView: TextView = itemView.findViewById(R.id.securityCodeCC)
        val positionView: TextView = itemView.findViewById(R.id.numberCC)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CCAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cc_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CCAdapter.ViewHolder, position: Int) {
        val card = cards[position]
        holder.cardNumberView.text = "Description: " + card.number
        holder.securityCodeView.text = "Password: " + card.securityCode
        holder.positionView.text = (position + 1).toString()
    }

    override fun getItemCount() = cards.size

}