package it.progmob.passwordmanager

data class Password(var siteName: String, var username: String, var password: String, var expirationDate: String, var notificationID: Int) {
    constructor() : this("", "", "", "", 0)
}
data class Pin(var description: String, var password: String, var expirationDate: String, var notificationID: Int) {
    constructor() : this("", "", "", 0)
}
data class CreditCard(var description: String, var number: String, var securityCode: String, var expirationDate: String, var notificationID: Int) {
    constructor() : this("", "", "", "", 0)
}
data class User(var id: String, var email: String) {
    constructor() : this("", "")
}