package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//κλάση για εμφάνιση προϊόντων
class ProductAdapter(
    private val products: List<EshopProduct>
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val wishSet = mutableSetOf<String>()

    init {
        auth.currentUser?.uid?.let { uid ->
            db.collection("wishlist")
                .whereEqualTo("userid", uid)
                .get()
                .addOnSuccessListener { snap ->
                    wishSet.clear()
                    wishSet += snap.documents.mapNotNull { it.getString("nameproduct") }
                    notifyDataSetChanged()
                }
        }
    }

    //υποκλάση για εμφάνιση πληροφοριών εκάστοτε προϊόντος
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.productImage)
        val name: TextView = v.findViewById(R.id.productName)
        val desc: TextView = v.findViewById(R.id.productDescription)
        val price: TextView = v.findViewById(R.id.productPrice)
        val unitType: TextView = v.findViewById(R.id.productUnitType)
        val availability: TextView = v.findViewById(R.id.productAvailability)
        val addBtn: Button = v.findViewById(R.id.addToCartButton)
        val wishCheck: CheckBox = v.findViewById(R.id.wishlistCheckbox)
        val offerBadge: TextView = v.findViewById(R.id.offerBadge)
        val posothta: TextView = v.findViewById(R.id.posothta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
        )

    override fun getItemCount(): Int = products.size

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val product = products[pos]
        val ctx = holder.itemView.context
        val user = auth.currentUser ?: return

        Glide.with(ctx).load(product.imageUrl).into(holder.image)
        holder.name.text = product.name
        holder.desc.text = product.description
        holder.price.text = ctx.getString(R.string.price_format, product.pricePerUnit ?: 0.0)
        holder.unitType.text = product.unitType ?: ""

        // εμφάνιση ποσότητας
        val quantity = product.posothta ?: 0
        Log.d("ProductAdapter", "Binding ${product.name} - Posothta: $quantity")
        holder.posothta.text = ctx.getString(R.string.quantities, quantity)

        //εάν το προΪόν είναι διαθέσιμο εμφανίζει το κουμπί και το fotn-color είναι πράσινο
        if (product.availability) {
            holder.availability.text = ctx.getString(R.string.available)
            holder.availability.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_green_dark))
            holder.addBtn.visibility = View.VISIBLE
        } else {
            holder.availability.text = ctx.getString(R.string.not_available)
            holder.availability.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_red_dark))
            holder.addBtn.visibility = View.GONE
        }

        //εάν το προϊόν είναι σε έκπτωση εμφανίζεται
        if (product.isOnOffer) {
            holder.offerBadge.visibility = View.VISIBLE
            holder.offerBadge.text = ctx.getString(R.string.offer_badge)
        } else {
            holder.offerBadge.visibility = View.GONE
        }

        //πρόσθεση προϊόντος στο καλάθι
        holder.addBtn.setOnClickListener {
            CartManager.add(product)
            Toast.makeText(ctx, ctx.getString(R.string.added_to_cart, product.name), Toast.LENGTH_SHORT).show()
        }

        holder.wishCheck.setOnCheckedChangeListener(null)
        holder.wishCheck.isChecked = wishSet.contains(product.name)

        holder.wishCheck.setOnCheckedChangeListener { _, isChecked ->
            val uid = user.uid
            val nameKey = product.name ?: return@setOnCheckedChangeListener

            val wishlistQuery = db.collection("wishlist")
                .whereEqualTo("userid", uid)
                .whereEqualTo("nameproduct", nameKey)

            if (isChecked) {
                val data = mapOf(
                    "nameproduct" to product.name,
                    "userid" to uid,
                    "imageurl" to product.imageUrl,
                    "description" to product.description,
                    "priceperunit" to product.pricePerUnit,
                    "unittype" to product.unitType,
                    "posothta" to (product.posothta ?: 0)
                )
                db.collection("wishlist").add(data)
                    .addOnSuccessListener {//προσθήκη στα αγαπημένα
                        wishSet.add(nameKey)
                        Toast.makeText(ctx, ctx.getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(ctx, ctx.getString(R.string.failed_to_add_favorite, it.message), Toast.LENGTH_LONG).show()
                    }
            } else {
                wishlistQuery.get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            db.collection("wishlist").document(doc.id).delete()
                        }
                        wishSet.remove(nameKey)
                        Toast.makeText(ctx, ctx.getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(ctx, ctx.getString(R.string.failed_to_remove_favorite, it.message), Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}
