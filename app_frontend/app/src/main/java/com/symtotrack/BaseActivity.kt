package com.symtotrack

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val updatedContext = TranslationUtils.updateLocale(newBase)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyUserTheme()
        super.onCreate(savedInstanceState)
    }

    private fun applyUserTheme() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val mode = prefs.getInt("theme_mode", 1) // Default 1 (Dark Mode)
        val nightMode = when (mode) {
            2 -> AppCompatDelegate.MODE_NIGHT_NO
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_YES
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    fun tr(text: String): String {
        return TranslationUtils.translate(this, text)
    }
}
