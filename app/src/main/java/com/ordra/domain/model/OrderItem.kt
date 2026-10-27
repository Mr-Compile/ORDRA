package com.ordra.domain.model

data class OrderItem(
    val id: String,
    val orderId: String,
    val name: String,
    val variant: String? = null,
    val quantity: Int = 1,
    val unitPriceMinor: Long? = null,
    val lineTotalMinor: Long? = null,
    val imageUri: String? = null,
    val notes: String? = null,
)
