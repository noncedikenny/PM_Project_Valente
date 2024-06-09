package it.progmob.passwordmanager

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
import it.progmob.passwordmanager.databinding.MainFragmentBinding

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.usersList.observe(viewLifecycleOwner) { users ->
                    if (users.size > 1) {
                        // Perform existing functionality
                        view?.let { Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_usersFragment) }
                    }
                }
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

        binding.backButton.setOnClickListener {
            val notificationID = System.currentTimeMillis().toInt()

            val intent = Intent(requireContext(), Notification::class.java).apply {
                putExtra("notification_title", "Reminder")
                putExtra("notification_text", "This is your 1-minute reminder!")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                notificationID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + 60 * 1000  // 1 minuto

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            Toast.makeText(requireContext(), "Alarm set.", Toast.LENGTH_LONG).show()
        }
    }
}