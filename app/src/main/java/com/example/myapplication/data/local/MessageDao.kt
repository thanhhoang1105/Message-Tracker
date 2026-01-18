package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entities.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    // Lấy tin nhắn chi tiết (Màn hình chat)
    @Query("SELECT * FROM message_table WHERE sender = :senderName ORDER BY time ASC")
    fun getMessagesBySender(senderName: String): Flow<List<Message>>

    // Lệnh 1: Lấy TẤT CẢ (Khi chưa chọn app nào)
    @Query("SELECT * FROM message_table WHERE id IN (SELECT MAX(id) FROM message_table GROUP BY sender) ORDER BY time DESC")
    fun getListSenders(): Flow<List<Message>>

    // Lệnh 2: Lấy tin nhắn THEO APP (SỬA LẠI DÒNG NÀY)
    // Nguyên tắc: Phải có "WHERE packageName = :pkgName" ở cả 2 chỗ (câu lệnh chính và câu lệnh con)
    @Query("SELECT * FROM message_table WHERE packageName = :pkgName AND id IN (SELECT MAX(id) FROM message_table WHERE packageName = :pkgName GROUP BY sender) ORDER BY time DESC")
    fun getListSendersByApp(pkgName: String): Flow<List<Message>>

    // Xóa tin theo App
    @Query("DELETE FROM message_table WHERE packageName = :pkgName")
    suspend fun deleteMessagesByPackage(pkgName: String)

    @Query("DELETE FROM message_table WHERE time < :timeThreshold")
    suspend fun deleteOldMessages(timeThreshold: Long)

    @Query("DELETE FROM message_table WHERE sender = :senderName")
    suspend fun deleteMessagesBySender(senderName: String)
}