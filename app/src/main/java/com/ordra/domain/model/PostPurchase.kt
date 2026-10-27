package com.ordra.domain.model

import java.time.Instant
import java.time.LocalDate

enum class ReturnStatus { REQUESTED, APPROVED, SHIPPED_BACK, COMPLETED, REJECTED }
enum class RefundStatus { PENDING, PARTIAL, FULL, DENIED }

data class PurchaseReturn(
    val id: String,
    val orderId: String,
    val reason: String?,
    val status: ReturnStatus,
    val requestedAt: Instant,
    val resolvedAt: Instant?,
    val notes: String?,
)

data class Refund(
    val id: String,
    val orderId: String,
    val amountMinor: Long?,
    val status: RefundStatus,
    val reason: String?,
    val requestedAt: Instant,
    val processedAt: Instant?,
)

data class Warranty(
    val id: String,
    val orderId: String,
    val provider: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String?,
) {
    init { require(!endDate.isBefore(startDate)) { "Warranty end before start" } }
}

data class Receipt(
    val id: String,
    val orderId: String,
    val imageUri: String,
    val label: String?,
    val addedAt: Instant,
)
