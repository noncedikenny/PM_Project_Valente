package it.progmob.passwordmanager

import android.util.Log
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
    var user: User = User("", "")
    var isOperator: Boolean = false

    fun addItem(password: Password) {
        val passwordList = _passwordList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val existingPassword = passwordList.find { it.siteName == password.siteName }

        if (existingPassword != null) {
            // Update the existing subject with the new grade and credits
            existingPassword.siteName = password.siteName
            existingPassword.username = password.username
            existingPassword.password = password.password
            existingPassword.expirationDate = password.expirationDate
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
        val existingPin = pinList.find { it.description == pin.description }

        if (existingPin != null) {
            // Update the existing subject with the new grade and credits
            existingPin.description = pin.description
            existingPin.password = pin.password
            existingPin.expirationDate = pin.expirationDate
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
            existingCC.expirationDate = cc.expirationDate
        } else {
            // Add the new subject to the list
            ccList.add(cc)
        }

        // Update the LiveData with the modified list
        _ccList.value = ccList
    }

    fun addUser(user: User) {
        val usersList = _usersList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val existingUser = usersList.find { it.id == user.id }

        if (existingUser != null) {
            // Update the existing subject with the new grade and credits
            existingUser.id = user.id
            existingUser.email = user.email
        } else {
            // Add the new subject to the list
            usersList.add(user)
        }

        // Update the LiveData with the modified list
        _usersList.value = usersList
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
        val ccToDelete = ccList.find { it.description == cc.description }

        ccList.remove(ccToDelete)

        // Update the LiveData with the modified list
        _ccList.value = ccList
    }

    fun removeUser(user: User) {
        val usersList = _usersList.value ?: mutableListOf()

        // Check if the subject with the same name already exists
        val userToDelete = usersList.find { it.id == user.id }

        usersList.remove(userToDelete)

        // Update the LiveData with the modified list
        _usersList.value = usersList
    }

    fun reset() {
        _passwordList.value = ArrayList()
        _pinList.value = ArrayList()
        _ccList.value = ArrayList()
        _usersList.value = ArrayList()
        imageClicked = 0
    }

    fun fetchDataFromDatabase(userId: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        val passwordsRef = userRef.collection("Passwords")
        val pinsRef = userRef.collection("Pins")
        val ccRef = userRef.collection("CreditCards")

        passwordsRef.get()
            .addOnSuccessListener { result ->
                val passwordList = _passwordList.value ?: mutableListOf()
                for (document in result) {
                    val password = document.toObject(Password::class.java)
                    password.let { passwordList.add(it) }
                }
                _passwordList.value = passwordList
            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseTest", "Error getting documents.", exception)
            }

        pinsRef.get()
            .addOnSuccessListener { result ->
                val pinList = _pinList.value ?: mutableListOf()
                for (document in result) {
                    val pin = document.toObject(Pin::class.java)
                    pin.let { pinList.add(it) }
                }
                _pinList.value = pinList
            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseTest", "Error getting documents.", exception)
            }

        ccRef.get()
            .addOnSuccessListener { result ->
                val ccList = _ccList.value ?: mutableListOf()
                for (document in result) {
                    val cc = document.toObject(CreditCard::class.java)
                    cc.let { ccList.add(it) }
                }
                _ccList.value = ccList
            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseTest", "Error getting documents.", exception)
            }
    }

    fun fetchUsersFromDatabase(userId: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId).collection("associated_users")

        userRef.get()
            .addOnSuccessListener { result ->
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

                if (usersList.size == 1) {
                    user.id = currentUser!!.uid
                    user.email = currentUser.email.toString()
                }

                _usersList.value = usersList
            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseTest", "Error getting documents.", exception)
            }
    }
}