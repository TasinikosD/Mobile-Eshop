package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

//address activity class
class AddressActivity : AppCompatActivity() {


    //αρχικοποίηση ui στοιχείων
    private lateinit var fullNameEditText: EditText
    private lateinit var streetAddressEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var postalCodeEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var paymentMethodGroup: RadioGroup
    private lateinit var deliveryMethodGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        streetAddressEditText = findViewById(R.id.streetAddressEditText)
        cityEditText = findViewById(R.id.cityEditText)
        postalCodeEditText = findViewById(R.id.postalCodeEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        continueButton = findViewById(R.id.continueButton)
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup)
        deliveryMethodGroup = findViewById(R.id.deliveryMethodGroup)

        //με το πάτημα το κουμπιού γίνεται έλγχος και αν όλα είναι καλά προχωράει στην ολοκλήρωση
        continueButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val street = streetAddressEditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()
            val postalCode = postalCodeEditText.text.toString().trim()
            val phone = phoneNumberEditText.text.toString().trim()

            val selectedPaymentId = paymentMethodGroup.checkedRadioButtonId
            val selectedDeliveryId = deliveryMethodGroup.checkedRadioButtonId

            if (fullName.isEmpty() || street.isEmpty() || city.isEmpty() ||
                postalCode.isEmpty() || phone.isEmpty()
            ) {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPaymentId == -1 || selectedDeliveryId == -1) {
                Toast.makeText(this, getString(R.string.please_select_payment_delivery), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod = findViewById<RadioButton>(selectedPaymentId).text.toString()
            val deliveryMethod = findViewById<RadioButton>(selectedDeliveryId).text.toString()

            if (paymentMethod.contains("Card", ignoreCase = true) && !isPOSAvailable()) {
                Toast.makeText(this, getString(R.string.card_payment_pos_only), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var extraCost = 0.0
            if (paymentMethod.contains("Cash", true)) extraCost += 2.5
            if (deliveryMethod.contains("ACS", true)) extraCost += 3.5
            else if (deliveryMethod.contains("ELTA", true)) extraCost += 1.5

            val baseTotal = CartManager.getTotalPrice()
            val finalTotal = baseTotal + extraCost

            saveOrderToFirestore(
                fullName, street, city, postalCode, phone,
                paymentMethod, deliveryMethod, finalTotal
            )
        }
    }

    private fun isPOSAvailable(): Boolean = true

    //συνάρτηση για αποθήκευση δεδομένων στη βάση
    private fun saveOrderToFirestore(
        fullName: String,
        street: String,
        city: String,
        postalCode: String,
        phone: String,
        paymentMethod: String,
        deliveryMethod: String,
        finalTotal: Double
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartItems = CartManager.getCartItems()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.cart_is_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val historyRef = db.collection("history").document(userId)
        val detailRef = historyRef.collection("history_details").document()

        val deliveryData = hashMapOf(
            "address" to "$fullName, $street",
            "city" to city,
            "postal" to postalCode,
            "phone" to phone,
            "payment" to paymentMethod,
            "delivery" to deliveryMethod,
            "sum_price" to finalTotal,
            "userID" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.runBatch { batch ->
            batch.set(detailRef, deliveryData)

            for (product in cartItems) {
                val orderProductRef = detailRef.collection("products").document()
                val productData = hashMapOf(
                    "name" to (product.name ?: "Unnamed"),
                    "price" to (product.pricePerUnit ?: 0.0),
                    "quantity" to (product.posothta ?: 0)
                )
                batch.set(orderProductRef, productData)
            }
            //Με επιτυχία, το καλάθι αδειάζει, εμφανίζεται μήνυμα και ο χρήστης μεταφέρεται στη σελίδα ευχαριστίας.
        }.addOnSuccessListener {
            CartManager.clearCart()
            Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ThankYouActivity::class.java))
            finish()
            //μήνυμα λάθους
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Batch failed", e)
            Toast.makeText(this, getString(R.string.order_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }
}
