package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//κλάση για wishlist
class WishlistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val wishlistItems = mutableListOf<EshopProduct>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_wishlist, container, false)

        recyclerView = view.findViewById(R.id.wishlistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(wishlistItems)
        recyclerView.adapter = adapter

        loadWishlist()

        return view
    }

    //συνάρτηση φόρτωσης wishlist από τη βάση δεδομένων
    private fun loadWishlist() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("wishlist")
            .whereEqualTo("userid", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                wishlistItems.clear()
                for (doc in snapshot.documents) {
                    val data = doc.data
                    if (data != null) {
                        val product = EshopProduct(
                            name         = data["nameproduct"] as? String ?: "Unknown",
                            description  = data["description"] as? String ?: "",
                            category     = "", // category missing in wishlist, or add if stored
                            imageUrl     = data["imageurl"] as? String ?: "",
                            pricePerUnit = (data["priceperunit"] as? Number)?.toDouble() ?: 0.0,
                            unitType     = data["unittype"] as? String ?: "€/unit",
                            availability = true, // assume true or store it if possible,
                            isOnOffer    = false, // default false, or add if stored,
                            posothta     = (data["posothta"] as? Number)?.toInt() ?: 0
                        )
                        wishlistItems.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.error_loading_wishlist, it.message), Toast.LENGTH_LONG).show()
            }
    }

}
