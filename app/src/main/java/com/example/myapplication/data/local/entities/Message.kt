package com.example.myapplication.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val time: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.Companion.BLOB) val avatar: ByteArray? = null,
    val packageName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Message
        if (id != other.id) return false
        if (!avatar.contentEquals(other.avatar)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (avatar?.contentHashCode() ?: 0)
        return result
    }
}