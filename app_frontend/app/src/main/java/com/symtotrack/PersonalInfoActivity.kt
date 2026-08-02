package com.symtotrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class PersonalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnChangePassword = findViewById<Button>(R.id.btn_change_password)

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        etName.setText(sharedPref.getString("user_name", ""))
        etEmail.setText(sharedPref.getString("user_email", ""))

        btnChangePassword.setOnClickListener {
            startActivity(android.content.Intent(this, ChangePasswordActivity::class.java))
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            Thread {
                try {
                    val url = java.net.URL("${AppConfig.BASE_URL}/profile.php")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    
                    val jsonParam = JSONObject()
                    jsonParam.put("name", name)
                    jsonParam.put("email", email)
                    jsonParam.put("password", "") // sending empty so backend doesn't change it
                    
                    connection.outputStream.use { os ->
                        val input = jsonParam.toString().toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                    }
                    
                    if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        runOnUiThread {
                            sharedPref.edit().apply {
                                putString("user_name", name)
                                putString("user_email", email)
                                apply()
                            }
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            btnSave.isEnabled = true
                            btnSave.text = "Save Changes"
                            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}
