package com.ordra.domain.repository

import com.ordra.domain.model.ChatMessage
import com.ordra.domain.model.ChatThread
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeThreads(): Flow<List<ChatThread>>
    fun observeMessages(threadId: String): Flow<List<ChatMessage>>

    /** Creates a thread (title from [firstPrompt]) and returns its id. */
    suspend fun createThread(firstPrompt: String): String

    /** Persists the user message, generates Ordi's local reply, persists it. */
    suspend fun send(threadId: String, text: String)

    suspend fun deleteThread(threadId: String)
}
