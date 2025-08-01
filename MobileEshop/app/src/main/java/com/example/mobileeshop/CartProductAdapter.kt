package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

//δημιουργία κλάσης για προϊόντα στο καλάθι
class CartProductAdapter(
    private val products: MutableList<EshopProduct>,
    private val onDeleteClicked: (EshopProduct) -> Unit,
    private val onQuantityChanged: () -> Unit  // callback to update total price
) : RecyclerView.Adapter<CartProductAdapter.ViewHolder>() {

    //υποκλάση για την εμφάνιση του κάθε προϊόντος
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.productName)
        val price: TextView = v.findViewById(R.id.productPrice)
        val quantityText: TextView = v.findViewById(R.id.quantityText)
        val btnMinus: Button = v.findViewById(R.id.btnMinus)
        val btnPlus: Button = v.findViewById(R.id.btnPlus)
        val deleteButton: Button = v.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val ctx = holder.itemView.context

        holder.name.text = product.name

        val quantity = product.posothta ?: 1
        holder.quantityText.text = quantity.toString()

        // αναβάθμιση τιμής για την εκάστοτε ποσότητα
        val priceForQuantity = (product.pricePerUnit ?: 0.0) * quantity
        holder.price.text = ctx.getString(R.string.price_format, priceForQuantity)

        //μείωση ποσότητας
        holder.btnMinus.setOnClickListener {
            if (product.posothta == null) product.posothta = 1
            if (product.posothta!! > 1) {
                product.posothta = product.posothta!! - 1
                holder.quantityText.text = product.posothta.toString()
                // Update price
                val newPrice = (product.pricePerUnit ?: 0.0) * product.posothta!!
                holder.price.text = ctx.getString(R.string.price_format, newPrice)
                CartManager.updateQuantity(product, product.posothta!!)
                onQuantityChanged()
            }
        }

        //αύξηση ποσότητας
        holder.btnPlus.setOnClickListener {
            if (product.posothta == null) product.posothta = 1
            product.posothta = product.posothta!! + 1
            holder.quantityText.text = product.posothta.toString()
            // Update price
            val newPrice = (product.pricePerUnit ?: 0.0) * product.posothta!!
            holder.price.text = ctx.getString(R.string.price_format, newPrice)
            CartManager.updateQuantity(product, product.posothta!!)
            onQuantityChanged()
        }

        //διαγραφή προϊόντος
        holder.deleteButton.setOnClickListener {
            onDeleteClicked(product)
            notifyDataSetChanged()
            onQuantityChanged()
        }
    }
}


