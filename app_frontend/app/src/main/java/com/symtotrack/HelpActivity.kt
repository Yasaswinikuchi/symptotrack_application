package com.symtotrack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_contact_help).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@symtotrack.com")
                putExtra(Intent.EXTRA_SUBJECT, "SymptoTrack Support Request")
            }
            startActivity(Intent.createChooser(intent, "Send Email via..."))
        }
    }
}
