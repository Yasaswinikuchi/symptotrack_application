package com.symtotrack

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONArray
import org.json.JSONObject

class SymptomsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val spinnerGender = findViewById<Spinner>(R.id.spinner_gender)
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        spinnerGender.adapter = genderAdapter

        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_conditions)
        val btnAddCondition = findViewById<ImageButton>(R.id.btn_add_condition)
        val btnAnalyze = findViewById<MaterialButton>(R.id.btn_analyze)
        val etSymptoms = findViewById<EditText>(R.id.et_symptoms)

        // Add initial chip removed

        btnAddCondition.setOnClickListener {
            val input = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("Add Condition")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val condition = input.text.toString()
                    if (condition.isNotBlank()) {
                        addChip(chipGroup, condition)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnAnalyze.setOnClickListener {
            val symptomsText = etSymptoms.text.toString()
            if (symptomsText.isBlank()) {
                Toast.makeText(this, "Please describe your symptoms", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            btnAnalyze.isEnabled = false
            btnAnalyze.text = "Analyzing..."
            
            val age = findViewById<EditText>(R.id.et_age).text.toString()
            val gender = spinnerGender.selectedItem.toString()
            
            val conditions = ArrayList<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                conditions.add(chip.text.toString())
            }
            
            Thread {
                try {
                    val url = java.net.URL("${AppConfig.BASE_URL}/analyze.php")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    
                    val jsonParam = JSONObject()
                    jsonParam.put("symptomsText", symptomsText)
                    jsonParam.put("age", age)
                    jsonParam.put("gender", gender)
                    jsonParam.put("conditions", JSONArray(conditions))
                    
                    val os = connection.outputStream
                    os.write(jsonParam.toString().toByteArray())
                    os.flush()
                    os.close()
                    
                    if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().readText()
                        runOnUiThread {
                            btnAnalyze.isEnabled = true
                            btnAnalyze.text = "Analyze Symptoms"
                            val intent = Intent(this@SymptomsActivity, AnalysisResultActivity::class.java)
                            intent.putExtra("analysis_result", response)
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            btnAnalyze.isEnabled = true
                            btnAnalyze.text = "Analyze Symptoms"
                            Toast.makeText(this@SymptomsActivity, "Failed to analyze", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "Analyze Symptoms"
                        Toast.makeText(this@SymptomsActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_symptoms
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_symptoms -> true
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
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
    }

    private fun addChip(group: ChipGroup, text: String) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            group.removeView(chip)
        }
        group.addView(chip)
    }
}
