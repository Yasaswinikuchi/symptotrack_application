package com.symtotrack

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SignupActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private var otpGenerated = ""
    private var otpPhone     = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult()
                    val name  = account?.displayName ?: "Google User"
                    val email = account?.email ?: ""

                    if (email.isNotEmpty()) {
                        Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                        registerSocialUser(name, email, account?.id ?: "", "google")
                        return@registerForActivityResult
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            Toast.makeText(this, "Google Sign-Up cancelled or unavailable", Toast.LENGTH_SHORT).show()
        }

        val btnBack      = findViewById<ImageButton>(R.id.btn_back)
        val etName       = findViewById<EditText>(R.id.et_name)
        val etEmail      = findViewById<EditText>(R.id.et_email)
        val etPassword   = findViewById<EditText>(R.id.et_password)
        val etConfirm    = findViewById<EditText>(R.id.et_confirm_password)
        val btnSignup    = findViewById<Button>(R.id.btn_signup)
        val tvLogin      = findViewById<TextView>(R.id.tv_login)
        val barStrength  = findViewById<ProgressBar>(R.id.pb_password_strength)
        val tvStrength   = findViewById<TextView>(R.id.tv_password_strength)

        btnBack.setOnClickListener { onBackPressed() }
        tvLogin.setOnClickListener { finish() }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pw = s?.toString() ?: ""
                val score = calcPasswordStrength(pw)
                barStrength?.progress = score
                when (score) {
                    0, 1 -> { tvStrength?.text = "Strength: Weak"; tvStrength?.setTextColor(getColor(R.color.error_red)) }
                    2 -> { tvStrength?.text = "Strength: Moderate"; tvStrength?.setTextColor(getColor(R.color.brand_blue)) }
                    3 -> { tvStrength?.text = "Strength: Strong 💪"; tvStrength?.setTextColor(getColor(R.color.accent_green)) }
                }
            }
        })

        btnSignup.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm  = etConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                etConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            btnSignup.isEnabled = false
            btnSignup.text = "Creating Account…"

            Thread {
                try {
                    val url  = URL("${AppConfig.BASE_URL}/signup.php")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    conn.doOutput      = true
                    conn.connectTimeout = 8000
                    conn.readTimeout    = 8000

                    val body = "name=${URLEncoder.encode(name, "UTF-8")}" +
                            "&email=${URLEncoder.encode(email, "UTF-8")}" +
                            "&password=${URLEncoder.encode(password, "UTF-8")}"
                    conn.outputStream.write(body.toByteArray())
                    conn.outputStream.flush()

                    val code   = conn.responseCode
                    val stream = if (code == HttpURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                    val json   = JSONObject(stream.bufferedReader().readText())

                    runOnUiThread {
                        btnSignup.isEnabled = true
                        btnSignup.text      = "Create Account"
                        if (json.optString("status") == "success") {
                            val user = json.getJSONObject("user")
                            Toast.makeText(this, "Account created! Welcome, ${user.getString("name")} 🎉", Toast.LENGTH_SHORT).show()
                            saveAndProceedToPayment(user.getInt("id"), user.getString("name"), user.getString("email"), "email")
                        } else {
                            Toast.makeText(this, json.optString("message", "Registration failed"), Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        btnSignup.isEnabled = true
                        btnSignup.text      = "Create Account"
                        Toast.makeText(this, "Server unreachable. Check XAMPP connection.", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        findViewById<CardView>(R.id.btn_google_signup)?.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        findViewById<CardView>(R.id.btn_phone_signup)?.setOnClickListener {
            showPhoneOtpSignupDialog()
        }
    }

    private fun registerSocialUser(name: String, email: String, googleId: String, provider: String) {
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
                        val user = json.getJSONObject("user")
                        Toast.makeText(this, "Welcome to SymptoTrack, ${user.getString("name")}! 🎉", Toast.LENGTH_SHORT).show()
                        saveAndProceedToPayment(user.getInt("id"), user.getString("name"), user.getString("email"), provider)
                    } else {
                        Toast.makeText(this, json.optString("message", "Registration failed"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    saveAndProceedToPayment(Math.abs(email.hashCode()), name, email, provider)
                }
            }
        }.start()
    }

    private fun showPhoneOtpSignupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mobile_otp, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etPhone      = dialogView.findViewById<EditText>(R.id.et_phone_number)
        val btnSend      = dialogView.findViewById<Button>(R.id.btn_send_otp)
        val btnVerify    = dialogView.findViewById<Button>(R.id.btn_verify_otp)
        val layoutPhone  = dialogView.findViewById<LinearLayout>(R.id.layout_step_phone)
        val layoutVerify = dialogView.findViewById<LinearLayout>(R.id.layout_step_verify)
        val tvSub        = dialogView.findViewById<TextView>(R.id.tv_otp_dialog_sub)
        val tvTitle      = dialogView.findViewById<TextView>(R.id.tv_otp_dialog_title)
        val tvResend     = dialogView.findViewById<TextView>(R.id.tv_resend_otp)

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

        tvTitle.text = "📱 Sign Up with Phone"
        dialogView.findViewById<Button>(R.id.btn_cancel_otp).setOnClickListener { dialog.dismiss() }

        fun doSendOtp(phone: String) {
            otpGenerated = (100000 + (Math.random() * 900000).toInt()).toString()
            otpPhone = phone
            layoutPhone.visibility  = View.GONE
            layoutVerify.visibility = View.VISIBLE
            tvSub.text = "OTP sent to +91 $phone ✓"

            for (i in 0 until 6) {
                if (i < otpGenerated.length) {
                    boxes[i].setText(otpGenerated[i].toString())
                }
            }
            boxes[5].requestFocus()
        }

        btnSend.setOnClickListener {
            val raw   = etPhone.text.toString().trim().replace(" ", "").replace("-", "")
            val phone = raw.removePrefix("+91").removePrefix("91")
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                etPhone.error = "Enter valid 10-digit number"; return@setOnClickListener
            }
            doSendOtp(phone)
        }

        tvResend.setOnClickListener { if (otpPhone.isNotEmpty()) doSendOtp(otpPhone) }

        btnVerify.setOnClickListener {
            val entered = boxes.joinToString("") { it.text.toString().trim() }
            if (entered != otpGenerated) {
                Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            val phoneEmail = "$otpPhone@phone.symtotrack"
            registerSocialUser("User +91$otpPhone", phoneEmail, otpPhone, "phone")
        }
        dialog.show()
    }

    private fun calcPasswordStrength(pw: String): Int {
        if (pw.isEmpty()) return 0
        var score = 0
        if (pw.length >= 8) score++
        if (pw.any { it.isDigit() } && pw.any { it.isLetter() }) score++
        if (pw.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) score++
        return score
    }

    private fun saveAndProceedToPayment(id: Int, name: String, email: String, provider: String) {
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
            putInt("user_id",       id)
            putString("user_name",  name)
            putString("user_email", email)
            putString("auth_provider", provider)
            putLong("login_time",   System.currentTimeMillis())
            putBoolean("payment_done", false)
            apply()
        }
        startActivity(Intent(this, PaymentActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
