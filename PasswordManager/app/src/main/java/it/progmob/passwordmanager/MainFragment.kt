package it.progmob.passwordmanager

import NotificationWorker
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import it.progmob.passwordmanager.databinding.MainFragmentBinding
import java.util.Calendar

class MainFragment : Fragment() {

    //binding connected to the specific layout of the fragment
    private lateinit var binding: MainFragmentBinding

    // Use of viewModel among fragments to share data
    private val viewModel : ManagerViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using data binding
        (activity as AppCompatActivity).supportActionBar?.title = "Main menu"
        binding = MainFragmentBinding.inflate(inflater, container, false)

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
                if (isOp) view?.let { Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_usersFragment) }
            }
        })

        return binding.root
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.passwordImageView.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_listFragment)
            viewModel.imageClicked = 1
        }

        binding.pinImageView.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_listFragment)
            viewModel.imageClicked = 2
        }

        binding.ccImageView.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_listFragment)
            viewModel.imageClicked = 3
        }

    }
}