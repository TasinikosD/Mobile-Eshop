package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.google.firebase.firestore.FirebaseFirestore

//κλάση για εμφάνιση προϊόντων καταστήματος
class ShopFragment : Fragment(R.layout.fragment_shop) {

    private lateinit var rv             : RecyclerView
    private lateinit var adapter        : ProductAdapter
    private lateinit var searchView     : SearchView
    private lateinit var spinner        : Spinner
    private lateinit var offersBox      : CheckBox
    private lateinit var availabilityBox: CheckBox
    private lateinit var slider         : RangeSlider
    private lateinit var priceLabel     : TextView

    private val allProducts       = mutableListOf<EshopProduct>()
    private val displayedProducts = mutableListOf<EshopProduct>()

    private var currentSearch   = ""
    private var currentCategory = ""
    private var onlyOffers      = false
    private var onlyAvailable   = false
    private var minPrice        = 0f
    private var maxPrice        = 100f

    private lateinit var localizedToDbCategory: Map<String, String>
    private lateinit var categories: List<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Bind views
        rv               = view.findViewById(R.id.recyclerView)
        searchView       = view.findViewById(R.id.searchView)
        spinner          = view.findViewById(R.id.categorySpinner)
        offersBox        = view.findViewById(R.id.offersCheckBox)
        availabilityBox  = view.findViewById(R.id.availabilityCheckBox)
        slider           = view.findViewById(R.id.priceRangeSlider)
        priceLabel       = view.findViewById(R.id.priceRangeLabel)

        // 2) Localization Map
        localizedToDbCategory = mapOf(
            getString(R.string.cat_all) to "Όλα",
            getString(R.string.cat_fresh_food) to "Φρέσκα Τρόφιμα",
            getString(R.string.cat_dairy) to "Γαλακτοκομικά",
            getString(R.string.cat_frozen) to "Κατεψυγμένα",
            getString(R.string.cat_cleaning) to "Καθαριστικά"
        )
        categories = localizedToDbCategory.keys.toList()
        currentCategory = getString(R.string.cat_all)

        // 3) RecyclerView Setup
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(displayedProducts)
        rv.adapter = adapter

        // 4) UI Listeners
        offersBox.setOnCheckedChangeListener { _, checked ->
            onlyOffers = checked
            applyFilters()
        }

        availabilityBox.setOnCheckedChangeListener { _, checked ->
            onlyAvailable = checked
            applyFilters()
        }

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentCategory = categories[position]
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearch = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // 5) Price Slider
        slider.apply {
            valueFrom = 0f
            valueTo = 100f
            stepSize = 1f
            values = listOf(minPrice, maxPrice)
            addOnChangeListener { slider, _, _ ->
                minPrice = slider.values[0]
                maxPrice = slider.values[1]
                priceLabel.text = getString(R.string.price_range_label, minPrice, maxPrice)
                applyFilters()
            }
        }

        // 6) Load products from Firestore
        FirebaseFirestore.getInstance()
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                allProducts.clear()
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val prod = EshopProduct(
                        id           = doc.id, // ← Add Firestore document ID here
                        name         = data["name"]        as? String ?: "",
                        description  = data["description"] as? String ?: "",
                        category     = data["category"]    as? String ?: "",
                        imageUrl     = data["imageUrl"]    as? String ?: "",
                        pricePerUnit = (data["pricePerUnit"] as? Number)?.toDouble() ?: 0.0,
                        unitType     = data["unitType"]    as? String ?: "",
                        availability = data["availability"] as? Boolean ?: false,
                        isOnOffer    = data["isOnOffer"]   as? Boolean ?: false,
                        posothta     = (data["posothta"] as? Number)?.toInt() ?: 0
                    )

                    allProducts.add(prod)
                }
                applyFilters()
            }
    }

    private fun applyFilters() {
        val dbCategory = localizedToDbCategory[currentCategory] ?: "Όλα"

        val filtered = allProducts.filter { product ->
            if (onlyOffers && !product.isOnOffer) return@filter false
            if (onlyAvailable && !product.availability) return@filter false
            if (dbCategory != "Όλα" && product.category != dbCategory) return@filter false

            val matchesSearch = product.name?.contains(currentSearch, true) == true ||
                    product.description?.contains(currentSearch, true) == true
            if (!matchesSearch) return@filter false

            val price = product.pricePerUnit?.toFloat()
            price?.let { it in minPrice..maxPrice } == true
        }

        displayedProducts.apply {
            clear()
            addAll(filtered)
        }
        adapter.notifyDataSetChanged()
    }
}
