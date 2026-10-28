package com.ordra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
@Entity(
    tableName = "chat_messages",
    indices = [Index("threadId", "createdAt")],
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val role: String,
    val text: String,
    val createdAt: Long,
)
