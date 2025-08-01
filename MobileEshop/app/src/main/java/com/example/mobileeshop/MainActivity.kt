package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

//κλάση για το main
class MainActivity : AppCompatActivity() {

    //ανακατεύθιυνση στο κάτω μέρος της σελίδας
    private lateinit var bottomNav: BottomNavigationView

    private val PREFS_NAME = "app_prefs"
    private val KEY_LANGUAGE = "language"
    private val KEY_JUST_SWITCHED = "just_switched"

    override fun onCreate(savedInstanceState: Bundle?) {
        // φόρτωση γλώσσας που έχει σωθεί
        val lang = getSavedLanguage(this)
        setLocale(lang, restartActivity = false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // σωστό icon
        updateFlagIcon(lang)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val justSwitched = prefs.getBoolean(KEY_JUST_SWITCHED, false)

        if (justSwitched) {
            // επανεμφάνιση αρχικής μετά την αλλαγή γλώσσας
            prefs.edit().putBoolean(KEY_JUST_SWITCHED, false).apply()
            loadFragment(HomeFragment())
            bottomNav.post {
                bottomNav.selectedItemId = R.id.home
            }
        } else if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        //ανακατεύθυνση στις σελίδες με bottomnavbar
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.message -> {
                    loadFragment(ShopFragment())
                    true
                }
                R.id.settings -> {
                    loadFragment(UserFragment())
                    true
                }
                R.id.language -> {
                    toggleLanguage()
                    true
                }
                else -> false
            }
        }
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    //συνάρτηση αλλαγής γλώσσας
    private fun toggleLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentLang = getSavedLanguage(this)
        val newLang = if (currentLang == "el") "en" else "el"
        saveLanguagePref(newLang)
        prefs.edit().putBoolean(KEY_JUST_SWITCHED, true).apply()
        recreate()
    }

    private fun setLocale(languageCode: String, restartActivity: Boolean = true) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        if (restartActivity) {
            recreate()
        }
    }

    //λήψη γλώσσας που έχει σωθεί ως προτειμόμενη
    private fun saveLanguagePref(languageCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    //λήψη γλώσσας που έχει σωθεί
    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    //αναβάθμιση flag icon
    private fun updateFlagIcon(lang: String) {
        val icon = if (lang == "el") R.drawable.uk else R.drawable.greek
        bottomNav.menu.findItem(R.id.language).icon = getDrawable(icon)
    }
}
