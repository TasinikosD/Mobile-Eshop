package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalLabel: TextView

    //Στο onCreate, φορτώνεται το layout activity_cart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cartRecyclerView)
        totalLabel = findViewById(R.id.totalPriceLabel)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductAdapter(CartManager.getCartItems())

        //συνολική τιμή αγοράς
        val total = CartManager.getTotalPrice()
        totalLabel.text = getString(R.string.total_price_format, total)
    }
}
