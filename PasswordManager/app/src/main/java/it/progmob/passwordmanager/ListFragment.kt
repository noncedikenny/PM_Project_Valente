package it.progmob.passwordmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.progmob.passwordmanager.databinding.ListFragmentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListFragment : Fragment() {

    // Binding connected to the specific layout of the fragment
    private lateinit var binding: ListFragmentBinding

    // Use of viewModel among fragments to share data
    private val viewModel : ManagerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = "Setup an item"
        binding = ListFragmentBinding.inflate(inflater, container, false)

        // Listener on Android's back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view?.let { Navigation.findNavController(it).navigate(R.id.action_listFragment_to_mainFragment) }
            }
        })

        return binding.root
    }

    @SuppressLint("CutPasteId", "InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = Firebase.firestore
        val userRef = viewModel.userID?.let { it1 -> db.collection("users").document(it1) }

        /*
        * This snippet loads the wanted list to observe, through the image clicked in the main fragment.
        * Every Adapter is created with three parameters: the list to update, long click listener, click listener.
        * The click listener, if not overwrote, will show / hide the sensitive data (password, pin, card number etc...)
        */
        when (viewModel.imageClicked) {
            // Observe passwords
            1 -> viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                binding.recyclerView.adapter = PasswordAdapter(passwordList, {
                        userRef?.collection("Passwords")?.document(it.siteName)?.delete()
                        viewModel.removeItem(it)
                    }, {})
                binding.resetAllButton.setOnClickListener {
                    resetFunction("Passwords")
                }
                binding.itemNameView.text = "Passwords"
            }


            // Observe pins
            2 -> viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                binding.recyclerView.adapter = PinAdapter(pinList, {
                        userRef?.collection("Pins")?.document(it.description)?.delete()
                        viewModel.removeItem(it)
                    }, {})
                binding.resetAllButton.setOnClickListener {
                    resetFunction("Pins")
                }
                binding.itemNameView.text = "Pins"
            }


            // Observe credit cards
            3 -> viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                binding.recyclerView.adapter = CCAdapter(ccList, {
                        userRef?.collection("CreditCards")?.document(it.number)?.delete()
                        viewModel.removeItem(it)
                    }, {})
                binding.resetAllButton.setOnClickListener {
                    resetFunction("CreditCards")
                }
                binding.itemNameView.text = "Credit Cards"
            }
        }

        // "+" click listener, adds an item depending on the list observed
        binding.addItem.setOnClickListener {
            // Setup builder to build the popup
            val dialogBuilder = AlertDialog.Builder(requireContext())
            lateinit var viewInflated: View
            lateinit var datePicker: DatePicker
            lateinit var expireCheckBox: CheckBox
            when (viewModel.imageClicked) {
                1 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_password_layout, null, false)
                    datePicker = viewInflated.findViewById(R.id.expirationPasswordInput)
                    expireCheckBox = viewInflated.findViewById(R.id.expirePasswordCheckBox)
                    val today = Calendar.getInstance()
                    datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ) { view, year, month, day -> }
                }
                2 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_pin_layout, null, false)
                    datePicker = viewInflated.findViewById(R.id.expirationPinInput)
                    expireCheckBox = viewInflated.findViewById(R.id.expirePinCheckBox)
                    val today = Calendar.getInstance()
                    datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ) { view, year, month, day -> }
                }
                3 -> {
                    viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_cc_layout, null, false)
                    datePicker = viewInflated.findViewById(R.id.expirationCreditCardInput)
                    expireCheckBox = viewInflated.findViewById(R.id.expireCreditCardCheckBox)
                    val today = Calendar.getInstance()
                    datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ) { view, year, month, day -> }
                }
                else -> null
            }

            expireCheckBox.setOnClickListener {
                if (datePicker.visibility == View.VISIBLE) {
                    datePicker.visibility = View.GONE
                } else {
                    datePicker.visibility = View.VISIBLE
                }
            }

            dialogBuilder.setView(viewInflated)
            dialogBuilder.setTitle("Setup item")
            dialogBuilder.setCancelable(false)

            // Buttons
            dialogBuilder.setPositiveButton("Submit") { _, _ -> }
            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            if(viewModel.imageClicked != 3) dialogBuilder.setNeutralButton("Generate Random") { _, _ -> }

            // Create popup
            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val somethingIsEmpty = emptyCheck(viewModel.imageClicked, viewInflated)

                if (somethingIsEmpty) Toast.makeText(requireContext(), "Every field must be filled.", Toast.LENGTH_SHORT).show()
                else {

                    val extractedStringFromDate = if (expireCheckBox.isChecked) {
                        getSelectedDateFromDatePicker(datePicker)
                    } else {
                        "Doesn't expire."
                    }

                    // Add a password
                    if(viewModel.imageClicked == 1) {
                        val newItem = Password(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString(),
                            extractedStringFromDate)

                        viewModel.addItem(newItem)
                        userRef?.collection("Passwords")?.document(newItem.siteName)?.set(newItem)

                        viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                            binding.recyclerView.adapter = PasswordAdapter(passwordList, {
                                    userRef?.collection("Passwords")?.document(it.siteName)?.delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }
                    // Add a pin
                    else if(viewModel.imageClicked == 2) {
                        val newItem = Pin(
                            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.pinInput).text.toString(),
                            extractedStringFromDate)
                        viewModel.addItem(newItem)
                        userRef?.collection("Pins")?.document(newItem.description)?.set(newItem)
                        viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                            binding.recyclerView.adapter = PinAdapter(pinList, {
                                    userRef?.collection("Pins")?.document(it.description)?.delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }
                    // Add a credit card
                    else if(viewModel.imageClicked == 3) {
                        val newItem = CreditCard(
                            viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.toString(),
                            extractedStringFromDate)
                        viewModel.addItem(newItem)
                        userRef?.collection("CreditCards")?.document(newItem.number)?.set(newItem)
                        viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                            binding.recyclerView.adapter = CCAdapter(ccList, {
                                    userRef?.collection("CreditCards")?.document(it.number)?.delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }

                    alertDialog.dismiss()
                }
            }

            // Generate a random password
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

    private fun resetFunction(collectionToDelete: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val viewInflated: View = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_layout, null, false)

        dialogBuilder.setView(viewInflated)
        dialogBuilder.setTitle("Reset")
        dialogBuilder.setMessage("You're going to reset everything, are you sure?")
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

            userRef?.collection(collectionToDelete)?.get()
                ?.addOnSuccessListener { documents ->
                    if (documents.isEmpty) Toast.makeText(requireContext(), "Nothing to reset.", Toast.LENGTH_LONG).show()
                    else {
                        for (document in documents) {
                            document.reference.delete()
                        }
                        Toast.makeText(requireContext(), "Everything has been deleted correctly.", Toast.LENGTH_LONG).show()
                    }
                };

            alertDialog.dismiss()

            viewModel.reset()
        }
    }

    @SuppressLint("InflateParams")
    private fun emptyCheck(imageClicked: Int, viewInflated: View): Boolean {

        when(imageClicked) {
            1 -> if(viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString().isEmpty() ||
                viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString().isEmpty() ||
                viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString().isEmpty()) {
                return true
            }
            2 -> if(viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString().isEmpty() ||
                viewInflated.findViewById<EditText>(R.id.pinInput).text.toString().isEmpty()) {
                return true
            }
            3 -> if(viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString().isEmpty() ||
                viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.toString().isEmpty()) {
                return true
            }
            else -> return false
        }
        return false
    }

    private fun getSelectedDateFromDatePicker(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
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