package it.progmob.passwordmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(private val passwords: List<Password>, val longClick: (Password) -> Unit, val onClick: (Password) -> Unit) :
    RecyclerView.Adapter<PasswordAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteNameView: TextView = itemView.findViewById(R.id.siteNamePassword)
        val usernameView: TextView = itemView.findViewById(R.id.usernamePassword)
        val passwordView: TextView = itemView.findViewById(R.id.password)
        val dateView: TextView = itemView.findViewById(R.id.expirationPasswordView)
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
        holder.passwordView.text = "Password: **********"
        holder.dateView.text = "Expiration Date: " + password.expirationDate
        holder.positionView.text = (position + 1).toString()

        var isPasswordHidden = true

        holder.itemView.setOnLongClickListener {
            longClick(password)
            true
        }
        holder.itemView.setOnClickListener {
            if (isPasswordHidden) {
                holder.passwordView.text = "Password: " + password.password
            } else {
                holder.passwordView.text = "Password: **********"
            }
            isPasswordHidden = !isPasswordHidden
            onClick(password)
        }
    }

    override fun getItemCount() = passwords.size

}

class PinAdapter(private val pins: List<Pin>, val longClick: (Pin) -> Unit, val onClick: (Pin) -> Unit) :
    RecyclerView.Adapter<PinAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionView: TextView = itemView.findViewById(R.id.descriptionPin)
        val pinView: TextView = itemView.findViewById(R.id.pin)
        val dateView: TextView = itemView.findViewById(R.id.expirationPinView)
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
        holder.pinView.text = "Pin: **********"
        holder.dateView.text = "Expiration Date: " + pin.expirationDate
        holder.positionView.text = (position + 1).toString()

        var isPinHidden = true

        holder.itemView.setOnLongClickListener {
            longClick(pin)
            true
        }
        holder.itemView.setOnClickListener {
            if (isPinHidden) {
                holder.pinView.text = "Pin: " + pin.password
            } else {
                holder.pinView.text = "Pin: **********"
            }
            isPinHidden = !isPinHidden
            onClick(pin)
        }
    }

    override fun getItemCount() = pins.size

}

class CCAdapter(private val cards: List<CreditCard>, val longClick: (CreditCard) -> Unit, val onClick: (CreditCard) -> Unit) :
    RecyclerView.Adapter<CCAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionView: TextView = itemView.findViewById(R.id.descriptionCC)
        val cardNumberView: TextView = itemView.findViewById(R.id.cardNumberCC)
        val securityCodeView: TextView = itemView.findViewById(R.id.securityCodeCC)
        val dateView: TextView = itemView.findViewById(R.id.expirationCreditCardView)
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
        holder.descriptionView.text = "Description: " + card.description
        holder.cardNumberView.text = "Number: **********"
        holder.securityCodeView.text = "Security Code: **********"
        holder.dateView.text = "Expiration Date: " + card.expirationDate
        holder.positionView.text = (position + 1).toString()

        var areFieldsHidden = true

        holder.itemView.setOnLongClickListener {
            longClick(card)
            true
        }
        holder.itemView.setOnClickListener {
            if (areFieldsHidden) {
                holder.cardNumberView.text = "Number: " + card.number
                holder.securityCodeView.text = "Security Code: " + card.securityCode
            } else {
                holder.cardNumberView.text = "Number: **********"
                holder.securityCodeView.text = "Security Code: **********"
            }
            areFieldsHidden = !areFieldsHidden
            onClick(card)
        }
    }

    override fun getItemCount() = cards.size

}

class UserAdapter(private val users: List<User>, val clickListener: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailUserView: TextView = itemView.findViewById(R.id.emailUser)
        val positionView: TextView = itemView.findViewById(R.id.numberUser)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = users[position]
        holder.emailUserView.text = user.email
        holder.positionView.text = (position + 1).toString()

        holder.itemView.setOnClickListener {
            clickListener(user)
        }
    }

    override fun getItemCount() = users.size

}