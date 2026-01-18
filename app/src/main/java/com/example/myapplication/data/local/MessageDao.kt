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

    /**
     * Retrieves detailed message history for a specific sender (Chat Screen).
     */
    @Query("SELECT * FROM message_table WHERE sender = :senderName ORDER BY time ASC")
    fun getMessagesBySender(senderName: String): Flow<List<Message>>

    /**
     * Retrieves the latest message from ALL senders across all apps.
     */
    @Query("SELECT * FROM message_table WHERE id IN (SELECT MAX(id) FROM message_table GROUP BY sender) ORDER BY time DESC")
    fun getListSenders(): Flow<List<Message>>

    /**
     * Retrieves the latest message from each sender, filtered by a specific app package.
     * @param pkgName The package name of the application (e.g., com.facebook.orca).
     */
    @Query("SELECT * FROM message_table WHERE packageName = :pkgName AND id IN (SELECT MAX(id) FROM message_table WHERE packageName = :pkgName GROUP BY sender) ORDER BY time DESC")
    fun getListSendersByApp(pkgName: String): Flow<List<Message>>

    /**
     * Deletes all messages associated with a specific app package.
     */
    @Query("DELETE FROM message_table WHERE packageName = :pkgName")
    suspend fun deleteMessagesByPackage(pkgName: String)

    /**
     * Deletes messages older than a specific time threshold.
     */
    @Query("DELETE FROM message_table WHERE time < :timeThreshold")
    suspend fun deleteOldMessages(timeThreshold: Long)

    /**
     * Deletes all messages from a specific sender.
     */
    @Query("DELETE FROM message_table WHERE sender = :senderName")
    suspend fun deleteMessagesBySender(senderName: String)
}
