package com.example.mobileeshop

//δημιουργία κλάσης δεδομένων χρήστη για παραγγελία
data class DeliveryHistory(
    val address: String = "",
    val city: String = "",
    val phone: String = "",
    val delivery: String = "",
    val payment: String = "",
    val sum_price: Double = 0.0,
    val timestamp: Long = 0L
)



