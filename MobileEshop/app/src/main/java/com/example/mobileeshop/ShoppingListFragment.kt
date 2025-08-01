package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore


class ShoppingListFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: CartProductAdapter
    private lateinit var totalLabel: TextView
    private lateinit var btnContinue: Button
    private lateinit var emptyCartMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_shopping_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.recyclerView)
        totalLabel = view.findViewById(R.id.totalPriceLabel)
        btnContinue = view.findViewById(R.id.btnContinue)
        emptyCartMessage = view.findViewById(R.id.emptyCartMessage)

        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartProductAdapter(
            CartManager.getCartItems(),
            onDeleteClicked = { product ->
                CartManager.remove(product)
                adapter.notifyDataSetChanged()
                updateTotalPrice()
                checkEmptyCartUI()
            },
            onQuantityChanged = {
                updateTotalPrice()
            }
        )
        rv.adapter = adapter

        updateTotalPrice()
        checkEmptyCartUI()

        btnContinue.setOnClickListener {
            checkStockAndProceed()
        }
    }

    //συνάρτηση ενημέρωσης τιμής
    private fun updateTotalPrice() {
        val total = CartManager.getTotalPrice()
        totalLabel.text = getString(R.string.total_price_format, total)
    }

    //έλεγχος για λαδειο καλάθι
    private fun checkEmptyCartUI() {
        val isEmpty = CartManager.getCartItems().isEmpty()
        btnContinue.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyCartMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (isEmpty) {
            emptyCartMessage.text = getString(R.string.empty_cart_message)
        }
    }

    //έλεγχος strock για συνέχεια
    private fun checkStockAndProceed() {
        val products = CartManager.getCartItems()
        val db = FirebaseFirestore.getInstance()
        val errors = mutableListOf<String>()

        val tasks = products.map { product ->
            db.collection("products")
                .whereEqualTo("name", product.name)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        errors.add(getString(R.string.product_not_found_in_db, product.name))
                    } else {
                        val stockQty = (snapshot.documents[0].getLong("posothta") ?: 0L).toInt()
                        val requestedQty = product.posothta ?: 1
                        if (requestedQty > stockQty) {
                            errors.add(getString(R.string.not_enough_stock, product.name, requestedQty, stockQty))
                        }
                    }
                }
        }

        //εάν όλα είναι σωστά προχωράμε παρακάτω
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            if (errors.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_products_in_stock), Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), AddressActivity::class.java)
                intent.putExtra("cartTotal", CartManager.getTotalPrice())
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), errors.joinToString("\n"), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        updateTotalPrice()
        checkEmptyCartUI()
    }
}
