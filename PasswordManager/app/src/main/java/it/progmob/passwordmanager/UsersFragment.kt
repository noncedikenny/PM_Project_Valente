package it.progmob.passwordmanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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

        if (user != null) {
            viewModel.fetchUsersFromDatabase(user.uid)
            viewModel.usersList.observe(viewLifecycleOwner) { usersList ->
                if (usersList.size == 1) {
                    viewModel.user.id = user.uid
                    viewModel.user.email = user.email.toString()
                    viewModel.fetchDataFromDatabase(user.uid)
                    Navigation.findNavController(view).navigate(R.id.action_usersFragment_to_mainFragment)
                }

                binding.usersRV.adapter = UserAdapter(usersList) { selectedUserId ->
                    viewModel.user.id = selectedUserId.id
                    viewModel.user.email = selectedUserId.email.toString()
                    viewModel.fetchDataFromDatabase(selectedUserId.id)
                    Navigation.findNavController(view).navigate(R.id.action_usersFragment_to_mainFragment)
                }
            }
        }
    }
}