package com.symtotrack

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class RemindersActivity : BaseActivity() {

    private lateinit var containerList: LinearLayout
    private lateinit var pbProgress: ProgressBar
    private lateinit var tvProgressSub: TextView
    private val medicationsArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        containerList = findViewById(R.id.layout_medication_list)
        pbProgress = findViewById(R.id.pb_pill_progress)
        tvProgressSub = findViewById(R.id.tv_pill_progress_sub)

        loadSavedMedications()

        findViewById<Button>(R.id.btn_add_reminder).setOnClickListener {
            showAddMedicationDialog()
        }
    }

    private fun loadSavedMedications() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("medications_list", null)

        medicationsArray.apply {
            while (length() > 0) remove(0)
        }

        if (raw != null && raw.isNotEmpty()) {
            try {
                val jsonArr = JSONArray(raw)
                for (i in 0 until jsonArr.length()) {
                    medicationsArray.put(jsonArr.getJSONObject(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Default sample items
            medicationsArray.put(JSONObject().apply {
                put("name", "Amoxicillin")
                put("dosage", "500mg")
                put("freq", "Twice Daily")
                put("time", "8:00 AM")
                put("notes", "After Food")
                put("taken", true)
            })
            medicationsArray.put(JSONObject().apply {
                put("name", "Paracetamol")
                put("dosage", "650mg")
                put("freq", "As needed")
                put("time", "2:00 PM")
                put("notes", "For fever/pain")
                put("taken", true)
            })
            medicationsArray.put(JSONObject().apply {
                put("name", "Vitamin D3 Supplement")
                put("dosage", "60,000 IU")
                put("freq", "Weekly")
                put("time", "9:00 PM")
                put("notes", "With warm water")
                put("taken", false)
            })
            saveMedications()
        }

        renderMedications()
    }

    private fun saveMedications() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("medications_list", medicationsArray.toString()).apply()
    }

    private fun renderMedications() {
        containerList.removeAllViews()
        var takenCount = 0
        val totalCount = medicationsArray.length()

        for (i in 0 until totalCount) {
            val item = medicationsArray.getJSONObject(i)
            val name = item.optString("name", "Medication")
            val dosage = item.optString("dosage", "")
            val time = item.optString("time", "Daily")
            val notes = item.optString("notes", "")
            val taken = item.optBoolean("taken", false)

            if (taken) takenCount++

            val view = LayoutInflater.from(this).inflate(R.layout.item_medication_card, containerList, false)
            
            val tvTitle = view.findViewById<TextView>(R.id.tv_med_title)
            val tvSub = view.findViewById<TextView>(R.id.tv_med_sub)
            val btnTake = view.findViewById<Button>(R.id.btn_take_action)
            val tvTaken = view.findViewById<TextView>(R.id.tv_taken_status)

            tvTitle.text = "$name $dosage".trim()
            tvSub.text = if (notes.isNotEmpty()) "$time • $notes" else time

            if (taken) {
                btnTake.visibility = View.GONE
                tvTaken.visibility = View.VISIBLE
            } else {
                btnTake.visibility = View.VISIBLE
                tvTaken.visibility = View.GONE

                btnTake.setOnClickListener {
                    item.put("taken", true)
                    saveMedications()
                    renderMedications()
                    Toast.makeText(this, "$name marked as taken!", Toast.LENGTH_SHORT).show()
                }
            }

            containerList.addView(view)
        }

        tvProgressSub.text = "$takenCount of $totalCount doses taken today"
        pbProgress.progress = if (totalCount > 0) ((takenCount.toFloat() / totalCount) * 100).toInt() else 100
    }

    private fun showAddMedicationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etName = dialogView.findViewById<EditText>(R.id.et_med_name)
        val etDosage = dialogView.findViewById<EditText>(R.id.et_med_dosage)
        val etTime = dialogView.findViewById<EditText>(R.id.et_med_time)
        val etStart = dialogView.findViewById<EditText>(R.id.et_med_start)
        val etEnd = dialogView.findViewById<EditText>(R.id.et_med_end)
        val etNotes = dialogView.findViewById<EditText>(R.id.et_med_notes)
        val spinnerFreq = dialogView.findViewById<Spinner>(R.id.spinner_med_freq)

        val freqOptions = arrayOf("Once Daily", "Twice Daily", "Three Times Daily", "As Needed", "Weekly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqOptions)
        spinnerFreq.adapter = adapter

        dialogView.findViewById<Button>(R.id.btn_cancel_med).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_save_med).setOnClickListener {
            val name = etName.text.toString().trim()
            val dosage = etDosage.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Medication name required"
                return@setOnClickListener
            }
            if (dosage.isEmpty()) {
                etDosage.error = "Dosage required"
                return@setOnClickListener
            }

            val newObj = JSONObject().apply {
                put("name", name)
                put("dosage", dosage)
                put("freq", spinnerFreq.selectedItem.toString())
                put("time", etTime.text.toString().ifEmpty { "08:00 AM" })
                put("start", etStart.text.toString())
                put("end", etEnd.text.toString())
                put("notes", etNotes.text.toString())
                put("taken", false)
            }

            medicationsArray.put(newObj)
            saveMedications()
            renderMedications()
            dialog.dismiss()
            Toast.makeText(this, "$name added to reminders!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}
