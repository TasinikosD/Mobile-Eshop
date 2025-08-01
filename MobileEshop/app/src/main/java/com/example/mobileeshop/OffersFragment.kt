package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.R.attr.data
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

//κλάση προσφορών
class OffersFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ProductAdapter

    // φόρτωμα όλων των προϊόντων
    private val allProducts = mutableListOf<EshopProduct>()
    // και εμφάνιση μόβο προσφορών
    private val displayedProducts = mutableListOf<EshopProduct>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_offers, container, false)

        // setup για εμφάνιση με recycler
        rv = view.findViewById(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(displayedProducts)
        rv.adapter = adapter

        loadOffersFromFirestore()
        return view
    }

    //συνάρτηση φόρτωσης προϊόντων από τη βάση
    private fun loadOffersFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("products")
            .get()
            .addOnSuccessListener { snap ->
                allProducts.clear()
                for (doc in snap.documents) {
                    doc.data?.let { m ->
                        allProducts += EshopProduct(
                            name         = m["name"]          as? String  ?: "Άγνωστο",
                            description  = m["description"]   as? String  ?: "",
                            category     = m["category"]      as? String  ?: "Άγνωστη",
                            imageUrl     = m["imageUrl"]      as? String  ?: "",
                            pricePerUnit = (m["pricePerUnit"] as? Number)?.toDouble() ?: 0.0,
                            unitType     = m["unitType"]      as? String  ?: "€/μονάδα",
                            availability = m["availability"]  as? Boolean ?: true,
                            isOnOffer    = m["isOnOffer"]     as? Boolean ?: false,
                            posothta     = (m["posothta"] as? Number)?.toInt() ?: 0
                        )
                    }
                }
                applyFilters()
            }
    }

    //συνάρτηση για φίλτρα στα προϊόντα
    private fun applyFilters() {
        val offersOnly = allProducts.filter { it.isOnOffer }
        displayedProducts.apply {
            clear()
            addAll(offersOnly)
        }
        adapter.notifyDataSetChanged()
    }
}
