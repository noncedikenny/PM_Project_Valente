package it.progmob.passwordmanager

data class Password(var siteName: String, var username: String, var password: String)
data class Pin(var description: String, var password: String)
data class CreditCard(var number: String, var securityCode: String)