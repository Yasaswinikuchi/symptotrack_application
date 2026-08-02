package com.symtotrack

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_delete_account).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account and all associated medical data? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    Thread {
                        try {
                            val url = java.net.URL("${AppConfig.BASE_URL}/delete_account.php")
                            val connection = url.openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = "POST"
                            if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                                runOnUiThread {
                                    Toast.makeText(this@PrivacyActivity, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                    getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply()
                                    val intent = Intent(this@PrivacyActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {}
                    }.start()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
