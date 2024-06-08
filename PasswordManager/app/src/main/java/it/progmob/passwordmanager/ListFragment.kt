package it.progmob.passwordmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
                        userRef?.collection("Passwords")?.document(it.siteName)?.delete()
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
                        userRef?.collection("Pins")?.document(it.description)?.delete()
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
                        userRef?.collection("CreditCards")?.document(it.number)?.delete()
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
        ) { view, year, month, day -> }

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
            // Setup builder to build the popup
            val dialogBuilder = AlertDialog.Builder(requireContext())

            val parent = viewInflated.parent as? ViewGroup
            parent?.removeView(viewInflated)

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
                    if (expireCheckBox.isChecked) scheduleNotification(datePicker, notificationID)

                    // Add a password
                    if(viewModel.imageClicked == 1) {
                        val newItem = Password(
                            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.usernameInput).text.toString(),
                            viewInflated.findViewById<EditText>(R.id.passwordInput).text.toString(),
                            extractedStringFromDate)

                        viewModel.addItem(newItem)
                        userRef?.collection("Passwords")?.document(newItem.siteName)?.set(newItem)
                        userRef?.collection("Passwords")?.document(newItem.siteName)?.update(notificationMap as Map<String, Int>)

                        viewModel.passwordList.observe(viewLifecycleOwner) { passwordList ->
                            binding.recyclerView.adapter = PasswordAdapter(passwordList, {
                                    cancelNotification(notificationID)
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
                        userRef?.collection("Pins")?.document(newItem.description)?.update(notificationMap as Map<String, Int>)
                        viewModel.pinList.observe(viewLifecycleOwner) { pinList ->
                            binding.recyclerView.adapter = PinAdapter(pinList, {
                                    cancelNotification(notificationID)
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
                        userRef?.collection("CreditCards")?.document(newItem.number)?.update(notificationMap as Map<String, Int>)
                        viewModel.ccList.observe(viewLifecycleOwner) { ccList ->
                            binding.recyclerView.adapter = CCAdapter(ccList, {
                                    cancelNotification(notificationID)
                                    userRef?.collection("CreditCards")?.document(it.number)?.delete()
                                    viewModel.removeItem(it)
                                }, {})
                        }
                    }

                    alertDialog.dismiss()
                }
            }

            // Generate a random password button
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

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(datePicker: DatePicker, id: Int) {
        // I NOTIFICATIONID DEVONO ESSERE UNICI, GESTISCI LA CASISTICA

        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        // Imposta il calendario alla data selezionata
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        // Sottrai un giorno
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        // Prepara l'intent per la notifica con i dati aggiuntivi (titolo e testo)
        val intent = Intent(requireContext(), Notification::class.java).apply {
            putExtra("notification_title", "Qualcosa sta per scadere!")
            putExtra("notification_text", "Accedi e modifica i campi.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = calendar.timeInMillis
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val msg = dateFormat.format(calendar.time)

        Toast.makeText(requireContext(), "Programmato in data: $msg", Toast.LENGTH_LONG).show()
    }


    private fun cancelNotification(notificationID: Int) {
        val intent = Intent(requireContext(), Notification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Toast.makeText(requireContext(), "Notifica eliminata: $notificationID", Toast.LENGTH_LONG).show()
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