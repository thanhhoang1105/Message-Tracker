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

/**
 * Background service that listens for incoming notifications and saves them to the database.
 */
class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName

        // Check if this app is selected for monitoring
        val allowedApps = AppPreferences.getSelectedPackages(applicationContext)

        if (allowedApps.contains(packageName)) {
            val extras = sbn.notification.extras

            // Handle conversation title (Group name or Sender name)
            var finalTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            if (finalTitle.isNullOrEmpty()) {
                finalTitle = extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
            }

            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val postTime = sbn.postTime

            // Filter out system or junk notifications
            if (finalTitle == "Messenger" ||
                text.contains("Chat head") ||
                text.contains("Active now") ||
                text.isEmpty()) {
                return
            }

            // Process Avatar icon
            var avatarBytes: ByteArray? = null
            try {
                val largeIcon = sbn.notification.getLargeIcon()
                if (largeIcon != null) {
                    val bitmap = loadBitmapFromIcon(largeIcon)
                    avatarBytes = bitmapToBytes(bitmap)
                }
            } catch (e: Exception) {
                Log.e("MSG_SAVER", "Error fetching avatar: ${e.message}")
            }

            // Save message to database
            val newMessage = Message(
                sender = finalTitle,
                content = text,
                time = postTime,
                avatar = avatarBytes,
                packageName = packageName // Store package name for filtering (e.g., com.zing.zalo)
            )

            val db = AppDatabase.Companion.getDatabase(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                db.messageDao().insert(newMessage)

                // Optional: Automatically delete messages older than 24 hours
                // val oneDayAgo = System.currentTimeMillis() - 86400000
                // db.messageDao().deleteOldMessages(oneDayAgo)
            }
        }
    }

    /**
     * Converts a notification Icon to a Bitmap.
     */
    private fun loadBitmapFromIcon(icon: Icon): Bitmap? {
        val drawable = icon.loadDrawable(this) ?: return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Converts a Bitmap to a ByteArray for storage in Room database.
     */
    private fun bitmapToBytes(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
