package it.progmob.passwordmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ManagerViewModel : ViewModel() {
    private val _passwordList = MutableLiveData<MutableList<Password>>()
    private val _pinList = MutableLiveData<MutableList<Pin>>()
    private val _ccList = MutableLiveData<MutableList<CreditCard>>()

    val passwordList: LiveData<MutableList<Password>>
        get() = _passwordList
    val pinList: LiveData<MutableList<Pin>>
        get() = _pinList
    val ccList: LiveData<MutableList<CreditCard>>
        get() = _ccList

    var imageClicked: Int = 0

    fun addItem(password: Password) {
        val passwordList = _passwordList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val existingPassword = passwordList.find { it.siteName == password.siteName }

        if (existingPassword != null) {
            // Update the existing subject with the new grade and credits
            existingPassword.siteName = password.siteName
            existingPassword.username = password.username
            existingPassword.password = password.password
        } else {
            // Add the new subject to the list
            passwordList.add(password)
        }

        // Update the LiveData with the modified list
        _passwordList.value = passwordList
    }

    fun addItem(pin: Pin) {
        val pinList = _pinList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val existingPin = pinList.find { it.password == pin.password }

        if (existingPin != null) {
            // Update the existing subject with the new grade and credits
            existingPin.description = pin.description
            existingPin.password = pin.password
        } else {
            // Add the new subject to the list
            pinList.add(pin)
        }

        // Update the LiveData with the modified list
        _pinList.value = pinList
    }

    fun addItem(cc: CreditCard) {
        val ccList = _ccList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val existingCC = ccList.find { it.number == cc.number }

        if (existingCC != null) {
            // Update the existing subject with the new grade and credits
            existingCC.number = cc.number
            existingCC.securityCode = cc.securityCode
        } else {
            // Add the new subject to the list
            ccList.add(cc)
        }

        // Update the LiveData with the modified list
        _ccList.value = ccList
    }

    fun removeItem(password: Password) {
        val passwordList = _passwordList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val passwordToDelete = passwordList.find { it.siteName == password.siteName }

        passwordList.remove(passwordToDelete)

        // Update the LiveData with the modified list
        _passwordList.value = passwordList
    }

    fun removeItem(pin: Pin) {
        val pinList = _pinList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val pinToDelete = pinList.find { it.description == pin.description }

        pinList.remove(pinToDelete)

        // Update the LiveData with the modified list
        _pinList.value = pinList
    }

    fun removeItem(cc: CreditCard) {
        val ccList = _ccList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val ccToDelete = ccList.find { it.number == cc.number }

        ccList.remove(ccToDelete)

        // Update the LiveData with the modified list
        _ccList.value = ccList
    }

    fun reset() {
        val passwordList: ArrayList<Password> = ArrayList()
        val pinList: ArrayList<Pin> = ArrayList()
        val ccList: ArrayList<CreditCard> = ArrayList()

        _passwordList.value = passwordList
        _pinList.value = pinList
        _ccList.value = ccList
    }

    fun fetchDataFromDatabase() {
        val db = Firebase.firestore

        db.collection("Passwords")
            .get()
            .addOnSuccessListener { result ->
                val passwordList = _passwordList.value ?: mutableListOf()
                for (document in result) {
                    val password = document.toObject(Password::class.java)
                    password.let { passwordList.add(it) }
                }
                _passwordList.value = passwordList
            }

        db.collection("Pins")
            .get()
            .addOnSuccessListener { result ->
                val pinList = _pinList.value ?: mutableListOf()
                for (document in result) {
                    val pin = document.toObject(Pin::class.java)
                    pin.let { pinList.add(it) }
                }
                _pinList.value = pinList
            }

        db.collection("CreditCards")
            .get()
            .addOnSuccessListener { result ->
                val ccList = _ccList.value ?: mutableListOf()
                for (document in result) {
                    val cc = document.toObject(CreditCard::class.java)
                    cc.let { ccList.add(it) }
                }
                _ccList.value = ccList
            }

    }
}