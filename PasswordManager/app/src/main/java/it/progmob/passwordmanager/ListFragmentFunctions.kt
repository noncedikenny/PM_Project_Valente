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
        3 -> if(viewInflated.findViewById<EditText>(R.id.cardNumberInput).text.toString().isEmpty() ||
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

@SuppressLint("ScheduleExactAlarm")
internal fun ListFragment.scheduleNotification(itemName: String, userEmail: String, datePicker: DatePicker, id: Int) {
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

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val msg = dateFormat.format(calendar.time)

    Toast.makeText(requireContext(), "Programmato in data: $msg", Toast.LENGTH_LONG).show()
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

internal fun observeDecryptedPasswords(list: List<Password>): List<Password> {
    return list.map { passwordItem ->
        val decryptedPassword = AESEncyption.decrypt(passwordItem.password)
        Password(
            passwordItem.siteName,
            passwordItem.username,
            decryptedPassword,  // Usa la password decriptata
            passwordItem.expirationDate
        )
    }
}

internal fun observeDecryptedPins(list: List<Pin>): List<Pin> {
    return list.map { pinItem ->
        val decryptedPin = AESEncyption.decrypt(pinItem.password)
        Pin(
            pinItem.description,
            decryptedPin,
            pinItem.expirationDate
        )
    }
}

internal fun observeDecryptedCreditCards(list: List<CreditCard>): List<CreditCard> {
    return list.map { ccItem ->
        val decryptedCCNumber = AESEncyption.decrypt(ccItem.number)
        val decryptedCCSecurityCode = AESEncyption.decrypt(ccItem.securityCode)
        CreditCard(
            decryptedCCNumber,
            decryptedCCSecurityCode,
            ccItem.expirationDate
        )
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