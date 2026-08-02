package com.symtotrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val etReenterPassword = findViewById<EditText>(R.id.et_reenter_password)
        val btnSavePassword = findViewById<Button>(R.id.btn_save_password)

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val currentName = sharedPref.getString("user_name", "") ?: ""
        val currentEmail = sharedPref.getString("user_email", "") ?: ""

        btnSavePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val reenterPassword = etReenterPassword.text.toString().trim()

            if (newPassword.isEmpty() || reenterPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != reenterPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSavePassword.isEnabled = false
            btnSavePassword.text = "Saving..."

            Thread {
                try {
                    val url = java.net.URL("${AppConfig.BASE_URL}/profile.php")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    
                    val jsonParam = JSONObject()
                    jsonParam.put("name", currentName)
                    jsonParam.put("email", currentEmail)
                    jsonParam.put("password", newPassword)
                    
                    connection.outputStream.use { os ->
                        val input = jsonParam.toString().toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                    }
                    
                    if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        runOnUiThread {
                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            btnSavePassword.isEnabled = true
                            btnSavePassword.text = "Save Password"
                            Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        btnSavePassword.isEnabled = true
                        btnSavePassword.text = "Save Password"
                        Toast.makeText(this, "Error connecting to server", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}
