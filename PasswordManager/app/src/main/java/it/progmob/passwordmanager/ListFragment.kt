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
        val passwordCollection = userRef.collection("Passwords")
        val pinCollection = userRef.collection("Pins")
        val ccCollection = userRef.collection("CreditCards")

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
                    val decryptedPasswordList = observeDecryptedPasswords(passwordList)
                    binding.recyclerView.adapter = PasswordAdapter(decryptedPasswordList, {
                        cancelNotification(it.notificationID)
                        passwordCollection.document(it.siteName).delete()
                        viewModel.removeItem(it)
                    }, {})
                }
                binding.itemNameView.text = "Passwords"
                binding.resetAllButton.setOnClickListener {
                    resetFunction("Passwords")
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
                    val decryptedPinList = observeDecryptedPins(pinList)
                    binding.recyclerView.adapter = PinAdapter(decryptedPinList, {
                        cancelNotification(it.notificationID)
                        pinCollection.document(it.description).delete()
                        viewModel.removeItem(it)
                    }, {})
                }
                binding.itemNameView.text = "Pins"
                binding.resetAllButton.setOnClickListener {
                    resetFunction("Pins")
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
                    val decryptedCCList = observeDecryptedCreditCards(ccList)
                    binding.recyclerView.adapter = CCAdapter(decryptedCCList, {
                        cancelNotification(it.notificationID)
                        ccCollection.document(it.description).delete()
                        viewModel.removeItem(it)
                    }, {})
                }
                binding.itemNameView.text = "Credit Cards"
                binding.resetAllButton.setOnClickListener {
                    resetFunction("CreditCards")
                }

                // Initialize variables
                viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.add_cc_layout, null, false)
                datePicker = viewInflated.findViewById(R.id.expirationCreditCardInput)
                expireCheckBox = viewInflated.findViewById(R.id.expireCreditCardCheckBox)
            }

            else -> {
                Navigation.findNavController(view).navigate(R.id.action_listFragment_to_mainFragment)
                Toast.makeText(requireContext(), "Something gone wrong, retry or contact the assistance.", Toast.LENGTH_SHORT).show()
            }
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
            // Check if the popup was removed from parent correctly, if not remove it
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
                    val extractedStringFromDate = if (expireCheckBox.isChecked) getSelectedDateFromDatePicker(datePicker)
                                                  else "Doesn't expire."
                    val notificationID = if (expireCheckBox.isChecked) System.currentTimeMillis().toInt()
                                         else 0
                    val databaseNotificationID = hashMapOf("notificationID" to notificationID)

                    // Add a password
                    if(viewModel.imageClicked == 1) {
                        val encryptedPassword: String = AESEncyption.encrypt(viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString())
                        if(!encryptionSucceed(encryptedPassword)) {
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }

                        val newItem = Password(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString(),
                            encryptedPassword,
                            extractedStringFromDate,
                            notificationID)
                        viewModel.addItem(newItem)
                        passwordCollection.document(newItem.siteName).set(newItem)

                        scheduleNotification(
                            expireCheckBox,
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)
                        passwordCollection.document(newItem.siteName).update(databaseNotificationID as Map<String, Int>)

                        viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                            val decryptedPasswordList = observeDecryptedPasswords(passwordList)
                            binding.recyclerView.adapter = PasswordAdapter(decryptedPasswordList, {
                                    cancelNotification(notificationID)
                                    passwordCollection.document(it.siteName).delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }
                    // Add a pin
                    else if(viewModel.imageClicked == 2) {
                        val encryptedPin: String = AESEncyption.encrypt(viewInflated.findViewById<EditText>(R.id.pinInput).text.toString())
                        if(!encryptionSucceed(encryptedPin)) {
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }

                        val newItem = Pin(
                            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString(),
                            encryptedPin,
                            extractedStringFromDate,
                            notificationID)
                        viewModel.addItem(newItem)
                        pinCollection.document(newItem.description).set(newItem)

                        scheduleNotification(
                            expireCheckBox,
                            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)
                        pinCollection.document(newItem.description).update(databaseNotificationID as Map<String, Int>)

                        viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                            val decryptedPinList = observeDecryptedPins(pinList)
                            binding.recyclerView.adapter = PinAdapter(decryptedPinList, {
                                    cancelNotification(notificationID)
                                    pinCollection.document(it.description).delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }
                    // Add a credit card
                    else if(viewModel.imageClicked == 3) {
                        val description = viewInflated.findViewById<EditText>(R.id.cardDescriptionInput).text.toString()
                        val number = viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString()
                        val securityCode = viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.toString()

                        if(number.length != 16 || securityCode.length != 3){
                            Toast.makeText(requireContext(), "Invalid card.", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }
                        val encryptedNumber: String = AESEncyption.encrypt(number)
                        val encryptedSecurityCode: String = AESEncyption.encrypt(securityCode)
                        if(!encryptionSucceed(encryptedNumber) || !encryptionSucceed(encryptedSecurityCode)) {
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }

                        val newItem = CreditCard(
                            description,
                            encryptedNumber,
                            encryptedSecurityCode,
                            extractedStringFromDate,
                            notificationID  )
                        viewModel.addItem(newItem)
                        ccCollection.document(newItem.description).set(newItem)

                        scheduleNotification(
                            expireCheckBox,
                            viewInflated.findViewById<EditText>(R.id.cardDescriptionInput).text.toString(),
                            viewModel.user.email,
                            datePicker,
                            notificationID)
                        ccCollection.document(newItem.description).update(databaseNotificationID as Map<String, Int>)

                        viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                            val decryptedCCList = observeDecryptedCreditCards(ccList)
                            binding.recyclerView.adapter = CCAdapter(decryptedCCList, {
                                    cancelNotification(notificationID)
                                    ccCollection.document(it.description).delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }

                    alertDialog.dismiss()
                }
            }

            if (viewModel.imageClicked == 1 || viewModel.imageClicked == 2) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                    val random =
                        if (viewModel.imageClicked == 1) generateRandomPassword(12)
                        else generateRandomPin(8)
                    val item: EditText =
                        if (viewModel.imageClicked == 1) viewInflated.findViewById(R.id.passwordInput)
                        else viewInflated.findViewById(R.id.pinInput)
                    item.setText(random)
                }
            }
        }
    }
}