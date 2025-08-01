package com.example.mobileeshop

//καλάθι αγορών με εισαγωγή , αφαίρεση, καθαρισμό σε περίπτωση παραγγελίας, ανανέωση και συνολικό ποσό
object CartManager {
    private val cartItems = mutableListOf<EshopProduct>()

    fun getCartItems(): MutableList<EshopProduct> = cartItems

    fun add(product: EshopProduct) {
        val existing = cartItems.find { it.name == product.name }
        if (existing != null) {
            existing.posothta = (existing.posothta ?: 1) + 1
        } else {
            product.posothta = 1
            cartItems.add(product)
        }
    }

    fun remove(product: EshopProduct) {
        cartItems.removeAll { it.name == product.name }
    }

    fun updateQuantity(product: EshopProduct, quantity: Int) {
        cartItems.find { it.name == product.name }?.posothta = quantity
    }

    fun getTotalPrice(): Double {
        return cartItems.sumOf { (it.pricePerUnit ?: 0.0) * (it.posothta ?: 1) }
    }

    fun clearCart() {
        cartItems.clear()
    }
}


