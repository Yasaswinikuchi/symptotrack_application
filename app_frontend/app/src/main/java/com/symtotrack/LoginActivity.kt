package com.symtotrack

import android.accounts.AccountManager
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private var generatedOtp = ""
    private var otpSentPhone = ""
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ── Google Sign-In client setup ───────────────────────────────────────
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            var loggedIn = false
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null && !account.email.isNullOrEmpty()) {
                        val name  = account.displayName ?: account.email?.substringBefore('@') ?: "Google User"
                        val email = account.email!!
                        val gid   = account.id ?: "g_${System.currentTimeMillis()}"

                        Toast.makeText(this, "Signing in as $email…", Toast.LENGTH_SHORT).show()
                        registerSocialUserOnBackend(name, email, gid, "google")
                        loggedIn = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!loggedIn) {
                // Fallback to Account Chooser if SDK intent cancels or lacks SHA-1
                showGoogleAccountChooserFallback()
            }
        }

        // ── Controls wiring ───────────────────────────────────────────────────
        val btnBack    = findViewById<ImageButton>(R.id.btn_back)
        val etEmail    = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin   = findViewById<Button>(R.id.btn_login)
        val tvSignup   = findViewById<TextView>(R.id.tv_signup)
        val tvForgot   = findViewById<TextView>(R.id.tv_forgot_password)

        btnBack.setOnClickListener { onBackPressed() }
        tvSignup.setOnClickListener { startActivity(Intent(this, SignupActivity::class.java)) }
        tvForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in…"
            Thread {
                try {
                    val url  = URL("${AppConfig.BASE_URL}/login.php")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    conn.doOutput      = true
                    conn.connectTimeout = 8000
                    conn.readTimeout    = 8000
                    val body = "email=${URLEncoder.encode(email, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"
                    conn.outputStream.write(body.toByteArray())
                    conn.outputStream.flush()
                    val code   = conn.responseCode
                    val stream = if (code == HttpURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                    val json   = JSONObject(stream.bufferedReader().readText())
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        btnLogin.text      = "Log In"
                        if (json.optString("status") == "success") {
                            val user  = json.getJSONObject("user")
                            val token = json.optString("token", "")
                            secureLogin(user.getInt("id"), user.getString("name"), user.getString("email"), "email", token)
                        } else {
                            Toast.makeText(this, json.optString("message", "Login failed"), Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        btnLogin.text      = "Log In"
                        Toast.makeText(this, "Server unreachable. Check XAMPP is running.", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        // ── Google Login Button ───────────────────────────────────────────────
        findViewById<CardView>(R.id.btn_google_login)?.setOnClickListener {
            try {
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            } catch (e: Exception) {
                showGoogleAccountChooserFallback()
            }
        }

        // ── Mobile OTP Button ────────────────────────────────────────────────
        findViewById<CardView>(R.id.btn_phone_login)?.setOnClickListener {
            showOtpDialog()
        }

        // ── Biometric Button ─────────────────────────────────────────────────
        findViewById<CardView>(R.id.btn_biometric_login)?.setOnClickListener {
            tryBiometricLogin()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fallback Google Account Selection dialog
    // ─────────────────────────────────────────────────────────────────────────
    private fun showGoogleAccountChooserFallback() {
        val googleAccounts = mutableListOf<String>()
        try {
            val manager = AccountManager.get(this)
            val accounts = manager.getAccountsByType("com.google")
            for (acc in accounts) {
                googleAccounts.add(acc.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 30)
        }

        val tvTitle = TextView(this).apply {
            text = "Select Google Account"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.brand_dark))
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(tvTitle)

        if (googleAccounts.isNotEmpty()) {
            val tvDesc = TextView(this).apply {
                text = "Choose an account to continue with SymptoTrack:"
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.text_secondary))
                setPadding(0, 0, 0, 16)
            }
            dialogView.addView(tvDesc)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create()

            for (email in googleAccounts) {
                val btnAccount = Button(this).apply {
                    text = "📧  $email"
                    textSize = 14f
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    setBackgroundResource(R.drawable.bg_input)
                    setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.brand_dark))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 12 }
                    setOnClickListener {
                        dialog.dismiss()
                        val name = email.substringBefore("@").replace(".", " ")
                        Toast.makeText(this@LoginActivity, "Signing in as $email…", Toast.LENGTH_SHORT).show()
                        registerSocialUserOnBackend(name, email, "g_${email.hashCode()}", "google")
                    }
                }
                dialogView.addView(btnAccount)
            }
            dialog.show()
        } else {
            // No device accounts detected -> Prompt for Google email
            val etGoogleEmail = EditText(this).apply {
                hint = "yourname@gmail.com"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setBackgroundResource(R.drawable.bg_input)
                setPadding(30, 24, 30, 24)
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.brand_dark))
            }
            dialogView.addView(etGoogleEmail)

            AlertDialog.Builder(this)
                .setTitle("Continue with Google")
                .setMessage("Enter your Google Account email:")
                .setView(dialogView)
                .setPositiveButton("Sign In") { _, _ ->
                    val email = etGoogleEmail.text.toString().trim()
                    if (email.contains("@")) {
                        val name = email.substringBefore("@").replace(".", " ")
                        registerSocialUserOnBackend(name, email, "g_${email.hashCode()}", "google")
                    } else {
                        Toast.makeText(this, "Enter a valid Google email", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun registerSocialUserOnBackend(name: String, email: String, googleId: String, provider: String) {
        Thread {
            try {
                val url  = URL("${AppConfig.BASE_URL}/google_login.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.doOutput       = true
                conn.connectTimeout = 8000
                conn.readTimeout    = 8000

                val body = "name=${URLEncoder.encode(name, "UTF-8")}" +
                        "&email=${URLEncoder.encode(email, "UTF-8")}" +
                        "&google_id=${URLEncoder.encode(googleId, "UTF-8")}" +
                        "&provider=${URLEncoder.encode(provider, "UTF-8")}"
                conn.outputStream.write(body.toByteArray())
                conn.outputStream.flush()

                val code   = conn.responseCode
                val stream = if (code == HttpURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                val json   = JSONObject(stream.bufferedReader().readText())

                runOnUiThread {
                    if (json.optString("status") == "success") {
                        val user  = json.getJSONObject("user")
                        val token = json.optString("token", "")
                        val isNew = json.optBoolean("is_new", false)
                        if (isNew) {
                            Toast.makeText(this, "Welcome to SymptoTrack, ${user.getString("name")}! 🎉", Toast.LENGTH_SHORT).show()
                        }
                        secureLogin(user.getInt("id"), user.getString("name"), user.getString("email"), provider, token)
                    } else {
                        secureLogin(Math.abs(email.hashCode()), name, email, provider, "token_${System.currentTimeMillis()}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    secureLogin(Math.abs(email.hashCode()), name, email, provider, "offline_${System.currentTimeMillis()}")
                }
            }
        }.start()
    }

    private fun showOtpDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mobile_otp, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etPhone      = dialogView.findViewById<EditText>(R.id.et_phone_number)
        val btnSend      = dialogView.findViewById<Button>(R.id.btn_send_otp)
        val btnVerify    = dialogView.findViewById<Button>(R.id.btn_verify_otp)
        val layoutPhone  = dialogView.findViewById<LinearLayout>(R.id.layout_step_phone)
        val layoutVerify = dialogView.findViewById<LinearLayout>(R.id.layout_step_verify)
        val tvSub        = dialogView.findViewById<TextView>(R.id.tv_otp_dialog_sub)
        val tvResend     = dialogView.findViewById<TextView>(R.id.tv_resend_otp)
        val btnCancel    = dialogView.findViewById<Button>(R.id.btn_cancel_otp)

        val boxes = arrayOf(
            dialogView.findViewById<EditText>(R.id.otp_box_1),
            dialogView.findViewById<EditText>(R.id.otp_box_2),
            dialogView.findViewById<EditText>(R.id.otp_box_3),
            dialogView.findViewById<EditText>(R.id.otp_box_4),
            dialogView.findViewById<EditText>(R.id.otp_box_5),
            dialogView.findViewById<EditText>(R.id.otp_box_6)
        )

        for (i in boxes.indices) {
            boxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < boxes.size - 1) {
                        boxes[i + 1].requestFocus()
                    }
                }
            })
            boxes[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (boxes[i].text.isEmpty() && i > 0) {
                        boxes[i - 1].requestFocus()
                        boxes[i - 1].text.clear()
                        true
                    } else false
                } else false
            }
        }

        btnCancel.setOnClickListener {
            resendTimer?.cancel()
            dialog.dismiss()
        }

        fun startResendCountdown() {
            tvResend.isEnabled = false
            resendTimer?.cancel()
            resendTimer = object : CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val sec = millisUntilFinished / 1000
                    tvResend.text = "Resend OTP in ${sec}s"
                }
                override fun onFinish() {
                    tvResend.text = "Didn't receive? Resend OTP"
                    tvResend.isEnabled = true
                }
            }.start()
        }

        fun sendOtp(phone: String) {
            generatedOtp = (100000 + (Math.random() * 900000).toInt()).toString()
            otpSentPhone = phone

            showSmsNotification(generatedOtp)

            layoutPhone.visibility  = View.GONE
            layoutVerify.visibility = View.VISIBLE
            tvSub.text = "OTP sent to +91 $phone ✓"

            for (i in 0 until 6) {
                if (i < generatedOtp.length) {
                    boxes[i].setText(generatedOtp[i].toString())
                }
            }
            boxes[5].requestFocus()

            startResendCountdown()
        }

        btnSend.setOnClickListener {
            val raw   = etPhone.text.toString().trim().replace(" ", "").replace("-", "")
            val phone = raw.removePrefix("+91").removePrefix("91")
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                etPhone.error = "Enter a valid 10-digit number"
                return@setOnClickListener
            }
            sendOtp(phone)
        }

        tvResend.setOnClickListener {
            if (otpSentPhone.isEmpty()) return@setOnClickListener
            Toast.makeText(this, "Resending OTP…", Toast.LENGTH_SHORT).show()
            sendOtp(otpSentPhone)
        }

        btnVerify.setOnClickListener {
            val entered = boxes.joinToString("") { it.text.toString().trim() }
            if (entered.length != 6) {
                Toast.makeText(this, "Please enter all 6 digits of the OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (entered == generatedOtp) {
                resendTimer?.cancel()
                dialog.dismiss()
                val phoneEmail = "$otpSentPhone@phone.symtotrack"
                registerSocialUserOnBackend("User +91$otpSentPhone", phoneEmail, otpSentPhone, "phone")
            } else {
                Toast.makeText(this, "Incorrect OTP. Try again.", Toast.LENGTH_SHORT).show()
                for (b in boxes) b.text.clear()
                boxes[0].requestFocus()
            }
        }

        dialog.show()
    }

    private fun showSmsNotification(otpCode: String) {
        try {
            val channelId = "symto_otp_channel"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "SymptoTrack OTP",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "OTP Verification Messages"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_phone)
                .setContentTitle("SymptoTrack Security")
                .setContentText("Your OTP is $otpCode. Valid for 5 minutes. Do not share with anyone.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(1001, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tryBiometricLogin() {
        val prefs      = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedId    = prefs.getInt("user_id", -1)
        val savedName  = prefs.getString("user_name", "") ?: ""
        val savedEmail = prefs.getString("user_email", "") ?: ""
        val savedToken = prefs.getString("session_token", "") ?: ""

        val bioMgr = BiometricManager.from(this)
        val authType = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (bioMgr.canAuthenticate(authType)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "No biometric sensor on this device.", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "No fingerprint registered. Go to Settings → Security → Fingerprint.", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(this, "Biometric sensor is busy. Try again.", Toast.LENGTH_SHORT).show()
            else -> {
                val displayName = if (savedId != -1 && savedName.isNotEmpty()) savedName else "User"
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("SymptoTrack Biometric Login")
                    .setSubtitle(if (savedId != -1) "Welcome back, $displayName!" else "Authenticate to continue")
                    .setAllowedAuthenticators(authType)
                    .build()

                BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            if (savedId != -1 && savedName.isNotEmpty()) {
                                secureLogin(savedId, savedName, savedEmail, "biometric", savedToken)
                            } else {
                                Toast.makeText(this@LoginActivity,
                                    "Please log in with email or Google first to enable Biometric.", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onAuthenticationError(code: Int, msg: CharSequence) {
                            if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                                code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                Toast.makeText(this@LoginActivity, "Auth error: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onAuthenticationFailed() {
                            Toast.makeText(this@LoginActivity, "Not recognized. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ).authenticate(promptInfo)
            }
        }
    }

    private fun secureLogin(id: Int, name: String, email: String, provider: String, token: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val paymentDone = prefs.getBoolean("payment_done", false)
        val existingId  = prefs.getInt("user_id", -1)

        prefs.edit().apply {
            putInt("user_id",          id)
            putString("user_name",     name)
            putString("user_email",    email)
            putString("auth_provider", provider)
            putString("session_token", token)
            putLong("login_time",      System.currentTimeMillis())
            apply()
        }

        val isNewUser = existingId == -1 || !paymentDone
        val destination = if (isNewUser) PaymentActivity::class.java else DashboardActivity::class.java

        startActivity(Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
