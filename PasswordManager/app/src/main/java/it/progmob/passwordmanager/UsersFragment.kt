package it.progmob.passwordmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usersRV.layoutManager = LinearLayoutManager(requireContext())

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            viewModel.fetchUsersFromDatabase(user.uid)
        }

        viewModel.usersList.observe(viewLifecycleOwner) { usersList ->
            binding.usersRV.adapter = UserAdapter(usersList) { selectedUserId ->
                viewModel.userID = selectedUserId.id
                Toast.makeText(requireContext(), selectedUserId.id, Toast.LENGTH_SHORT).show()
                //viewModel.fetchDataFromDatabase(selectedUserId.id)
                //Navigation.findNavController(view).navigate(R.id.action_usersFragment_to_mainFragment)
            }
        }
    }
}