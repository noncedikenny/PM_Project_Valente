package it.progmob.passwordmanager

data class Password(var siteName: String, var username: String, var password: String, var expirationDate: String) {
    constructor() : this("", "", "", "")
}
data class Pin(var description: String, var password: String, var expirationDate: String) {
    constructor() : this("", "", "")
}
data class CreditCard(var number: String, var securityCode: String, var expirationDate: String) {
    constructor() : this("", "", "")
}
data class User(var id: String, var email: String) {
    constructor() : this("", "")
}