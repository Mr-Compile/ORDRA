package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ordra.data.local.entity.ChatMessageEntity
import com.ordra.data.local.entity.ChatThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_threads ORDER BY updatedAt DESC")
    fun observeThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insertThread(thread: ChatThreadEntity)

    @Update
    suspend fun updateThread(thread: ChatThreadEntity)

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_threads WHERE id = :id")
    suspend fun getThread(id: String): ChatThreadEntity?

    @Query("DELETE FROM chat_threads WHERE id = :id")
    suspend fun deleteThread(id: String)

    @Query("DELETE FROM chat_messages WHERE threadId = :id")
    suspend fun deleteMessages(id: String)
}
