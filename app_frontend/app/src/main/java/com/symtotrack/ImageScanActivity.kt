package com.symtotrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageScanActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var llPlaceholder: LinearLayout
    private lateinit var btnAnalyze: MaterialButton
    
    private var base64Image: String? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                displayAndEncodeImage(bitmap)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                displayAndEncodeImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_scan)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        ivPreview = findViewById(R.id.iv_preview)
        llPlaceholder = findViewById(R.id.ll_placeholder)
        btnAnalyze = findViewById(R.id.btn_analyze)

        setupBottomNavigation()

        findViewById<MaterialButton>(R.id.btn_take_photo).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            }
        }

        findViewById<MaterialButton>(R.id.btn_upload_file).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAnalyze.setOnClickListener {
            if (base64Image == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            analyzeImage(base64Image!!)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }
    }

    private fun displayAndEncodeImage(bitmap: Bitmap) {
        ivPreview.setImageBitmap(bitmap)
        ivPreview.visibility = View.VISIBLE
        llPlaceholder.visibility = View.GONE
        
        btnAnalyze.isEnabled = true
        
        // Resize bitmap to avoid sending huge payload
        val resized = resizeBitmap(bitmap, 800)
        
        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageBytes = baos.toByteArray()
        base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
    
    private fun resizeBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun analyzeImage(base64: String) {
        btnAnalyze.isEnabled = false
        btnAnalyze.text = "Analyzing Image..."
        
        Thread {
            try {
                val url = java.net.URL("${AppConfig.BASE_URL}/analyze_image.php")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                val jsonParam = JSONObject()
                jsonParam.put("image", base64)
                
                connection.outputStream.use { os ->
                    val input = jsonParam.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
                
                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val resultObj = JSONObject(response)
                    
                    runOnUiThread {
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "Analyze Image"
                        
                        val isValid = resultObj.optBoolean("valid", false)
                        if (isValid) {
                            val intent = Intent(this@ImageScanActivity, AnalysisResultActivity::class.java)
                            intent.putExtra("analysis_result", response)
                            startActivity(intent)
                        } else {
                            val msg = resultObj.optString("message", "Invalid image uploaded.")
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Validation Failed")
                                .setMessage(msg)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    runOnUiThread {
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "Analyze Image"
                        androidx.appcompat.app.AlertDialog.Builder(this@ImageScanActivity)
                            .setTitle("API Error")
                            .setMessage("Code: ${connection.responseCode}\n\n$errorStream")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    btnAnalyze.isEnabled = true
                    btnAnalyze.text = "Analyze Image"
                    Toast.makeText(this@ImageScanActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun setupBottomNavigation() {
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
