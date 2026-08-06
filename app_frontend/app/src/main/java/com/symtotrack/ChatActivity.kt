package com.symtotrack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutUser: LinearLayout = itemView.findViewById(R.id.layout_user)
        val layoutAi: LinearLayout = itemView.findViewById(R.id.layout_ai)
        val tvUser: TextView = itemView.findViewById(R.id.tv_user_msg)
        val tvAi: TextView = itemView.findViewById(R.id.tv_ai_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return VH(v)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = messages[position]
        if (msg.isUser) {
            holder.layoutUser.visibility = View.VISIBLE
            holder.layoutAi.visibility = View.GONE
            holder.tvUser.text = msg.text
        } else {
            holder.layoutUser.visibility = View.GONE
            holder.layoutAi.visibility = View.VISIBLE
            holder.tvAi.text = msg.text
        }
    }
}

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var typingIndicator: LinearLayout
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvMessages = findViewById(R.id.rv_messages)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
        typingIndicator = findViewById(R.id.typing_indicator)

        adapter = ChatAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        rvMessages.adapter = adapter

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        // Welcome message
        addAiMessage("👋 Hello! I'm your SymptoTrack AI Health Assistant. I can help you understand your symptoms, suggest remedies, and guide you. How can I help you today?")

        btnSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        etMessage.setText("")
        addUserMessage(text)
        showTyping(true)

        Thread {
            try {
                val url = URL("${AppConfig.BASE_URL}/chat.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val body = JSONObject().apply { put("message", text) }.toString()
                conn.outputStream.write(body.toByteArray())
                conn.outputStream.flush()

                val responseCode = conn.responseCode
                val stream = if (responseCode == HttpURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                val response = stream.bufferedReader().readText()
                val json = JSONObject(response)
                val reply = json.optString("reply", "I'm sorry, I couldn't understand that. Please try again.")

                handler.post {
                    showTyping(false)
                    animateAiMessage(reply)
                }
            } catch (e: Exception) {
                handler.post {
                    showTyping(false)
                    addAiMessage("I'm having trouble connecting right now. Please check that XAMPP is running, then try again. If symptoms are severe, please call 112.")
                }
            }
        }.start()
    }

    private fun animateAiMessage(fullText: String) {
        // Add empty AI message placeholder
        messages.add(ChatMessage("", false))
        val pos = messages.size - 1
        adapter.notifyItemInserted(pos)
        rvMessages.scrollToPosition(pos)

        var charIndex = 0
        val typingDelay = 18L // ms per character for streaming effect

        val runnable = object : Runnable {
            override fun run() {
                if (charIndex <= fullText.length) {
                    messages[pos] = ChatMessage(fullText.substring(0, charIndex), false)
                    adapter.notifyItemChanged(pos)
                    rvMessages.scrollToPosition(pos)
                    charIndex++
                    handler.postDelayed(this, typingDelay)
                }
            }
        }
        handler.post(runnable)
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun addAiMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun showTyping(show: Boolean) {
        typingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }
}
