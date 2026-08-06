package com.symtotrack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class DashboardActivity : BaseActivity() {

    private var nowServing = 14
    private var yourToken = 18
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvNowServing: TextView
    private lateinit var tvYourToken: TextView
    private lateinit var tvAhead: TextView
    private lateinit var tvQueueTime: TextView
    private lateinit var pbQueue: ProgressBar

    private val queueRunnable = object : Runnable {
        override fun run() {
            advanceQueue()
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initQueueViews()
        loadUserGreeting()
        translateDashboardScreen()
        wireCards()
        wireBottomNav()
        updateQueueUI()
        updateVitalsUI()
        handler.postDelayed(queueRunnable, 30_000)
    }

    override fun onResume() {
        super.onResume()
        loadUserGreeting()
        translateDashboardScreen()
        updateQueueUI()
        updateVitalsUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(queueRunnable)
    }

    private fun initQueueViews() {
        tvNowServing = findViewById(R.id.tv_now_serving)
        tvYourToken  = findViewById(R.id.tv_your_token)
        tvAhead      = findViewById(R.id.tv_ahead)
        tvQueueTime  = findViewById(R.id.tv_queue_time)
        pbQueue      = findViewById(R.id.pb_queue)
    }

    private fun advanceQueue() {
        if (nowServing < yourToken) {
            nowServing++
        }
        updateQueueUI()
    }

    private fun updateQueueUI() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val hasActiveAppt = prefs.getBoolean("has_active_appointment", false)
        val cardNoAppt = findViewById<CardView>(R.id.card_no_appointment)
        val cardLiveQueue = findViewById<CardView>(R.id.card_live_queue)

        if (!hasActiveAppt) {
            cardNoAppt?.visibility = android.view.View.VISIBLE
            cardLiveQueue?.visibility = android.view.View.GONE
            findViewById<Button>(R.id.btn_book_now_dash)?.apply {
                text = tr("+ Book Appointment & Get Token")
                setOnClickListener {
                    startActivity(Intent(this@DashboardActivity, NearbyActivity::class.java))
                }
            }
        } else {
            cardNoAppt?.visibility = android.view.View.GONE
            cardLiveQueue?.visibility = android.view.View.VISIBLE

            yourToken = prefs.getInt("your_token", 18)
            nowServing = prefs.getInt("now_serving", 14)

            tvNowServing.text = "#$nowServing"
            tvYourToken.text  = "#$yourToken"
            val ahead = (yourToken - nowServing).coerceAtLeast(0)
            tvAhead.text = "$ahead"
            val estimatedMinutes = ahead * 5
            tvQueueTime.text = if (estimatedMinutes <= 0) tr("Your turn!") else "~$estimatedMinutes min"
            val progress = if (yourToken > 0) ((nowServing.toFloat() / yourToken) * 100).toInt().coerceIn(0, 100) else 100
            pbQueue.progress = progress
        }
    }

    private fun updateVitalsUI() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val activeSymptomCount = prefs.getInt("active_symptom_count", 0)
        val lastSeverity = prefs.getString("last_severity", "Low") ?: "Low"

        val (score, hr, bp) = when (lastSeverity.lowercase()) {
            "high", "severe" -> Triple(62, "88 bpm", "130/85")
            "medium", "moderate" -> Triple(78, "78 bpm", "124/82")
            else -> if (activeSymptomCount == 0) Triple(98, "72 bpm", "118/76") else Triple(88, "74 bpm", "120/78")
        }

        findViewById<TextView>(R.id.tv_heart_rate_val)?.text = hr
        findViewById<TextView>(R.id.tv_bp_val)?.text = bp
        val tvScore = findViewById<TextView>(R.id.tv_health_score_val)
        tvScore?.text = "$score%"
    }

    private fun loadUserGreeting() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val name  = prefs.getString("user_name", "User") ?: "User"

        val rawGreeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Hello"
        }
        val translatedGreeting = tr(rawGreeting)

        findViewById<TextView>(R.id.tv_greeting).text  = tr("SymptoTrack")
        findViewById<TextView>(R.id.tv_user_name).text = "$translatedGreeting, $name 👋"
    }

    private fun translateDashboardScreen() {
        // Translate all card titles dynamically
        val cardEnter = findViewById<CardView>(R.id.card_enter_symptoms)
        ((cardEnter?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Enter Symptoms")
        ((cardEnter?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("AI Analysis")

        val cardScan = findViewById<CardView>(R.id.card_scan_image)
        ((cardScan?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Scan Image")
        ((cardScan?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Prescription/Lab")

        val cardVoice = findViewById<CardView>(R.id.card_voice_input)
        ((cardVoice?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Voice Symptoms")
        ((cardVoice?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Speak Symptoms")

        val cardAlerts = findViewById<CardView>(R.id.card_alerts)
        ((cardAlerts?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Emergency Services")
        ((cardAlerts?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Instant SOS Dial")

        val cardReminders = findViewById<CardView>(R.id.card_reminders)
        ((cardReminders?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Medicine Reminders")
        ((cardReminders?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Pill Schedules")

        val cardFamily = findViewById<CardView>(R.id.card_family)
        ((cardFamily?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Family Profiles")
        ((cardFamily?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Health Records")

        val cardTimeline = findViewById<CardView>(R.id.card_timeline)
        ((cardTimeline?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Symptom History")
        ((cardTimeline?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Past Reports")

        val cardHeatmap = findViewById<CardView>(R.id.card_heatmap)
        ((cardHeatmap?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("Body Heatmap")
        ((cardHeatmap?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Visual Symptoms")

        val cardChat = findViewById<CardView>(R.id.card_ai_chat)
        ((cardChat?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(1) as? TextView)?.text = tr("AI Health Assistant")
        ((cardChat?.getChildAt(0) as? android.view.ViewGroup)?.getChildAt(2) as? TextView)?.text = tr("Chat Support")
    }

    private fun wireCards() {
        findViewById<CardView>(R.id.card_enter_symptoms).setOnClickListener {
            startActivity(Intent(this, SymptomsActivity::class.java))
        }
        findViewById<CardView>(R.id.card_scan_image).setOnClickListener {
            startActivity(Intent(this, ImageScanActivity::class.java))
        }
        findViewById<CardView>(R.id.card_voice_input).setOnClickListener {
            startActivity(Intent(this, VoiceSymptomsActivity::class.java))
        }
        findViewById<CardView>(R.id.card_alerts).setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }
        findViewById<CardView>(R.id.card_reminders)?.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }
        findViewById<CardView>(R.id.card_family)?.setOnClickListener {
            startActivity(Intent(this, FamilyActivity::class.java))
        }
        findViewById<CardView>(R.id.card_timeline)?.setOnClickListener {
            startActivity(Intent(this, TimelineActivity::class.java))
        }
        findViewById<CardView>(R.id.card_heatmap)?.setOnClickListener {
            startActivity(Intent(this, HeatmapActivity::class.java))
        }
        findViewById<CardView>(R.id.card_ai_chat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        findViewById<ImageView>(R.id.btn_theme_toggle)?.setOnClickListener {
            showThemeSelectionDialog()
        }

        val dialEmergency = android.view.View.OnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            startActivity(callIntent)
        }
        findViewById<CardView>(R.id.card_emergency)?.setOnClickListener(dialEmergency)
        findViewById<TextView>(R.id.btn_call_emergency)?.setOnClickListener(dialEmergency)
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(tr("Dark Mode 🌙"), tr("Light Mode ☀️"), tr("System Default 📱"))
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val currentMode = prefs.getInt("theme_mode", 1)
        val selectedIndex = when (currentMode) {
            2 -> 1
            0 -> 2
            else -> 0
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(tr("Select App Theme"))
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val newMode = when (which) {
                    1 -> 2 // Light
                    2 -> 0 // System
                    else -> 1 // Dark
                }

                prefs.edit().putInt("theme_mode", newMode).apply()

                val nightMode = when (newMode) {
                    2 -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    0 -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                }
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton(tr("Cancel"), null)
            .show()
    }

    private fun wireBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
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
                R.id.navigation_map -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}
