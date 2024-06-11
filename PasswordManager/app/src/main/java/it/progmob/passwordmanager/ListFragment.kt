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
import java.util.Calendar

class ListFragment : Fragment() {

    // Binding connected to the specific layout of the fragment
    private lateinit var binding: ListFragmentBinding

    // Use of viewModel among fragments to share data
    val viewModel : ManagerViewModel by activityViewModels()

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
        val userRef = db.collection("users").document(viewModel.user.id)

        /*
        * This part loads the wanted list to observe, through the image clicked in the main fragment.
        * Every Adapter is created with three parameters: the list to update, long click listener, click listener.
        * The click listener, if not overwrote, will show / hide the sensitive data (password, pin, card number etc...)
        *
        * It initializes useful variables too, which will be used in the rest of the code. Every variable is initialized depending on
        * which image was clicked in the last fragment.
        */

        lateinit var viewInflated: View
        lateinit var datePicker: DatePicker
        lateinit var expireCheckBox: CheckBox

        when (viewModel.imageClicked) {

            1 ->
                // Observing passwords
                {
                viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                    binding.recyclerView.adapter = PasswordAdapter(passwordList, {
                        userRef.collection("Passwords").document(it.siteName).delete()
                        viewModel.removeItem(it)
                    }, {})
                    binding.resetAllButton.setOnClickListener {
                        resetFunction("Passwords")
                    }
                    binding.itemNameView.text = "Passwords"
                }

                // Initialize variables
                viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_password_layout, null, false)
                datePicker = viewInflated.findViewById(R.id.expirationPasswordInput)
                expireCheckBox = viewInflated.findViewById(R.id.expirePasswordCheckBox)
            }

            2 ->
                // Observing pins
                {
                viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                    binding.recyclerView.adapter = PinAdapter(pinList, {
                        userRef.collection("Pins").document(it.description).delete()
                        viewModel.removeItem(it)
                    }, {})
                    binding.resetAllButton.setOnClickListener {
                        resetFunction("Pins")
                    }
                    binding.itemNameView.text = "Pins"
                }

                // Initialize variables
                viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_pin_layout, null, false)
                datePicker = viewInflated.findViewById(R.id.expirationPinInput)
                expireCheckBox = viewInflated.findViewById(R.id.expirePinCheckBox)
            }

            3 ->
                // Observing credit cards
                {
                viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                    binding.recyclerView.adapter = CCAdapter(ccList, {
                        userRef.collection("CreditCards").document(it.number).delete()
                        viewModel.removeItem(it)
                    }, {})
                    binding.resetAllButton.setOnClickListener {
                        resetFunction("CreditCards")
                    }
                    binding.itemNameView.text = "Credit Cards"
                }

                // Initialize variables
                viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_cc_layout, null, false)
                datePicker = viewInflated.findViewById(R.id.expirationCreditCardInput)
                expireCheckBox = viewInflated.findViewById(R.id.expireCreditCardCheckBox)
            }

            else -> null
        }

        // Initialize date picker
        val today = Calendar.getInstance()
        datePicker.minDate = today.timeInMillis
        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ) { _, _, _, _ -> }

        // Setup checkbox
        expireCheckBox.setOnClickListener {
            if (datePicker.visibility == View.VISIBLE) {
                datePicker.visibility = View.GONE
            } else {
                datePicker.visibility = View.VISIBLE
            }
        }

        /*
        * This part sets the on click listener of the button which adds a new item to the recycler view / database.
        * 2 parts: Builder setup, AlertDialog setup
        * The click listener, if not overwrote, will show / hide the sensitive data (password, pin, card number etc...)
        */
        binding.addItem.setOnClickListener {

            val parent = viewInflated.parent as? ViewGroup
            parent?.removeView(viewInflated)


            // Setup builder to build the popup
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(viewInflated)
            dialogBuilder.setTitle("Setup item")
            dialogBuilder.setCancelable(false)

            // Setup builder's buttons
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

                    val notificationID = System.currentTimeMillis().toInt()
                    val notificationMap = hashMapOf("notificationID" to notificationID)

                    // Add a password
                    if(viewModel.imageClicked == 1) {
                        val newItem = Password(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString(),
                            extractedStringFromDate)

                        if(expireCheckBox.isChecked) scheduleNotification(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)

                        viewModel.addItem(newItem)
                        userRef.collection("Passwords").document(newItem.siteName).set(newItem)
                        userRef.collection("Passwords").document(newItem.siteName).update(notificationMap as Map<String, Int>)

                        viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                            binding.recyclerView.adapter = PasswordAdapter(passwordList, {
                                    cancelNotification(notificationID)
                                    userRef.collection("Passwords").document(it.siteName).delete()
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

                        if(expireCheckBox.isChecked) scheduleNotification(
                            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)

                        viewModel.addItem(newItem)
                        userRef.collection("Pins").document(newItem.description).set(newItem)
                        userRef.collection("Pins").document(newItem.description).update(notificationMap as Map<String, Int>)
                        viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                            binding.recyclerView.adapter = PinAdapter(pinList, {
                                    cancelNotification(notificationID)
                                    userRef.collection("Pins").document(it.description).delete()
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

                        if(expireCheckBox.isChecked) scheduleNotification(
                            viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)

                        viewModel.addItem(newItem)
                        userRef.collection("CreditCards").document(newItem.number).set(newItem)
                        userRef.collection("CreditCards").document(newItem.number).update(notificationMap as Map<String, Int>)
                        viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                            binding.recyclerView.adapter = CCAdapter(ccList, {
                                    cancelNotification(notificationID)
                                    userRef.collection("CreditCards").document(it.number).delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }

                    alertDialog.dismiss()
                }
            }

            // Setup button to generate a random password
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