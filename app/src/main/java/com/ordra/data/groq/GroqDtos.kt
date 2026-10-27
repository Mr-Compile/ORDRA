package com.ordra.data.groq

import kotlinx.serialization.Serializable

@Serializable
data class GroqChatRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    @kotlinx.serialization.SerialName("response_format")
    val responseFormat: GroqResponseFormat = GroqResponseFormat(),
    val temperature: Double = 0.1,
)

@Serializable
data class GroqMessage(val role: String, val content: String)

@Serializable
data class GroqResponseFormat(val type: String = "json_object")

@Serializable
data class GroqChatResponse(val choices: List<GroqChoice> = emptyList())

@Serializable
data class GroqChoice(val message: GroqMessage? = null)

/** Strict extraction DTO — every field nullable; unknown values rejected later. */
@Serializable
data class GroqExtractionDto(
    val title: String? = null,
    val platform: String? = null,
    val sellerName: String? = null,
    val totalAmountMinor: Long? = null,
    val shippingMinor: Long? = null,
    val discountMinor: Long? = null,
    val paymentMethod: String? = null,
    val courier: String? = null,
    val trackingNumber: String? = null,
    val shipmentStatus: String? = null,
    val orderedDate: String? = null,
    val etaStart: String? = null,
    val etaEnd: String? = null,
    val itemNames: List<String> = emptyList(),
)
