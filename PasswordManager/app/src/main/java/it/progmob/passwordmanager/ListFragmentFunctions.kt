package it.progmob.passwordmanager

import NotificationWorker
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// NOTIFICATION FUNCTIONS
// The two functions below are useful for preparing, sending and deleting notifications.
// BroadcastReceivers and Workers were used.
@SuppressLint("ScheduleExactAlarm")
internal fun ListFragment.scheduleNotification(checkBox: CheckBox, itemName: String, userEmail: String, datePicker: DatePicker, id: Int) {
    if(!checkBox.isChecked) return

    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year

    // Set the calendar
    //val calendar = Calendar.getInstance()
    //calendar.set(year, month, day)

    // Subtract a day
    //calendar.add(Calendar.DAY_OF_MONTH, -1)

    //val time = calendar.timeInMillis

    //UNCOMMENT THIS PART ONLY IF YOU'RE DEBUGGING.
    //INSTEAD COMMENT THE PART ABOVE.
    val calendar = Calendar.getInstance()
    val time = calendar.timeInMillis + 10 * 1000

    val inputData = Data.Builder()
        .putString("itemName", itemName)
        .putString("userEmail", userEmail)
        .putInt("notificationID", id)
        .putLong("triggerTime", time)
        .build()

    val myWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(inputData)
        .build()

    // Enqueue the WorkRequest
    val workManager = WorkManager.getInstance(requireContext())
    workManager.enqueue(myWorkRequest)

    //val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    //val msg = dateFormat.format(calendar.time)

    //Toast.makeText(requireContext(), "Programmato in data: $msg", Toast.LENGTH_LONG).show()
}

internal fun ListFragment.cancelNotification(notificationID: Int) {

    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        if (!alarmManager.canScheduleExactAlarms()) {
            return
        }
    }

    val intent = Intent(requireContext(), Notification::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        requireContext(),
        notificationID,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    alarmManager.cancel(pendingIntent)
}


// OBSERVE DECRYPTED ITEM FUNCTIONS
// The three functions below are useful for decrypting any list of item passed.
internal fun observeDecryptedPasswords(list: List<Password>): List<Password> {
    return list.map { passwordItem ->
        val decryptedPassword = AESEncyption.decrypt(passwordItem.password)
        Password(
            passwordItem.siteName,
            passwordItem.username,
            decryptedPassword,  // Usa la password decriptata
            passwordItem.expirationDate,
            passwordItem.notificationID
        )
    }
}

internal fun observeDecryptedPins(list: List<Pin>): List<Pin> {
    return list.map { pinItem ->
        val decryptedPin = AESEncyption.decrypt(pinItem.password)
        Pin(
            pinItem.description,
            decryptedPin,
            pinItem.expirationDate,
            pinItem.notificationID
        )
    }
}

internal fun observeDecryptedCreditCards(list: List<CreditCard>): List<CreditCard> {
    return list.map { ccItem ->
        val decryptedCCNumber = AESEncyption.decrypt(ccItem.number)
        val decryptedCCSecurityCode = AESEncyption.decrypt(ccItem.securityCode)
        CreditCard(
            ccItem.description,
            decryptedCCNumber,
            decryptedCCSecurityCode,
            ccItem.expirationDate,
            ccItem.notificationID
        )
    }
}


// UTILITY FUNCTIONS
// The functions below are useful for making the code readable.
internal fun ListFragment.resetFunction(collectionToDelete: String) {
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

        val userRef = db.collection("users").document(viewModel.user.id)

        userRef.collection(collectionToDelete).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "Nothing to reset.", Toast.LENGTH_LONG).show()
                } else {
                    for (document in documents) {
                        // Cancella la notifica legata a questo documento
                        val notificationID = document.getLong("notificationID")?.toInt()
                        if (notificationID != null) {
                            cancelNotification(notificationID)
                        }

                        // Cancella il documento
                        document.reference.delete()
                    }
                    Toast.makeText(requireContext(), "Everything has been deleted correctly.", Toast.LENGTH_LONG).show()
                }
            }

        alertDialog.dismiss()

        viewModel.reset()
    }
}

@SuppressLint("InflateParams")
internal fun emptyCheck(imageClicked: Int, viewInflated: View): Boolean {

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
        3 -> if(viewInflated.findViewById<EditText>(R.id.cardDescriptionInput).text.toString().isEmpty() ||
            viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString().isEmpty() ||
            viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.toString().isEmpty()) {
            return true
        }
        else -> return false
    }
    return false
}

internal fun getSelectedDateFromDatePicker(datePicker: DatePicker): String {
    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

internal fun ListFragment.encryptionSucceed(fieldToCheck: String) : Boolean {
    if(fieldToCheck == "-") {
        Toast.makeText(requireContext(), "Something gone wrong, retry or contact the assistance.", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

internal fun ListFragment.clearFormFields(shownList: CharSequence, viewInflated: View) {
    when (shownList) {
        "Passwords" -> {
            viewInflated.findViewById<EditText>(R.id.siteNameInput).text.clear()
            viewInflated.findViewById<EditText>(R.id.usernameInput).text.clear()
            viewInflated.findViewById<EditText>(R.id.passwordInput).text.clear()
            viewInflated.findViewById<CheckBox>(R.id.expirePasswordCheckBox).isChecked = false
        }
        "Pins" -> {
            viewInflated.findViewById<EditText>(R.id.pinDescriptionInput).text.clear()
            viewInflated.findViewById<EditText>(R.id.pinInput).text.clear()
            viewInflated.findViewById<CheckBox>(R.id.expirePinCheckBox).isChecked = false
        }
        "Credit Cards" -> {
            viewInflated.findViewById<EditText>(R.id.cardDescriptionInput).text.clear()
            viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.clear()
            viewInflated.findViewById<EditText>(R.id.cardSafetyCodeInput).text.clear()
            viewInflated.findViewById<CheckBox>(R.id.expireCreditCardCheckBox).isChecked = false
        }
        else -> {
            Toast.makeText(requireContext(), "Something gone wrong, retry or contact the assistance.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun generateRandomPassword(length: Int): String {
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

fun generateRandomPin(length: Int): String {
    val digitChars = ('0'..'9').toList()

    val requiredChars = listOf(digitChars).map { it.random() }
    val remainingChars = (length - requiredChars.size).coerceAtLeast(0)

    val randomChars = (1..remainingChars)
        .map { digitChars.random() }

    val shuffledPassword = (requiredChars + randomChars).shuffled()

    return shuffledPassword.joinToString("")
}
