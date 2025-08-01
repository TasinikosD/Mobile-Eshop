package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//δημιουργία κλάσης για ιστορικό
class HistoryFragment : Fragment() {

    //αρχικοποίηση ui στοιχείων
    private lateinit var recyclerView: RecyclerView
    private val ordersWithProducts = mutableListOf<HistoryAdapter.OrderWithProducts>()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val selectWeekButton = view.findViewById<Button>(R.id.selectWeekButton)
        val selectMonthButton = view.findViewById<Button>(R.id.selectMonthButton)

        selectWeekButton.setOnClickListener {
            showDatePicker(forWeek = true)
        }

        selectMonthButton.setOnClickListener {
            showDatePicker(forWeek = false)
        }



        //εμφάνιση όλων των περιεχομένων σε σειρά
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(FirebaseAuth.getInstance().currentUser?.uid ?: "", ordersWithProducts)
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadDeliveryHistory(userId)
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    //Η loadDeliveryHistory φορτώνει τα δεδομένα ιστορικού και προϊόντων από τη Firestore.
    private fun loadDeliveryHistory(userId: String) {
        val db = FirebaseFirestore.getInstance()


        db.collection("history")
            .document(userId)
            .collection("history_details")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No orders yet.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                ordersWithProducts.clear()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()


                for (doc in result) {
                    val orderId = doc.id
                    val deliveryHistory = doc.toObject(DeliveryHistory::class.java)

                    val productsTask = db.collection("history")
                        .document(userId)
                        .collection("history_details")
                        .document(orderId)
                        .collection("products")
                        .get()

                    tasks.add(productsTask)

                    productsTask.addOnSuccessListener { productsSnapshot ->
                        val products = productsSnapshot.map { it.toObject(ProductInHistory::class.java) }

                        val orderWithProducts = HistoryAdapter.OrderWithProducts(
                            orderId,
                            deliveryHistory,
                            products
                        )
                        ordersWithProducts.add(orderWithProducts)

                        if (ordersWithProducts.size == result.size()) {
                            ordersWithProducts.sortByDescending { it.deliveryHistory.timestamp }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT).show()
            }
    }

    //συνάρτηση εμφάνισης calendar για επιλογή βδομάδας ή μήνσ
    private fun showDatePicker(forWeek: Boolean) {
        val today = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selected = Calendar.getInstance()
            selected.set(year, month, day)

            if (forWeek) {
                val week = selected.get(Calendar.WEEK_OF_YEAR)
                val yearStr = selected.get(Calendar.YEAR)
                showSumForWeek(yearStr, week)
            } else {
                val selectedMonth = selected.get(Calendar.MONTH)
                val yearStr = selected.get(Calendar.YEAR)
                showSumForMonth(yearStr, selectedMonth)
            }

        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
    }

    //συνάρτηση εμφάνσιςη τιμής για εβδομάδα
    private fun showSumForWeek(year: Int, week: Int) {
        val total = ordersWithProducts
            .filter {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.deliveryHistory.timestamp
                cal.get(Calendar.WEEK_OF_YEAR) == week && cal.get(Calendar.YEAR) == year
            }
            .sumOf { it.deliveryHistory.sum_price }

        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val startOfWeek = formatter.format(cal.time)
        cal.add(Calendar.DATE, 6)
        val endOfWeek = formatter.format(cal.time)

        Toast.makeText(requireContext(),
            getString(R.string.week_summary, startOfWeek, endOfWeek, total),
            Toast.LENGTH_LONG
        ).show()
    }

    //συνάρτηση εμφάνσιςη τιμής για μήνα
    private fun showSumForMonth(year: Int, month: Int) {
        val total = ordersWithProducts
            .filter {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.deliveryHistory.timestamp
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }
            .sumOf { it.deliveryHistory.sum_price }

        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
        }.time)

        Toast.makeText(requireContext(),
            getString(R.string.month_summary, monthName, year, total),
            Toast.LENGTH_LONG
        ).show()
    }


}
