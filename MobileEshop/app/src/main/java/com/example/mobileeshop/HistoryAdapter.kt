package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val userId: String,
    private val orders: List<OrderWithProducts>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    data class OrderWithProducts(
        val orderId: String,
        val deliveryHistory: DeliveryHistory,
        val products: List<ProductInHistory>
    )

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderSummaryText: TextView = view.findViewById(R.id.orderSummaryText)
        val productsText: TextView = view.findViewById(R.id.productsText)
        val repeatButton: Button = view.findViewById(R.id.btnRepeatOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        val delivery = order.deliveryHistory
        val date = android.text.format.DateFormat.format("yyyy-MM-dd", delivery.timestamp)

        // Δημιουργία κειμένου με χρήση πόρων για κάθε γραμμή
        val summary = """
        ${context.getString(R.string.order_id)} ${order.orderId}
        ${context.getString(R.string.date)} $date
        ${context.getString(R.string.address)} ${delivery.address}, ${delivery.city}
        ${context.getString(R.string.phone)} ${delivery.phone}
        ${context.getString(R.string.payment)} ${delivery.payment}
        ${context.getString(R.string.delivery)} ${delivery.delivery}
        ${context.getString(R.string.total, delivery.sum_price)}
    """.trimIndent()

        holder.orderSummaryText.text = summary

        val productsInfo = order.products.joinToString("\n") { product ->
            context.getString(R.string.product_info, product.name, product.quantity, product.price)
        }
        holder.productsText.text = productsInfo

        holder.repeatButton.setOnClickListener {
            CartManager.clearCart()
            for (product in order.products) {
                val productToAdd = EshopProduct(
                    name = product.name,
                    pricePerUnit = product.price,
                    posothta = product.quantity
                )
                CartManager.add(productToAdd)
            }
            Toast.makeText(context, context.getString(R.string.repeat_order_success), Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = orders.size
}

