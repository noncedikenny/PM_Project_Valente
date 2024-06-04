package it.progmob.passwordmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ManagerViewModel : ViewModel() {
    private val _passwordList = MutableLiveData<MutableList<Password>>()
    private val _pinList = MutableLiveData<MutableList<Pin>>()
    private val _ccList = MutableLiveData<MutableList<CreditCard>>()
    private val _usersList = MutableLiveData<MutableList<User>>()

    val passwordList: LiveData<MutableList<Password>>
        get() = _passwordList
    val pinList: LiveData<MutableList<Pin>>
        get() = _pinList
    val ccList: LiveData<MutableList<CreditCard>>
        get() = _ccList
    val usersList: LiveData<MutableList<User>>
        get() = _usersList

    var imageClicked: Int = 0
    var userID: String? = null

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
        val usersList: ArrayList<User> = ArrayList()

        _passwordList.value = passwordList
        _pinList.value = pinList
        _ccList.value = ccList
        _usersList.value = usersList
        imageClicked = 0
    }

    fun fetchDataFromDatabase(userId: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        val passwordsRef = userRef.collection("Passwords")
        val pinsRef = userRef.collection("Pins")
        val ccRef = userRef.collection("CreditCards")

        passwordsRef.get().addOnSuccessListener { result ->
            val passwordList = _passwordList.value ?: mutableListOf()
            for (document in result) {
                val password = document.toObject(Password::class.java)
                password.let { passwordList.add(it) }
            }
            _passwordList.value = passwordList
        }

        pinsRef.get().addOnSuccessListener { result ->
            val pinList = _pinList.value ?: mutableListOf()
            for (document in result) {
                val pin = document.toObject(Pin::class.java)
                pin.let { pinList.add(it) }
            }
            _pinList.value = pinList
        }

        ccRef.get().addOnSuccessListener { result ->
            val ccList = _ccList.value ?: mutableListOf()
            for (document in result) {
                val cc = document.toObject(CreditCard::class.java)
                cc.let { ccList.add(it) }
            }
            _ccList.value = ccList
        }
    }

    fun fetchUsersFromDatabase(userId: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId).collection("associated_users")

        userRef.get().addOnSuccessListener { result ->
            val usersList = _usersList.value ?: mutableListOf()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val self = currentUser?.let { User(it.uid, currentUser.email.toString()) }

            if (self != null) {
                usersList.add(self)
            }

            for (document in result) {
                val user = document.toObject(User::class.java)
                user.let { usersList.add(it) }
            }
            _usersList.value = usersList
        }
    }
}