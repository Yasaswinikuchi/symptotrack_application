package com.symtotrack

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_history

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_symptoms -> {
                    startActivity(Intent(this, SymptomsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_history -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        loadHistory()
        
        val tvClearAll = findViewById<TextView>(R.id.tv_clear_all)
        tvClearAll.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all history records? This cannot be undone.")
                .setPositiveButton("Clear All") { _, _ -> clearAllHistory() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun clearAllHistory() {
        Thread {
            try {
                val url = java.net.URL("${AppConfig.BASE_URL}/clear_history.php")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                
                val response = conn.inputStream.bufferedReader().readText()
                val data = JSONObject(response)
                
                runOnUiThread {
                    if (data.optBoolean("success")) {
                        Toast.makeText(this, "All records deleted.", Toast.LENGTH_SHORT).show()
                        loadHistory()
                    } else {
                        Toast.makeText(this, "Failed to clear history.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun loadHistory() {
        val llHistoryList = findViewById<LinearLayout>(R.id.ll_history_list)
        
        Thread {
            try {
                val url = java.net.URL("${AppConfig.BASE_URL}/history.php")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                
                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    
                    runOnUiThread {
                        llHistoryList.removeAllViews()
                        if (jsonArray.length() == 0) {
                            val tv = TextView(this)
                            tv.text = "No history records found."
                            tv.gravity = Gravity.CENTER
                            tv.setPadding(0, 60, 0, 0)
                            llHistoryList.addView(tv)
                            return@runOnUiThread
                        }
                        
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                        
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val type = obj.optString("type")
                            val symptoms = obj.optString("symptoms")
                            val risk = obj.optString("risk_level")
                            val id = obj.optInt("id")
                            val fullResponse = obj.optString("full_response")
                            val rawDate = obj.optString("created_at")
                            
                            var dateStr = rawDate
                            try {
                                val date = inputFormat.parse(rawDate)
                                if (date != null) dateStr = outputFormat.format(date)
                            } catch (e: Exception) {}
                            
                            val card = createHistoryCard(id, type, dateStr, symptoms, risk, fullResponse)
                            llHistoryList.addView(card)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val tvLoading = findViewById<TextView>(R.id.tv_loading)
                    if (tvLoading != null) tvLoading.text = "Failed to load history."
                }
            }
        }.start()
    }

    private fun createHistoryCard(id: Int, type: String, date: String, symptoms: String, risk: String, fullResponse: String): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setBackgroundColor(Color.WHITE)
        val p = (resources.displayMetrics.density * 16).toInt()
        card.setPadding(p, p, p, p)
        
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, (resources.displayMetrics.density * 16).toInt())
        card.layoutParams = lp
        
        // Icon
        val iconBg = LinearLayout(this)
        val iconSize = (resources.displayMetrics.density * 48).toInt()
        iconBg.layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply { rightMargin = p }
        iconBg.gravity = Gravity.CENTER
        iconBg.setBackgroundResource(R.drawable.rounded_edittext) // simple rounded background
        val iv = ImageView(this)
        iv.setImageResource(if (type == "Image Scan") R.drawable.ic_pulse else R.drawable.ic_symptoms)
        iv.setColorFilter(Color.parseColor("#3B82F6"))
        iconBg.addView(iv)
        
        // Content
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        
        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        val tvTitle = TextView(this)
        tvTitle.text = type
        tvTitle.setTextColor(Color.parseColor("#0F172A"))
        tvTitle.textSize = 15f
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        tvTitle.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        
        val tvRisk = TextView(this)
        tvRisk.text = risk
        tvRisk.textSize = 11f
        tvRisk.setPadding((resources.displayMetrics.density * 8).toInt(), (resources.displayMetrics.density * 4).toInt(), (resources.displayMetrics.density * 8).toInt(), (resources.displayMetrics.density * 4).toInt())
        tvRisk.setTypeface(null, android.graphics.Typeface.BOLD)
        when (risk) {
            "High" -> {
                tvRisk.setTextColor(Color.parseColor("#DC2626"))
                tvRisk.setBackgroundColor(Color.parseColor("#FEE2E2"))
            }
            "Medium" -> {
                tvRisk.setTextColor(Color.parseColor("#EA580C"))
                tvRisk.setBackgroundColor(Color.parseColor("#FFEDD5"))
            }
            else -> {
                tvRisk.setTextColor(Color.parseColor("#16A34A"))
                tvRisk.setBackgroundColor(Color.parseColor("#DCFCE7"))
            }
        }
        header.addView(tvTitle)
        header.addView(tvRisk)
        
        val tvDate = TextView(this)
        tvDate.text = date
        tvDate.setTextColor(Color.parseColor("#94A3B8"))
        tvDate.textSize = 12f
        tvDate.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = (resources.displayMetrics.density * 2).toInt() }
        
        val tvSymptoms = TextView(this)
        tvSymptoms.text = symptoms
        tvSymptoms.setTextColor(Color.parseColor("#64748B"))
        tvSymptoms.textSize = 13f
        tvSymptoms.maxLines = 1
        tvSymptoms.ellipsize = android.text.TextUtils.TruncateAt.END
        
        content.addView(header)
        content.addView(tvDate)
        content.addView(tvSymptoms)
        
        card.addView(iconBg)
        card.addView(content)

        card.setOnClickListener {
            if (fullResponse.isEmpty() || fullResponse == "null") {
                Toast.makeText(this, "Detailed report is not available for older records.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HistoryResultActivity::class.java)
                intent.putExtra("report_id", id)
                intent.putExtra("analysis_result", fullResponse)
                startActivity(intent)
            }
        }
        
        return card
    }
}
