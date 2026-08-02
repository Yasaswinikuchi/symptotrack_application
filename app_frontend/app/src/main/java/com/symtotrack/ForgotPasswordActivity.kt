package com.symtotrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ForgotPasswordActivity : AppCompatActivity() {

    private var currentEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)
        val viewFlipper = findViewById<ViewFlipper>(R.id.view_flipper)

        // Step 1 Views
        val etEmail = findViewById<EditText>(R.id.et_email)
        val btnNext = findViewById<Button>(R.id.btn_next)

        // Step 2 Views
        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnSave = findViewById<Button>(R.id.btn_save)

        btnBack.setOnClickListener {
            if (viewFlipper.displayedChild == 1) {
                viewFlipper.displayedChild = 0
                tvSubtitle.text = "Enter your email to verify your account."
            } else {
                finish()
            }
        }

        btnNext.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // In a real app we'd verify the email exists or send an OTP here.
            // For now, we move directly to password reset.
            currentEmail = email
            tvSubtitle.text = "Enter your new password."
            viewFlipper.displayedChild = 1
        }

        btnSave.setOnClickListener {
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill both password fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            Thread {
                try {
                    val url = URL("${AppConfig.BASE_URL}/reset_password.php")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    
                    val jsonReq = JSONObject().apply { 
                        put("email", currentEmail)
                        put("new_password", newPass)
                    }
                    
                    conn.outputStream.write(jsonReq.toString().toByteArray())
                    
                    val responseCode = conn.responseCode
                    val stream = if (responseCode == HttpURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                    val response = InputStreamReader(stream).readText()
                    val data = JSONObject(response)
                    
                    runOnUiThread {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"
                        if (data.optString("status") == "success") {
                            Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, data.optString("message", "Reset failed"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}
