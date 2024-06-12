package it.progmob.passwordmanager

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import it.progmob.passwordmanager.databinding.UsersFragmentBinding


class UsersFragment : Fragment() {
    private lateinit var binding: UsersFragmentBinding
    private val viewModel : ManagerViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = "Main menu"
        binding = UsersFragmentBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Nothing to do!
            }
        })

        viewModel.reset()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usersRV.layoutManager = LinearLayoutManager(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore
        val usersInDB = db.collection("users")

        if (user != null) {
            // Get user role
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if(document.contains("role")){
                        val userRole = document.getString("role")

                        if (userRole == "operator") {
                            viewModel.fetchUsersFromDatabase(user.uid)
                            binding.addUser.visibility = View.VISIBLE

                            // Setup popup's view
                            val popupLayout = LayoutInflater.from(requireContext()).inflate(R.layout.add_user_layout, null, false)
                            val dialogBuilder = AlertDialog.Builder(requireContext())
                            dialogBuilder.setView(popupLayout)
                            dialogBuilder.setTitle("Add an User")
                            dialogBuilder.setCancelable(false)

                            // Setup builder's buttons
                            dialogBuilder.setPositiveButton("Submit") { _, _ -> }
                            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                                dialog.cancel()
                            }

                            // Add a new user
                            binding.addUser.setOnClickListener {
                                val parent = popupLayout.parent as? ViewGroup
                                parent?.removeView(popupLayout)

                                popupLayout.findViewById<EditText>(R.id.userIDInput).text.clear()
                                popupLayout.findViewById<EditText>(R.id.userEmailInput).text.clear()

                                val alertDialog = dialogBuilder.create()
                                alertDialog.show()
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                                    val idInput: String = popupLayout.findViewById<EditText>(R.id.userIDInput).text.toString()
                                    val emailInput: String = popupLayout.findViewById<EditText>(R.id.userEmailInput).text.toString()
                                    val userToAdd = User(idInput, emailInput)

                                    if (idInput.isEmpty() || emailInput.isEmpty()) {
                                        Toast.makeText(requireContext(), "Please fill every field.", Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        usersInDB.document(idInput).get().addOnSuccessListener { document ->
                                            if(document.exists()) {
                                                db.collection("users").document(user.uid)
                                                    .collection("associated_users").document(idInput).set(userToAdd)
                                                viewModel.addUser(userToAdd)
                                            }
                                            else Toast.makeText(requireContext(), "The user doesn't exist.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    alertDialog.dismiss()
                                }
                            }
                        }
                        else {
                            viewModel.user.id = user.uid
                            viewModel.user.email = user.email.toString()
                            viewModel.fetchDataFromDatabase(user.uid)
                            Navigation.findNavController(view).navigate(R.id.action_usersFragment_to_mainFragment)
                        }
                    }
                    else Toast.makeText(requireContext(), "Something gone wrong, retry or contact the assistance.", Toast.LENGTH_SHORT).show()
                }

            // Observe the users list (filled only if the user is an operator)
            viewModel.usersList.observe(viewLifecycleOwner) { usersList ->
                binding.usersRV.adapter = UserAdapter(usersList,
                    clickListener = { selectedUserId ->
                        viewModel.user.id = selectedUserId.id
                        viewModel.user.email = selectedUserId.email
                        viewModel.fetchDataFromDatabase(selectedUserId.id)
                        Navigation.findNavController(view).navigate(R.id.action_usersFragment_to_mainFragment)
                    },
                    longClickListener = { selectedUserId ->
                        if (user.uid != selectedUserId.id) {
                            db.collection("users").document(user.uid)
                                .collection("associated_users").document(selectedUserId.id).delete()
                            viewModel.removeUser(selectedUserId)
                        }
                    })
            }
        }
    }
}