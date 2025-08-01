package com.example.mobileeshop

import java.io.Serializable
//κλάση τιμών προϊόντος για χρήση από άλλες κλάσεις
data class EshopProduct(
    val id: String = "",
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val pricePerUnit: Double? = null,
    val unitType: String? = null,
    val availability: Boolean = true,
    val isOnOffer: Boolean = false,
    var posothta: Int? = null
) : Serializable

annotation class Product
