package com.symtotrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AlertsActivity : AppCompatActivity() {

    private lateinit var rvAlerts: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var layoutEmpty: View
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        rvAlerts = findViewById(R.id.rv_alerts)
        rvAlerts.layoutManager = LinearLayoutManager(this)
        
        tvEmpty = findViewById(R.id.tv_empty)
        layoutEmpty = findViewById(R.id.layout_empty_appointments)
        progressBar = findViewById(R.id.progress_bar)

        findViewById<android.widget.Button>(R.id.btn_book_new_appointment)?.setOnClickListener {
            startActivity(Intent(this, NearbyActivity::class.java))
        }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        fetchAppointments()
    }

    private fun fetchAppointments() {
        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        rvAlerts.visibility = View.GONE

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 1)

        Thread {
            try {
                val url = URL("${AppConfig.BASE_URL}/get_appointments.php?user_id=$userId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                
                val response = InputStreamReader(conn.inputStream).readText()
                val data = JSONObject(response)
                
                val success = data.optBoolean("success", false)
                if (success) {
                    val appointmentsArray = data.optJSONArray("data")
                    val appointments = mutableListOf<Appointment>()
                    
                    if (appointmentsArray != null) {
                        for (i in 0 until appointmentsArray.length()) {
                            val obj = appointmentsArray.getJSONObject(i)
                            appointments.add(
                                Appointment(
                                    id = obj.optInt("id", 0),
                                    hospitalName = obj.optString("hospital_name", "Unknown"),
                                    timeSlot = obj.optString("time_slot", "TBD"),
                                    description = obj.optString("problem_description", ""),
                                    status = obj.optString("status", "Booked")
                                )
                            )
                        }
                    }
                    
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        if (appointments.isEmpty()) {
                            layoutEmpty.visibility = View.VISIBLE
                        } else {
                            rvAlerts.visibility = View.VISIBLE
                            rvAlerts.adapter = AppointmentAdapter(appointments)
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "No appointments found."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "No appointments found."
                }
            }
        }.start()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_home // keep home or set to none if it's a subscreen
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_symptoms -> {
                    startActivity(Intent(this, SymptomsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, NearbyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}

data class Appointment(
    val id: Int,
    val hospitalName: String,
    val timeSlot: String,
    val description: String,
    val status: String
)

class AppointmentAdapter(private val items: MutableList<Appointment>) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHospital: TextView = view.findViewById(R.id.tv_hospital_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvTime: TextView = view.findViewById(R.id.tv_time_slot)
        val tvDesc: TextView = view.findViewById(R.id.tv_desc)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_appointment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appt = items[position]
        holder.tvHospital.text = appt.hospitalName
        holder.tvStatus.text = "Status: ${appt.status}"
        holder.tvTime.text = appt.timeSlot
        holder.tvDesc.text = appt.description
        
        holder.btnDelete.setOnClickListener {
            val context = holder.itemView.context
            Thread {
                try {
                    val url = URL("${AppConfig.BASE_URL}/delete_appointment.php")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    
                    val jsonReq = JSONObject().apply { put("id", appt.id) }
                    conn.outputStream.write(jsonReq.toString().toByteArray())
                    
                    val response = InputStreamReader(conn.inputStream).readText()
                    val data = JSONObject(response)
                    
                    if (data.optBoolean("success")) {
                        (context as AppCompatActivity).runOnUiThread {
                            val curPos = holder.adapterPosition
                            if (curPos != RecyclerView.NO_POSITION) {
                                items.removeAt(curPos)
                                notifyItemRemoved(curPos)
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        (context as AppCompatActivity).runOnUiThread {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    (context as AppCompatActivity).runOnUiThread {
                        Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    override fun getItemCount() = items.size
}
