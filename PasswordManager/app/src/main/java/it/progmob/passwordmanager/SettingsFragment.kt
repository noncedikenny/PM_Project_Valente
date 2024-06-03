package it.progmob.passwordmanager

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.progmob.passwordmanager.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    //binding connected to the specific layout of the fragment
    private lateinit var binding: SettingsFragmentBinding

    // Use of viewModel among fragments to share data
    private val viewModel : ManagerViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = "Settings"
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val viewInflated: View = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_layout, null, false)

            dialogBuilder.setView(viewInflated)
            dialogBuilder.setTitle("You're going to reset everything, are you sure?")
            dialogBuilder.setCancelable(false)

            // Buttons
            dialogBuilder.setPositiveButton("Submit") { _, _ -> }
            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val db = Firebase.firestore

                val userRef = viewModel.userID?.let { it1 -> db.collection("users").document(it1) }

                userRef?.collection("Passwords")?.get()
                    ?.addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                        }
                    };
                userRef?.collection("Pins")?.get()
                    ?.addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                        }
                    };
                userRef?.collection("CreditCards")?.get()
                    ?.addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                        }
                    };

                viewModel.reset()

                alertDialog.dismiss()

                Toast.makeText(requireContext(), "Everything has been deleted correctly.", Toast.LENGTH_LONG).show()
            }
        }

        binding.logoutButton.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    Toast.makeText(requireContext(), "Logged out.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    startActivity(intent)
                }
        }
    }
}