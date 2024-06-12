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
import com.google.firebase.auth.FirebaseAuth
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

        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore

        var isOp = false

        db.collection("users").document(user!!.uid).get()
            .addOnSuccessListener { document ->
                val userRole = document.getString("role")
                if(userRole == "user") isOp = false
                else if (userRole == "operator") isOp = true
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Perform navigation back
                if (isOp) view?.let { Navigation.findNavController(it).navigate(R.id.action_settingsFragment_to_usersFragment) }
                else view?.let { Navigation.findNavController(it).navigate(R.id.action_settingsFragment_to_mainFragment) }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        val db = Firebase.firestore
        var isOp = false

        db.collection("users").document(currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val userRole = document.getString("role")
                if(userRole == "user") isOp = false
                else if (userRole == "operator") isOp = true
            }

        binding.emailView.text = "You're: " + currentUser!!.email.toString()

        binding.associatedUsersLenView.text = if(isOp) "Associated users: " + viewModel.usersList.value!!.size
                                              else "You're an user."

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