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

        // 1. Nhận tên người gửi
        val senderName = intent.getStringExtra("SENDER_NAME") ?: return

        // 2. Khởi tạo các View trên Header
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvHeaderName = findViewById<TextView>(R.id.tvHeaderName)
        val imgHeaderAvatar = findViewById<ImageView>(R.id.imgHeaderAvatar)

        // Gán tên và xử lý nút Back
        tvHeaderName.text = senderName
        btnBack.setOnClickListener {
            finish() // Đóng màn hình hiện tại để quay về màn hình trước
        }

        // 3. Cài đặt RecyclerView (Danh sách chat)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerDetail)
        // stackFromEnd = true: Để tin nhắn luôn bắt đầu từ dưới lên (giống Messenger)
        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.layoutManager = layoutManager

        val adapter = DetailAdapter()
        recyclerView.adapter = adapter

        // 4. Load dữ liệu từ Database
        val db = AppDatabase.Companion.getDatabase(this)
        lifecycleScope.launch {
            db.messageDao().getMessagesBySender(senderName).collect { listMessages ->
                // Cập nhật danh sách tin nhắn
                adapter.submitList(listMessages)

                // --- XỬ LÝ HIỆN AVATAR LÊN HEADER ---
                // Lấy tin nhắn đầu tiên (hoặc bất kỳ tin nào) để lấy avatar
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

                    // Tự động cuộn xuống tin nhắn mới nhất
                    // (Sử dụng post để đảm bảo giao diện đã vẽ xong mới cuộn)
                    recyclerView.post {
                        recyclerView.scrollToPosition(listMessages.size - 1)
                    }
                }
            }
        }
    }
}