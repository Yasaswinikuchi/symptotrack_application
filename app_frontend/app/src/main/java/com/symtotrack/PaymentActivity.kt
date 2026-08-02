package com.symtotrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private var selectedPlan    = "free"
    private var selectedAmount  = 0   // in paise (₹ × 100)
    private var selectedPlanName = "Free"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Pre-warm Razorpay
        Checkout.preload(applicationContext)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName  = prefs.getString("user_name",  "User") ?: "User"
        val userEmail = prefs.getString("user_email", "") ?: ""

        // ── Plan Selection ────────────────────────────────────────────────────
        findViewById<Button>(R.id.btn_select_free).setOnClickListener {
            // Free plan — skip payment, go straight to Dashboard
            savePlanAndNavigate("free", "Free", 0)
        }

        findViewById<Button>(R.id.btn_select_basic).setOnClickListener {
            selectedPlan     = "basic"
            selectedAmount   = 9900   // ₹99 in paise
            selectedPlanName = "Basic"
            highlightCard(R.id.card_basic)
            startRazorpayPayment(userName, userEmail, 9900, "SymptoTrack Basic Plan")
        }

        findViewById<Button>(R.id.btn_select_premium).setOnClickListener {
            selectedPlan     = "premium"
            selectedAmount   = 19900  // ₹199 in paise
            selectedPlanName = "Premium"
            highlightCard(R.id.card_premium)
            startRazorpayPayment(userName, userEmail, 19900, "SymptoTrack Premium Plan")
        }

        // Card click = same as button
        findViewById<CardView>(R.id.card_basic).setOnClickListener {
            findViewById<Button>(R.id.btn_select_basic).performClick()
        }
        findViewById<CardView>(R.id.card_premium).setOnClickListener {
            findViewById<Button>(R.id.btn_select_premium).performClick()
        }
        findViewById<CardView>(R.id.card_free).setOnClickListener {
            savePlanAndNavigate("free", "Free", 0)
        }

        // Skip
        findViewById<TextView>(R.id.tv_skip_payment).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Skip Payment?")
                .setMessage("You'll start on the Free plan. You can upgrade anytime from your profile.")
                .setPositiveButton("Yes, Skip") { _, _ ->
                    savePlanAndNavigate("free", "Free", 0)
                }
                .setNegativeButton("View Plans", null)
                .show()
        }
    }

    // ── Razorpay Payment ──────────────────────────────────────────────────────
    private fun startRazorpayPayment(name: String, email: String, amountPaise: Int, description: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_SqOZwDnHPrqJm0")

        try {
            val options = JSONObject().apply {
                put("name",        "SymptoTrack Health")
                put("description", description)
                put("image",       "https://i.imgur.com/placeholder.png") // app logo
                put("currency",    "INR")
                put("amount",      amountPaise)
                put("prefill", JSONObject().apply {
                    put("name",    name)
                    put("email",   email)
                    put("contact", "")
                })
                put("theme", JSONObject().apply {
                    put("color", "#2E6CEB")
                })
                put("modal", JSONObject().apply {
                    put("ondismiss", "")
                    put("animation", true)
                })
            }
            checkout.open(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Payment gateway error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ── Razorpay Callbacks ────────────────────────────────────────────────────
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment successful! Welcome to $selectedPlanName ✅", Toast.LENGTH_SHORT).show()
        savePlanAndNavigate(selectedPlan, selectedPlanName, selectedAmount)
    }

    override fun onPaymentError(code: Int, response: String?) {
        // Show friendly error with retry option
        val msg = when (code) {
            Checkout.NETWORK_ERROR  -> "Network error. Please check your connection."
            Checkout.INVALID_OPTIONS -> "Payment configuration error."
            else                    -> "Payment cancelled or failed. You can try again or use the Free plan."
        }
        AlertDialog.Builder(this)
            .setTitle("Payment Unsuccessful")
            .setMessage(msg)
            .setPositiveButton("Try Again") { _, _ ->
                startRazorpayPayment(
                    getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_name", "User") ?: "User",
                    getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_email", "") ?: "",
                    selectedAmount, "SymptoTrack $selectedPlanName Plan"
                )
            }
            .setNegativeButton("Use Free Plan") { _, _ ->
                savePlanAndNavigate("free", "Free", 0)
            }
            .show()
    }

    // ── Save Plan & Navigate to Dashboard ────────────────────────────────────
    private fun savePlanAndNavigate(plan: String, planName: String, amountPaise: Int) {
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
            putString("subscription_plan",   plan)
            putString("subscription_name",   planName)
            putInt("subscription_amount",    amountPaise)
            putLong("subscription_date",     System.currentTimeMillis())
            putBoolean("payment_done",       plan != "free")
            apply()
        }
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // ── Visual feedback: highlight selected card ───────────────────────────────
    private fun highlightCard(cardId: Int) {
        val ids = listOf(R.id.card_free, R.id.card_basic, R.id.card_premium)
        ids.forEach { id ->
            val card = findViewById<CardView>(id)
            card?.cardElevation = if (id == cardId) 12f else 2f
        }
    }
}
