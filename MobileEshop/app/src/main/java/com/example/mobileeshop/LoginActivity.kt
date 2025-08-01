package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

//κλάση για login
class LoginActivity : AppCompatActivity() {

    //αρχικοποίηση ui στοιχείων
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    private lateinit var languageToggle: ImageView
    private lateinit var rememberMeCheckbox: CheckBox

    override fun attachBaseContext(newBase: Context) {
        val lang = getSavedLanguage(newBase)
        val localeUpdatedContext = updateLocale(newBase, lang)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        //κουμπιά σελίδας
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToRegisterButton = findViewById<Button>(R.id.goToRegisterButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        languageToggle = findViewById(R.id.languageToggle)

        val currentLang = getSavedLanguage(this)
        updateLanguageToggleImage(currentLang)

        //εναλλάγή γλωσσών
        languageToggle.setOnClickListener {
            val newLang = if (getSavedLanguage(this) == "el") "en" else "el"
            saveLanguagePref(this, newLang)
            val intent = intent
            finish()
            startActivity(intent)
        }

        // αποθηκευμένα credentials αν υπάρχουν
        val savedEmail = prefs.getString("saved_email", "")
        val savedPassword = prefs.getString("saved_password", "")
        val isRemembered = prefs.getBoolean("remember_me", false)

        if (isRemembered && !savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            emailInput.setText(savedEmail)
            passwordInput.setText(savedPassword)
            rememberMeCheckbox.isChecked = true
        }

        //έλεγχος στοιχείων στο login και είσοδος
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    if (rememberMeCheckbox.isChecked) {
                        prefs.edit()
                            .putString("saved_email", email)
                            .putString("saved_password", password)
                            .putBoolean("remember_me", true)
                            .apply()
                    } else {
                        prefs.edit()
                            .remove("saved_email")
                            .remove("saved_password")
                            .putBoolean("remember_me", false)
                            .apply()
                    }

                    // Check Firestore for user existence
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val username = doc.getString("username") ?: getString(R.string.user)
                                Toast.makeText(this, getString(R.string.welcome_back, username), Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, getString(R.string.failed_to_fetch_user), Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.invalid_email_or_password), Toast.LENGTH_SHORT).show()
                }
        }

        goToRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // αλλαγή κωδικού μέσω mail
        changePasswordButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_email_to_reset), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.failed_to_send_reset), Toast.LENGTH_SHORT).show()
                }
        }
    }

    //συνάρτηση αλλαγής γλώσσας
    private fun updateLanguageToggleImage(lang: String) {
        if (lang == "el") {
            languageToggle.setImageResource(R.drawable.uk)
        } else {
            languageToggle.setImageResource(R.drawable.greek)
        }
    }

    //συνάρτηση προτιμώμενης γλώσσας
    private fun saveLanguagePref(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
    }

    //λήψη γλώσσας που είναι αποθηκευμένη ως τελευταία
    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }


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
