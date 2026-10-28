package com.ordra.domain.model

import java.time.Instant

enum class ChatRole { USER, ORDI }

data class ChatThread(
    val id: String,
    val title: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ChatMessage(
    val id: String,
    val threadId: String,
    val role: ChatRole,
    val text: String,
    val createdAt: Instant,
)
