package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI
    private lateinit var languageToggle: ImageView


    //εισαγωγή σωζόμενων locale
    override fun attachBaseContext(newBase: Context) {
        val lang = getSavedLanguage(newBase)
        val localeUpdatedContext = updateLocale(newBase, lang)
        super.attachBaseContext(localeUpdatedContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase instances
        auth  = FirebaseAuth.getInstance()
        db    = FirebaseFirestore.getInstance()

        // εναλλαγή γλώσσας
        languageToggle = findViewById(R.id.languageToggle)
        val currentLang = getSavedLanguage(this)
        updateFlagIcon(currentLang)

        //clicklistener για αλλαγή γλώσσας
        languageToggle.setOnClickListener {
            val newLang = if (getSavedLanguage(this) == "el") "en" else "el"
            saveLanguagePref(this, newLang)
            val intent = intent
            finish()
            startActivity(intent)
        }

        // πεδία φόρμας
        val usernameInput     = findViewById<EditText>(R.id.usernameInput)
        val emailInput        = findViewById<EditText>(R.id.emailInput)
        val passwordInput     = findViewById<EditText>(R.id.passwordInput)
        val registerButton    = findViewById<Button>(R.id.registerButton)
        val goToLoginButton   = findViewById<Button>(R.id.goToLoginButton)

        //κουμπί για εγγραφή χρήστη
        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email    = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    val userMap = hashMapOf(
                        "username" to username,
                        "email"    to email
                    )

                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, getString(R.string.failed_to_save_user, e.message), Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.registration_failed, e.message), Toast.LENGTH_LONG).show()
                }
        }

        //ανακατεύθιυνση σε login page
        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    //συνάρτηση αλλαγής γλώσσας
    private fun updateFlagIcon(lang: String) {
        val iconRes = if (lang == "el") R.drawable.uk else R.drawable.greek
        languageToggle.setImageResource(iconRes)
    }


    private fun saveLanguagePref(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
    }

    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    //αναβάθμιση του locale
    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
