package com.example.myapplication.ui.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.detail.DetailAdapter
import com.example.myapplication.R
import com.example.myapplication.data.local.AppDatabase
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 1. Get sender's name from Intent
        val senderName = intent.getStringExtra("SENDER_NAME") ?: return

        // 2. Initialize Header views
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvHeaderName = findViewById<TextView>(R.id.tvHeaderName)
        val imgHeaderAvatar = findViewById<ImageView>(R.id.imgHeaderAvatar)

        // Set name and handle Back button click
        tvHeaderName.text = senderName
        btnBack.setOnClickListener {
            finish() // Close current activity to return to the previous screen
        }

        // 3. Setup RecyclerView (Chat list)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerDetail)
        // stackFromEnd = true: Ensures messages start from the bottom (similar to Messenger/WhatsApp)
        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.layoutManager = layoutManager

        val adapter = DetailAdapter()
        recyclerView.adapter = adapter

        // 4. Load data from Database
        val db = AppDatabase.Companion.getDatabase(this)
        lifecycleScope.launch {
            db.messageDao().getMessagesBySender(senderName).collect { listMessages ->
                // Update message list in adapter
                adapter.submitList(listMessages)

                // --- HANDLE HEADER AVATAR DISPLAY ---
                // Retrieve avatar from the first available message
                if (listMessages.isNotEmpty()) {
                    val firstMessage = listMessages[0]
                    if (firstMessage.avatar != null) {
                        try {
                            val bitmap = BitmapFactory.decodeByteArray(firstMessage.avatar, 0, firstMessage.avatar.size)
                            imgHeaderAvatar.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Automatically scroll to the latest message
                    // (Use post to ensure the UI has finished rendering before scrolling)
                    recyclerView.post {
                        recyclerView.scrollToPosition(listMessages.size - 1)
                    }
                }
            }
        }
    }
}
