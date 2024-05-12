package it.progmob.passwordmanager

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.progmob.passwordmanager.databinding.ListFragmentBinding

class ListFragment : Fragment() {

    //binding connected to the specific layout of the fragment
    private lateinit var binding: ListFragmentBinding

    // Use of viewModel among fragments to share data
    private val viewModel : ManagerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = "Setup an item"
        binding = ListFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        when (viewModel.imageClicked) {
            1 -> viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                binding.recyclerView.adapter = PasswordAdapter(passwordList) {
                    val db = Firebase.firestore
                    db.collection("Passwords").document(it.siteName).delete()
                    viewModel.removeItem(it)
                }
            }

            2 -> viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                binding.recyclerView.adapter = PinAdapter(pinList) {
                    val db = Firebase.firestore
                    db.collection("Pins").document(it.description).delete()
                    viewModel.removeItem(it)
                }
            }

            3 -> viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                binding.recyclerView.adapter = CCAdapter(ccList) {
                    val db = Firebase.firestore
                    db.collection("CreditCards").document(it.number).delete()
                    viewModel.removeItem(it)
                }
            }
        }

        binding.addItem.setOnClickListener {
            // Setup builder to build the popup
            val dialogBuilder = AlertDialog.Builder(requireContext())
            lateinit var viewInflated: View

            when (viewModel.imageClicked) {
                1 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_password_layout, null, false)
                }
                2 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_pin_layout, null, false)
                }
                3 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_cc_layout, null, false)
                }
                else -> Toast.makeText(requireContext(), "0", Toast.LENGTH_SHORT).show()
            }

            dialogBuilder.setView(viewInflated)
            dialogBuilder.setTitle("Setup item")
            dialogBuilder.setCancelable(false)

            // Buttons
            dialogBuilder.setPositiveButton("Submit") { _, _ -> }
            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            if(viewModel.imageClicked != 3) {
                dialogBuilder.setNeutralButton("Generate Random") { _, _ -> }
            }

            // Create popup
            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                // Bind input
                var somethingIsEmpty = false

                if (somethingIsEmpty) {
                    Toast.makeText(requireContext(), "Every field must be filled.", Toast.LENGTH_SHORT).show()
                } else {
                    val db = Firebase.firestore

                    if(viewModel.imageClicked == 1) {
                        val newItem = Password(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString())
                        viewModel.addItem(newItem)
                        db.collection("Passwords").document(newItem.siteName).set(newItem)

                        viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                            binding.recyclerView.adapter = PasswordAdapter(passwordList) {
                                db.collection("Passwords").document(it.siteName).delete()
                                viewModel.removeItem(it)
                            }
                        }
                    }
                    else if(viewModel.imageClicked == 2) {
                        val newItem = Pin(
                            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.pinInput).text.toString())
                        viewModel.addItem(newItem)
                        db.collection("Pins").document(newItem.description).set(newItem)
                        viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                            binding.recyclerView.adapter = PinAdapter(pinList) {
                                db.collection("Pins").document(it.description).delete()
                                viewModel.removeItem(it)
                            }
                        }
                    }
                    else if(viewModel.imageClicked == 3) {
                        val newItem = CreditCard(
                            viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.toString())
                        viewModel.addItem(newItem)
                        db.collection("CreditCards").document(newItem.number).set(newItem)
                        viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                            binding.recyclerView.adapter = CCAdapter(ccList) {
                                db.collection("CreditCards").document(it.number).delete()
                                viewModel.removeItem(it)
                            }
                        }
                    }

                    alertDialog.dismiss()
                }
            }

            if (viewModel.imageClicked == 1 || viewModel.imageClicked == 2) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                    val random = generateRandom(12)
                    val item: EditText = if (viewModel.imageClicked == 1) viewInflated.findViewById(R.id.passwordInput)
                    else viewInflated.findViewById(R.id.pinInput)
                    item.setText(random)
                }
            }
        }
    }
}

fun generateRandom(length: Int): String {
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
