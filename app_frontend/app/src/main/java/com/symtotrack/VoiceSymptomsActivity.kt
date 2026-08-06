package com.symtotrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class VoiceSymptomsActivity : BaseActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var tvTranscript: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var fabMic: FloatingActionButton
    private lateinit var btnAnalyze: MaterialButton

    private var isListening = false
    private var fullTranscript = ""

    // Auto-restart when a pause is detected – keeps mic open continuously
    private val handler = Handler(Looper.getMainLooper())
    private var restartPending = false

    // Pulse animation handler
    private val pulseHandler = Handler(Looper.getMainLooper())
    private var pulseRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_symptoms)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        tvTranscript = findViewById(R.id.tv_transcript)
        tvTitle      = findViewById(R.id.tv_title)
        fabMic       = findViewById(R.id.fab_mic)
        btnAnalyze   = findViewById(R.id.btn_analyze)

        // Optional subtitle view – may not exist in older layouts
        tvSubtitle = try { findViewById(R.id.tv_subtitle) } catch (e: Exception) { TextView(this) }

        setupBottomNavigation()
        checkPermissionAndSetup()

        fabMic.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
                return@setOnClickListener
            }
            if (isListening) stopListening() else startListening()
        }

        btnAnalyze.setOnClickListener {
            if (fullTranscript.isBlank()) {
                Toast.makeText(this, "Please record some symptoms first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            analyzeVoiceSymptoms(fullTranscript)
        }
    }

    // ── Permission ────────────────────────────────────────────────────────────
    private fun checkPermissionAndSetup() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        } else {
            buildRecognizer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buildRecognizer()
        } else {
            Toast.makeText(this,
                "Microphone permission is required for Voice Symptoms", Toast.LENGTH_LONG).show()
        }
    }

    // ── Build / rebuild the recognizer ────────────────────────────────────────
    /**
     * We always destroy and re-create the recognizer before each session.
     * Android's SpeechRecognizer becomes unusable after an error; creating a
     * fresh instance is the safest fix.
     */
    private fun buildRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    // ── Recognition listener ──────────────────────────────────────────────────
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            restartPending = false
            runOnUiThread {
                tvTitle.text = "🎤 Listening…"
                fabMic.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444"))
                startPulse()
            }
        }

        override fun onBeginningOfSpeech() {
            runOnUiThread { tvTitle.text = "🎙️ Speaking detected…" }
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            runOnUiThread { tvTitle.text = "⏳ Processing…" }
        }

        override fun onError(error: Int) {
            runOnUiThread {
                stopPulse()
                when (error) {
                    // ── Transient errors – just restart the mic ──
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        // Don't show any error; silently restart so mic stays on
                        if (isListening && !restartPending) {
                            restartPending = true
                            tvTitle.text = "🎤 Listening…"
                            handler.postDelayed({
                                if (isListening) {
                                    buildRecognizer()   // fresh instance
                                    startListeningIntent()
                                }
                            }, 300)
                        }
                    }

                    // ── Busy – wait a bit longer and retry ──
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        if (isListening && !restartPending) {
                            restartPending = true
                            handler.postDelayed({
                                if (isListening) {
                                    buildRecognizer()
                                    startListeningIntent()
                                }
                            }, 800)
                        }
                    }

                    // ── Network issues – inform user but keep listening ──
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        Toast.makeText(this@VoiceSymptomsActivity,
                            "Network issue – switching to offline mode", Toast.LENGTH_SHORT).show()
                        if (isListening && !restartPending) {
                            restartPending = true
                            handler.postDelayed({
                                if (isListening) {
                                    buildRecognizer()
                                    startListeningIntent()
                                }
                            }, 500)
                        }
                    }

                    // ── Fatal errors – stop ──
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        isListening = false
                        tvTitle.text = "Tap to start speaking"
                        fabMic.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                        Toast.makeText(this@VoiceSymptomsActivity,
                            "Microphone permission denied", Toast.LENGTH_LONG).show()
                    }

                    else -> {
                        // Any other error – restart silently
                        if (isListening && !restartPending) {
                            restartPending = true
                            handler.postDelayed({
                                if (isListening) {
                                    buildRecognizer()
                                    startListeningIntent()
                                }
                            }, 400)
                        }
                    }
                }
            }
        }

        override fun onResults(results: Bundle?) {
            runOnUiThread {
                stopPulse()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0].trim()
                    if (text.isNotEmpty()) {
                        fullTranscript = if (fullTranscript.isEmpty()) text
                                         else "$fullTranscript $text"
                        tvTranscript.text = fullTranscript
                        tvTranscript.setTextColor(Color.parseColor("#0F172A"))
                        tvTranscript.setTypeface(null, android.graphics.Typeface.NORMAL)
                        btnAnalyze.isEnabled = true
                        btnAnalyze.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                    }
                }
                // Auto-restart so the mic stays open for continued speech
                if (isListening && !restartPending) {
                    restartPending = true
                    tvTitle.text = "🎤 Listening…"
                    handler.postDelayed({
                        if (isListening) {
                            buildRecognizer()
                            startListeningIntent()
                        }
                    }, 200)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            runOnUiThread {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val live = if (fullTranscript.isEmpty()) matches[0]
                               else "$fullTranscript ${matches[0]}"
                    tvTranscript.text = live
                    tvTranscript.setTextColor(Color.parseColor("#374151"))
                    tvTranscript.setTypeface(null, android.graphics.Typeface.ITALIC)
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // ── Start / Stop ──────────────────────────────────────────────────────────
    private fun startListening() {
        isListening = true
        buildRecognizer()  // always fresh instance
        startListeningIntent()
        tvTitle.text = "🎤 Listening…"
        fabMic.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444"))
        startPulse()
    }

    private fun startListeningIntent() {
        restartPending = false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                     RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // ── Key fix: extend silence timeouts so mic doesn't cut off ──
            // Give user 5 seconds of silence before auto-stopping
            putExtra("android.speech.extra.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
            // Wait up to 3s for user to start speaking
            putExtra("android.speech.extra.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 3000L)
            // Max listen time: 60 seconds per session
            putExtra("android.speech.extra.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 60000L)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            // Recognizer not ready; rebuild and retry once
            handler.postDelayed({
                buildRecognizer()
                try { speechRecognizer?.startListening(intent) } catch (ignored: Exception) {}
            }, 500)
        }
    }

    private fun stopListening() {
        isListening = false
        restartPending = false
        handler.removeCallbacksAndMessages(null)
        speechRecognizer?.stopListening()
        stopPulse()
        tvTitle.text = "Tap to start speaking"
        fabMic.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
    }

    // ── Pulse animation on FAB while listening ────────────────────────────────
    private fun startPulse() {
        if (pulseRunning) return
        pulseRunning = true
        pulseTick()
    }

    private fun pulseTick() {
        if (!pulseRunning) return
        fabMic.animate().scaleX(1.15f).scaleY(1.15f).setDuration(400).withEndAction {
            fabMic.animate().scaleX(1f).scaleY(1f).setDuration(400).withEndAction {
                pulseTick()
            }.start()
        }.start()
    }

    private fun stopPulse() {
        pulseRunning = false
        pulseHandler.removeCallbacksAndMessages(null)
        fabMic.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
    }

    // ── Analyze ───────────────────────────────────────────────────────────────
    private fun analyzeVoiceSymptoms(symptomsText: String) {
        if (isListening) stopListening()
        btnAnalyze.isEnabled = false
        btnAnalyze.text = "Analyzing…"

        Thread {
            try {
                val url = java.net.URL("${AppConfig.BASE_URL}/analyze.php")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.doOutput = true

                val json = JSONObject().apply {
                    put("symptomsText", symptomsText)
                    put("age", "Unknown")
                    put("gender", "Unknown")
                    put("conditions", JSONArray())
                }
                connection.outputStream.use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }

                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    runOnUiThread {
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "Analyze Voice Input"
                        startActivity(
                            Intent(this, AnalysisResultActivity::class.java)
                                .putExtra("analysis_result", response)
                        )
                    }
                } else {
                    runOnUiThread {
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "Analyze Voice Input"
                        Toast.makeText(this,
                            "Server error: ${connection.responseCode}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnAnalyze.isEnabled = true
                    btnAnalyze.text = "Analyze Voice Input"
                    Toast.makeText(this,
                        "Cannot reach server. Check your connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────────
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_symptoms
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java)); finish(); true
                }
                R.id.navigation_symptoms -> true
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0); true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0, 0); true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); true
                }
                else -> false
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onPause() {
        super.onPause()
        if (isListening) stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        pulseHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
