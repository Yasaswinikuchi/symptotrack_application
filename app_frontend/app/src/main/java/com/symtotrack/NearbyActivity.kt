package com.symtotrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class NearbyActivity : BaseActivity(), PaymentResultWithDataListener {

    private lateinit var map: MapView
    private lateinit var rvHospitals: RecyclerView
    private lateinit var tvLoading: TextView
    private var currentHospital: String = ""
    private var currentProblem: String = ""
    private var currentTimeSlot: String = ""
    private var currentDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_nearby)

        map = findViewById(R.id.map_view)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        rvHospitals = findViewById(R.id.rv_hospitals)
        rvHospitals.layoutManager = LinearLayoutManager(this)
        
        tvLoading = findViewById(R.id.tv_loading)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_map

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

        // Initialize Razorpay
        Checkout.preload(applicationContext)

        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        } else {
            getLocationAndFetchHospitals()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val locationGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (locationGranted) {
                getLocationAndFetchHospitals()
            } else {
                tvLoading.text = "Location permission denied."
            }
        }
    }

    private fun getLocationAndFetchHospitals() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Try getting last known location first for immediate result
        var lastLoc: Location? = null
        try {
            lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (lastLoc != null) {
            updateMapAndFetch(lastLoc.latitude, lastLoc.longitude)
        } else {
            tvLoading.text = "Waiting for GPS location..."
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        runOnUiThread {
                            updateMapAndFetch(location.latitude, location.longitude)
                        }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, null)
                
                // Fallback to Network provider
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        runOnUiThread {
                            updateMapAndFetch(location.latitude, location.longitude)
                        }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, null)
            } catch (e: SecurityException) {
                e.printStackTrace()
                tvLoading.text = "Location access failed."
            }
        }
    }

    private fun updateMapAndFetch(lat: Double, lon: Double) {
        if (isFinishing || isDestroyed) return
        val startPoint = GeoPoint(lat, lon)
        map.controller.setZoom(14.0)
        map.controller.setCenter(startPoint)
        
        val marker = Marker(map)
        marker.position = startPoint
        marker.title = "You are here"
        map.overlays.add(marker)
        map.invalidate()

        tvLoading.text = "Fetching nearby hospitals..."
        fetchHospitals(lat, lon)
    }

    private fun fetchHospitals(lat: Double, lon: Double) {
        Thread {
            val hospitalList = mutableListOf<Hospital>()
            try {
                val url = URL("${AppConfig.BASE_URL}/hospitals.php?lat=$lat&lon=$lon")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val reader = InputStreamReader(conn.inputStream)
                val response = reader.readText()
                val data = JSONObject(response)
                val elements = data.optJSONArray("elements")

                val feeList = listOf("₹350", "₹500", "₹750", "₹1200", "Contact Hospital", "Fee Not Available")
                val specList = listOf(
                    "General Medicine, Cardiology",
                    "Emergency, Orthopedics, Pediatrics",
                    "Dermatology, ENT, General Surgery",
                    "Neurology, Pulmonology, ICUs",
                    "Maternity, Pediatrics, General"
                )

                if (elements != null) {
                    val limit = Math.min(elements.length(), 15)
                    for (i in 0 until limit) {
                        val el = elements.getJSONObject(i)
                        val hLat = el.optDouble("lat", lat)
                        val hLon = el.optDouble("lon", lon)
                        val tags = el.optJSONObject("tags")
                        val name = tags?.optString("name") ?: "Community Health Center #${i + 1}"
                        val street = tags?.optString("addr:street") ?: tags?.optString("address") ?: "Medical District Road"
                        val phone = tags?.optString("phone") ?: tags?.optString("contact:phone") ?: "+1 800-555-${1000 + i}"

                        val dist = distance(lat, lon, hLat, hLon)
                        val waitTime = (Math.random() * 45 + 5).toInt()
                        val rating = String.format("%.1f", Math.random() * 1.2 + 3.8)
                        val fee = feeList[i % feeList.size]
                        val spec = specList[i % specList.size]

                        hospitalList.add(Hospital(name, street, hLat, hLon, dist, rating, phone, spec, fee, waitTime))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback list if Overpass network/GPS is slow or empty
            if (hospitalList.isEmpty()) {
                val sampleHospitals = listOf(
                    Hospital("Metro City General Hospital", "102 Health Avenue, Downtown", lat + 0.008, lon + 0.005, 1.2, "4.8", "+1 800-555-0101", "Emergency, Cardiology, Pediatrics", "₹500", 12),
                    Hospital("Apollo Diagnostic & Multi-Specialty", "45 Park Ridge Road", lat + 0.015, lon - 0.008, 2.4, "4.6", "+1 800-555-0144", "Neurology, Orthopedics, General", "₹750", 25),
                    Hospital("St. Jude Community Healthcare", "88 Station Boulevard", lat - 0.012, lon + 0.014, 3.1, "4.4", "+1 800-555-0188", "Maternity, General Medicine", "₹350", 18),
                    Hospital("Global Heart & Vascular Center", "204 Ring Road Bypass", lat + 0.025, lon + 0.020, 4.5, "4.9", "+1 800-555-0199", "Cardiology, Vascular Surgery", "₹1200", 8),
                    Hospital("Sunshine Urgent Care & Clinic", "12 Market Square", lat - 0.020, lon - 0.018, 5.2, "4.2", "+1 800-555-0210", "Dermatology, ENT, Fever Triage", "Contact Hospital", 15),
                    Hospital("City Care Children & General Hospital", "67 Green Valley Avenue", lat + 0.035, lon - 0.025, 6.8, "4.5", "+1 800-555-0255", "Pediatrics, Neonatal Care", "Fee Not Available", 30)
                )
                hospitalList.addAll(sampleHospitals)
            }

            // Sort hospitals by distance ascending (closest first)
            hospitalList.sortBy { it.distance }

            runOnUiThread {
                tvLoading.visibility = TextView.GONE

                hospitalList.forEach { h ->
                    val m = Marker(map)
                    m.position = GeoPoint(h.lat, h.lon)
                    m.title = h.name
                    map.overlays.add(m)
                }
                map.invalidate()

                rvHospitals.adapter = HospitalAdapter(hospitalList) { hospital ->
                    showBookingDialog(hospital.name)
                }
            }
        }.start()
    }

    private fun showBookingDialog(hospitalName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_book_appointment, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvName = dialogView.findViewById<TextView>(R.id.tv_dialog_hospital_name)
        val etProblem = dialogView.findViewById<android.widget.EditText>(R.id.et_problem_desc)
        val btnPickDate = dialogView.findViewById<android.widget.Button>(R.id.btn_pick_date)
        val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_time_slot)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btn_cancel)
        val btnProceed = dialogView.findViewById<android.widget.Button>(R.id.btn_proceed)

        tvName.text = "Book: $hospitalName"

        var selectedDate = ""
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$year-${month + 1}-$day"
                btnPickDate.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val times = arrayOf("Select a time", "10:00 AM", "11:30 AM", "02:00 PM", "04:30 PM")
        val adapter = object : android.widget.ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, times) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val v = super.getView(position, convertView, parent) as TextView
                v.setTextColor(androidx.core.content.ContextCompat.getColor(this@NearbyActivity, R.color.brand_dark))
                v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                return v
            }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val v = super.getDropDownView(position, convertView, parent) as TextView
                v.setTextColor(androidx.core.content.ContextCompat.getColor(this@NearbyActivity, R.color.brand_dark))
                v.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@NearbyActivity, R.color.card_white))
                v.setPadding(30, 24, 30, 24)
                return v
            }
        }
        spinner.adapter = adapter

        btnCancel.setOnClickListener { dialog.dismiss() }
        
        btnProceed.setOnClickListener {
            val problem = etProblem.text.toString().trim()
            val timeSlot = spinner.selectedItem.toString()

            if (problem.isEmpty() || spinner.selectedItemPosition == 0 || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select date, time and enter problem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentHospital = hospitalName
            currentProblem = problem
            currentDate = selectedDate
            currentTimeSlot = timeSlot
            dialog.dismiss()
            
            startRazorpayCheckout()
        }

        dialog.show()
    }

    private fun startRazorpayCheckout() {
        Thread {
            try {
                val url = URL("${AppConfig.BASE_URL}/create_order.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                
                val reqJson = JSONObject().apply { put("amount", 50000) }
                conn.outputStream.write(reqJson.toString().toByteArray())
                
                val response = InputStreamReader(conn.inputStream).readText()
                val orderData = JSONObject(response)
                val orderId = orderData.optString("id")
                
                if (orderId.isEmpty()) {
                    runOnUiThread { Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show() }
                    return@Thread
                }
                
                runOnUiThread {
                    val checkout = Checkout()
                    checkout.setKeyID("rzp_test_SqOZwDnHPrqJm0")
                    try {
                        val options = JSONObject()
                        options.put("name", "SymptoTrack")
                        options.put("description", "Appointment at $currentHospital")
                        options.put("currency", "INR")
                        options.put("amount", "50000")
                        options.put("order_id", orderId)
                        options.put("prefill.name", "Alex Johnson")
                        options.put("prefill.email", "alex.johnson@example.com")
                        options.put("prefill.contact", "9999999999")
                        
                        checkout.open(this, options)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        Thread {
            try {
                val url = URL("${AppConfig.BASE_URL}/verify_payment.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                
                val verifyJson = JSONObject().apply {
                    put("razorpay_order_id", paymentData?.orderId)
                    put("razorpay_payment_id", paymentData?.paymentId)
                    put("razorpay_signature", paymentData?.signature)
                }
                conn.outputStream.write(verifyJson.toString().toByteArray())
                
                val response = InputStreamReader(conn.inputStream).readText()
                val vData = JSONObject(response)
                
                if (vData.optBoolean("success")) {
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val userId = prefs.getInt("user_id", 1)

                    val saveUrl = URL("${AppConfig.BASE_URL}/save_appointment.php")
                    val saveConn = saveUrl.openConnection() as HttpURLConnection
                    saveConn.requestMethod = "POST"
                    saveConn.setRequestProperty("Content-Type", "application/json")
                    saveConn.doOutput = true
                    val saveJson = JSONObject().apply {
                        put("user_id", userId)
                        put("hospital_name", currentHospital)
                        put("payment_id", paymentData?.paymentId)
                        put("problem_description", currentProblem)
                        put("time_slot", "$currentDate $currentTimeSlot")
                    }
                    saveConn.outputStream.write(saveJson.toString().toByteArray())
                    
                    runOnUiThread {
                        val tokenNum = (15..30).random()
                        val nowServingNum = tokenNum - (2..5).random()
                        getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
                            .putBoolean("has_active_appointment", true)
                            .putInt("your_token", tokenNum)
                            .putInt("now_serving", nowServingNum)
                            .putString("hospital_name", currentHospital)
                            .apply()

                        Toast.makeText(this, "Appointment Booked! Token #$tokenNum assigned.", Toast.LENGTH_LONG).show()
                        sendAppointmentNotification(currentHospital, "$currentDate $currentTimeSlot")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Payment Verification Failed!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(Math.toRadians(theta))
        dist = acos(dist)
        dist = Math.toDegrees(dist)
        dist *= 60 * 1.1515 // miles
        return dist * 1.609344 // km
    }

    private fun sendAppointmentNotification(hospital: String, dateTime: String) {
        val channelId = "appointment_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Appointments", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        val intent = Intent(this, AlertsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Appointment Confirmed")
            .setContentText("Your appointment at $hospital is confirmed for $dateTime.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).notify(1001, notification)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
