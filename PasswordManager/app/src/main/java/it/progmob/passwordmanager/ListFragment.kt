package it.progmob.passwordmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.list_fragment, container, false)
    }

    val passwordList: ArrayList<Password> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addPassword: Button = view.findViewById(R.id.addPassword)

        val adapter = PasswordAdapter(passwordList)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        addPassword.setOnClickListener {
            // Setup builder to build the popup
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val viewInflated: View = LayoutInflater.from(requireContext()).inflate(R.layout.add_popup_layout, requireView() as ViewGroup, false)
            dialogBuilder.setView(viewInflated)
            dialogBuilder.setTitle("Add a password")
            dialogBuilder.setCancelable(false)

            // Buttons
            dialogBuilder.setPositiveButton("Submit") { _, _ -> }
            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            dialogBuilder.setNeutralButton("Generate Password") {_, _ ->}

            // Create popup
            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                // Bind input
                val siteNameItem: EditText = viewInflated.findViewById(R.id.siteNameInput)
                val usernameItem: EditText = viewInflated.findViewById(R.id.usernameInput)
                val passwordItem: EditText = viewInflated.findViewById(R.id.passwordInput)
                val passwordRegex = Regex("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}")

                if (siteNameItem.text.toString().isEmpty() || usernameItem.text.toString().isEmpty() || passwordItem.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Every field must be filled.", Toast.LENGTH_SHORT).show()
                }
                else if (!passwordRegex.matches(passwordItem.text.toString())) {
                    Toast.makeText(requireContext(), "Password's weak.", Toast.LENGTH_SHORT).show()
                }
                else {
                    val newPassword = Password(siteNameItem.text.toString(), usernameItem.text.toString(), passwordItem.text.toString())
                    passwordList.add(newPassword)
                    val adapter2 = recyclerView.adapter as? PasswordAdapter
                    adapter2?.notifyDataSetChanged()
                    alertDialog.dismiss()
                }
            }

            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                val passwordItem: EditText = viewInflated.findViewById(R.id.passwordInput)
                val randomPassword = generateRandomPassword(12)
                passwordItem.setText(randomPassword)
            }
        }
    }
}

fun generateRandomPassword(length: Int): String {
    val lowercaseChars = ('a'..'z').toList()
    val uppercaseChars = ('A'..'Z').toList()
    val digitChars = ('0'..'9').toList()
    val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '-', '=', '[', ']', '{', '}', ';', ':', '\'', '"', '\\', '|', ',', '.', '<', '>', '/', '?')

    val requiredChars = listOf(lowercaseChars, uppercaseChars, digitChars, specialChars).map { it.random() }
    val remainingChars = (length - requiredChars.size).coerceAtLeast(0)

    val allChars = lowercaseChars + uppercaseChars + digitChars + specialChars

    val randomChars = (1..remainingChars)
        .map { allChars.random() }

    val shuffledPassword = (requiredChars + randomChars).shuffled()

    return shuffledPassword.joinToString("")
}
