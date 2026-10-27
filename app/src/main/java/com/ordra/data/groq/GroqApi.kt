package com.ordra.data.groq

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest,
    ): GroqChatResponse
}
