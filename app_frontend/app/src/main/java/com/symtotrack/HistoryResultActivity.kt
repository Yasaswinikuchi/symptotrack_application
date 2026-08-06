package com.symtotrack

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject

import android.widget.Toast
class HistoryResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_result)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val reportId = intent.getIntExtra("report_id", -1)

        val btnDeleteReport = findViewById<MaterialButton>(R.id.btn_delete_report)
        btnDeleteReport.setOnClickListener {
            if (reportId == -1) return@setOnClickListener
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report?")
                .setPositiveButton("Delete") { _, _ -> deleteReport(reportId) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val jsonString = intent.getStringExtra("analysis_result") ?: return
        try {
            val data = JSONObject(jsonString)
            
            // Risk level
            val riskLevel = data.optString("riskLevel", "Unknown")
            val riskDesc = data.optString("riskDescription", "")
            
            val tvRiskValue = findViewById<TextView>(R.id.tv_risk_value)
            val tvRiskDesc = findViewById<TextView>(R.id.tv_risk_desc)
            val ivRiskIcon = findViewById<ImageView>(R.id.iv_risk_icon)
            
            tvRiskValue.text = riskLevel
            tvRiskDesc.text = riskDesc
            
            when (riskLevel) {
                "High" -> {
                    tvRiskValue.setTextColor(Color.parseColor("#DC2626"))
                    ivRiskIcon.setColorFilter(Color.parseColor("#DC2626"))
                    (ivRiskIcon.parent as LinearLayout).setBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                "Medium" -> {
                    tvRiskValue.setTextColor(Color.parseColor("#0F172A"))
                    ivRiskIcon.setColorFilter(Color.parseColor("#EA580C"))
                }
                else -> {
                    tvRiskValue.setTextColor(Color.parseColor("#16A34A"))
                    ivRiskIcon.setColorFilter(Color.parseColor("#16A34A"))
                    (ivRiskIcon.parent as LinearLayout).setBackgroundColor(Color.parseColor("#DCFCE7"))
                }
            }
            
            // Conditions
            val llConditions = findViewById<LinearLayout>(R.id.ll_conditions)
            val conditions = data.optJSONArray("conditions") ?: JSONArray()
            for (i in 0 until conditions.length()) {
                val cond = conditions.getJSONObject(i)
                val view = createConditionView(cond.optString("name"), cond.optString("matchPercentage"), cond.optString("description"), i)
                llConditions.addView(view)
            }
            
            // Recommendations
            val llRecommendations = findViewById<LinearLayout>(R.id.ll_recommendations)
            val recs = data.optJSONArray("recommendations") ?: JSONArray()
            for (i in 0 until recs.length()) {
                val rec = recs.getJSONObject(i)
                val view = createRecommendationView(rec.optString("title"), rec.optString("description"), rec.optString("icon"))
                llRecommendations.addView(view)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createConditionView(name: String, match: String, desc: String, index: Int): LinearLayout {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setBackgroundColor(Color.WHITE)
        val padding = resources.displayMetrics.density * 16
        container.setPadding(padding.toInt(), padding.toInt(), padding.toInt(), padding.toInt())
        
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, (resources.displayMetrics.density * 16).toInt())
        container.layoutParams = lp
        
        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        
        val tvName = TextView(this)
        tvName.text = name
        tvName.textSize = 16f
        tvName.setTextColor(Color.parseColor("#0F172A"))
        tvName.setTypeface(null, android.graphics.Typeface.BOLD)
        val nameLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvName.layoutParams = nameLp
        
        val tvMatch = TextView(this)
        tvMatch.text = match
        tvMatch.textSize = 12f
        if (index % 2 == 0) {
            tvMatch.setTextColor(Color.parseColor("#EA580C"))
            tvMatch.setBackgroundColor(Color.parseColor("#FFEDD5"))
        } else {
            tvMatch.setTextColor(Color.parseColor("#CA8A04"))
            tvMatch.setBackgroundColor(Color.parseColor("#FEF9C3"))
        }
        val p = (resources.displayMetrics.density * 6).toInt()
        tvMatch.setPadding(p, p, p, p)
        
        header.addView(tvName)
        header.addView(tvMatch)
        
        val tvDesc = TextView(this)
        tvDesc.text = desc
        tvDesc.textSize = 14f
        tvDesc.setTextColor(Color.parseColor("#64748B"))
        tvDesc.setPadding(0, (resources.displayMetrics.density * 8).toInt(), 0, 0)
        
        container.addView(header)
        container.addView(tvDesc)
        
        return container
    }

    private fun createRecommendationView(title: String, desc: String, icon: String): LinearLayout {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.HORIZONTAL
        val padding = resources.displayMetrics.density * 16
        container.setPadding(padding.toInt(), padding.toInt(), padding.toInt(), padding.toInt())
        
        val iv = ImageView(this)
        iv.setImageResource(R.drawable.ic_pulse)
        iv.setColorFilter(if (icon == "alert") Color.parseColor("#EA580C") else Color.parseColor("#16A34A"))
        val ivLp = LinearLayout.LayoutParams((resources.displayMetrics.density * 24).toInt(), (resources.displayMetrics.density * 24).toInt())
        ivLp.setMargins(0, 0, padding.toInt(), 0)
        iv.layoutParams = ivLp
        
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        
        val tvTitle = TextView(this)
        tvTitle.text = title
        tvTitle.textSize = 14f
        tvTitle.setTextColor(Color.parseColor("#0F172A"))
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        
        val tvDesc = TextView(this)
        tvDesc.text = desc
        tvDesc.textSize = 12f
        tvDesc.setTextColor(Color.parseColor("#64748B"))
        
        content.addView(tvTitle)
        content.addView(tvDesc)
        
        container.addView(iv)
        container.addView(content)
        
        return container
    }

    private fun deleteReport(id: Int) {
        Thread {
            try {
                val url = java.net.URL("${AppConfig.BASE_URL}/delete_history.php")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val json = JSONObject().apply { put("id", id) }
                conn.outputStream.write(json.toString().toByteArray())
                
                val response = conn.inputStream.bufferedReader().readText()
                val data = JSONObject(response)
                
                runOnUiThread {
                    if (data.optBoolean("success")) {
                        Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show()
                        finish() // Return to HistoryActivity
                    } else {
                        Toast.makeText(this, "Failed to delete report", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
