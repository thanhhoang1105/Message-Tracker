package com.example.myapplication.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.myapplication.data.AppPreferences
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entities.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName

        // Kiểm tra xem App này có được phép lưu không
        val allowedApps = AppPreferences.getSelectedPackages(applicationContext)

        if (allowedApps.contains(packageName)) {
            val extras = sbn.notification.extras

            var finalTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            if (finalTitle.isNullOrEmpty()) {
                finalTitle = extras.getString(Notification.EXTRA_TITLE) ?: "Không tên"
            }

            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val postTime = sbn.postTime

            // Lọc tin rác
            if (finalTitle == "Messenger" ||
                text.contains("Bong bóng chat") ||
                text.contains("Đang hoạt động") ||
                text.isEmpty()) {
                return
            }

            // Xử lý Avatar
            var avatarBytes: ByteArray? = null
            try {
                val largeIcon = sbn.notification.getLargeIcon()
                if (largeIcon != null) {
                    val bitmap = loadBitmapFromIcon(largeIcon)
                    avatarBytes = bitmapToBytes(bitmap)
                }
            } catch (e: Exception) {
                Log.e("MSG_SAVER", "Lỗi lấy avatar: ${e.message}")
            }

            // --- QUAN TRỌNG: Lưu thêm packageName ---
            val newMessage = Message(
                sender = finalTitle,
                content = text,
                time = postTime,
                avatar = avatarBytes,
                packageName = packageName // Lưu tên gói (ví dụ: com.zing.zalo)
            )

            val db = AppDatabase.Companion.getDatabase(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                db.messageDao().insert(newMessage)

                // Tự động xóa tin cũ quá 24h (nếu muốn)
                // val oneDayAgo = System.currentTimeMillis() - 86400000
                // db.messageDao().deleteOldMessages(oneDayAgo)
            }
        }
    }

    private fun loadBitmapFromIcon(icon: Icon): Bitmap? {
        val drawable = icon.loadDrawable(this) ?: return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun bitmapToBytes(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}