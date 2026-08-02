package com.symtotrack

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Delegate immediately to SplashActivity which is the real entry point
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}