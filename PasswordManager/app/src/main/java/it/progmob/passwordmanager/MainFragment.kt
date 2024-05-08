package it.progmob.passwordmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passwordIcon: ImageView = view.findViewById(R.id.passwordImageView)
        val pinIcon: ImageView = view.findViewById(R.id.pinImageView)
        val ccIcon: ImageView = view.findViewById(R.id.ccImageView)
        val settingsIcon: ImageView = view.findViewById(R.id.settingsImageView)

        passwordIcon.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_listFragment)
        }

        pinIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Pin", Toast.LENGTH_SHORT).show()
        }

        ccIcon.setOnClickListener {
            Toast.makeText(requireContext(), "CC", Toast.LENGTH_SHORT).show()
        }

        settingsIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
        }
    }
}