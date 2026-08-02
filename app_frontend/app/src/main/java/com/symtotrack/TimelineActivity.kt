package com.symtotrack

import android.os.Bundle
import android.widget.ImageView

class TimelineActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
