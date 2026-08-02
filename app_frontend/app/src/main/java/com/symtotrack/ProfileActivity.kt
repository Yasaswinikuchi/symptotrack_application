package com.symtotrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : BaseActivity() {

    private lateinit var tvThemeLabel: TextView
    private lateinit var tvLanguageLabel: TextView

    private val worldLanguages = arrayOf(
        "Telugu (తెలుగు) 🇮🇳",
        "English (US) 🌐",
        "English (UK) 🇬🇧",
        "Hindi (हिंदी) 🇮🇳",
        "Tamil (தமிழ்) 🇮🇳",
        "Kannada (ಕನ್ನಡ) 🇮🇳",
        "Malayalam (മലയാളം) 🇮🇳",
        "Bengali (বাংলা) 🇮🇳",
        "Marathi (मराठी) 🇮🇳",
        "Gujarati (ગુજરાતી) 🇮🇳",
        "Punjabi (ਪੰਜਾਬੀ) 🇮🇳",
        "Urdu (اردو) 🇵🇰",
        "Spanish (Español) 🇪🇸",
        "French (Français) 🇫🇷",
        "German (Deutsch) 🇩🇪",
        "Mandarin Chinese (中文) 🇨🇳",
        "Japanese (日本語) 🇯🇵",
        "Korean (한국어) 🇰🇷",
        "Arabic (العربية) 🇸🇦",
        "Russian (Русский) 🇷🇺",
        "Portuguese (Português) 🇧🇷",
        "Italian (Italiano) 🇮🇹",
        "Turkish (Türkçe) 🇹🇷",
        "Vietnamese (Tiếng Việt) 🇻🇳",
        "Thai (ไทย) 🇹🇭",
        "Indonesian (Bahasa Indonesia) 🇮🇩",
        "Swahili (Kiswahili) 🇰🇪",
        "Polish (Polski) 🇵🇱",
        "Dutch (Nederlands) 🇳🇱",
        "Greek (Ελληνικά) 🇬🇷",
        "Hebrew (עברית) 🇮🇱",
        "Czech (Čeština) 🇨🇿",
        "Romanian (Română) 🇷🇴",
        "Swedish (Svenska) 🇸🇪",
        "Hungarian (Magyar) 🇭🇺",
        "Filipino / Tagalog 🇵🇭",
        "Persian / Farsi (فارسی) 🇮🇷",
        "Ukrainian (Українська) 🇺🇦",
        "Danish (Dansk) 🇩🇰",
        "Finnish (Suomi) 🇫🇮",
        "Norwegian (Norsk) 🇳🇴"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvThemeLabel    = findViewById(R.id.tv_current_theme_label)
        tvLanguageLabel = findViewById(R.id.tv_current_language_label)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_symptoms -> {
                    startActivity(Intent(this, SymptomsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> true
                R.id.navigation_map -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        fetchProfile()
        updateThemeLabel()
        updateLanguageLabel()
        translateProfileScreen()

        findViewById<LinearLayout>(R.id.btn_personal_info).setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btn_medical_records).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btn_privacy).setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btn_app_theme).setOnClickListener {
            showThemeSelectionDialog()
        }

        findViewById<LinearLayout>(R.id.btn_app_language).setOnClickListener {
            showWorldLanguageSelectionDialog()
        }

        findViewById<LinearLayout>(R.id.btn_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<TextView>(R.id.btn_logout).setOnClickListener {
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun translateProfileScreen() {
        findViewById<TextView>(R.id.tv_header)?.text = tr("Profile & Settings")
        
        // Translate setting item titles
        val btnPersonal = findViewById<LinearLayout>(R.id.btn_personal_info)
        (btnPersonal.getChildAt(1) as? TextView)?.text = tr("Personal Information")

        val btnMedical = findViewById<LinearLayout>(R.id.btn_medical_records)
        (btnMedical.getChildAt(1) as? TextView)?.text = tr("Medical Records")

        val btnPrivacy = findViewById<LinearLayout>(R.id.btn_privacy)
        (btnPrivacy.getChildAt(1) as? TextView)?.text = tr("Privacy & Security")

        val btnTheme = findViewById<LinearLayout>(R.id.btn_app_theme)
        (btnTheme.getChildAt(1) as? TextView)?.text = tr("App Theme")

        val btnLang = findViewById<LinearLayout>(R.id.btn_app_language)
        (btnLang.getChildAt(1) as? TextView)?.text = tr("App Language")

        val btnHelp = findViewById<LinearLayout>(R.id.btn_help)
        (btnHelp.getChildAt(1) as? TextView)?.text = tr("Help & Support")

        findViewById<TextView>(R.id.btn_logout)?.text = tr("Log Out")
    }

    private fun fetchProfile() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "Unknown User")
        val email = sharedPref.getString("user_email", "user@example.com")
        
        findViewById<TextView>(R.id.tv_profile_name).text = name
        findViewById<TextView>(R.id.tv_profile_email).text = email
    }

    private fun updateThemeLabel() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val mode = prefs.getInt("theme_mode", 1)
        tvThemeLabel.text = when (mode) {
            2 -> tr("Light Mode ☀️")
            0 -> tr("System Default 📱")
            else -> tr("Dark Mode 🌙")
        }
    }

    private fun updateLanguageLabel() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val lang  = prefs.getString("app_language", "Telugu (తెలుగు) 🇮🇳") ?: "Telugu (తెలుగు) 🇮🇳"
        tvLanguageLabel.text = lang
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(tr("Dark Mode 🌙"), tr("Light Mode ☀️"), tr("System Default 📱"))
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentMode = prefs.getInt("theme_mode", 1)
        val selectedIndex = when (currentMode) {
            2 -> 1
            0 -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(tr("Select App Theme"))
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val newMode = when (which) {
                    1 -> 2 // Light
                    2 -> 0 // System
                    else -> 1 // Dark
                }

                prefs.edit().putInt("theme_mode", newMode).apply()
                updateThemeLabel()

                val nightMode = when (newMode) {
                    2 -> AppCompatDelegate.MODE_NIGHT_NO
                    0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else -> AppCompatDelegate.MODE_NIGHT_YES
                }
                AppCompatDelegate.setDefaultNightMode(nightMode)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton(tr("Cancel"), null)
            .show()
    }

    private fun showWorldLanguageSelectionDialog() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val etSearch = EditText(this).apply {
            hint = "🔍 Search language (e.g. Telugu, Spanish, Hindi)..."
            textSize = 14f
            setBackgroundResource(R.drawable.bg_input)
            setPadding(30, 24, 30, 24)
            setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.brand_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 20 }
        }
        dialogView.addView(etSearch)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, worldLanguages.toMutableList())
        val listView = ListView(this).apply {
            this.adapter = adapter
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                800
            )
        }
        dialogView.addView(listView)

        val dialog = AlertDialog.Builder(this)
            .setTitle(tr("Select App Language 🌐"))
            .setView(dialogView)
            .setNegativeButton(tr("Cancel"), null)
            .create()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLang = adapter.getItem(position) ?: worldLanguages[0]
            getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit().putString("app_language", selectedLang).apply()
            dialog.dismiss()
            Toast.makeText(this, "App language updated: $selectedLang", Toast.LENGTH_SHORT).show()
            recreate()
        }

        dialog.show()
    }
}
