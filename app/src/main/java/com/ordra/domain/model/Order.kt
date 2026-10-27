package com.ordra.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Universal order aggregate — one model for every platform (see DOMAIN_MODELS contract).
 * Money is integer minor units + [currencyCode]; never floating point.
 * Instants are epoch-based; estimated delivery fields are date-only.
 * [archivedAt] doubles as the archive flag: null = active, non-null = archived.
 * [items] is the owned item list (Order 1→N OrderItem).
 */
data class Order(
    val id: String,
    val title: String,
    val platform: Platform,
    val customPlatformName: String? = null,
    val sellerName: String? = null,
    val status: OrderStatus = OrderStatus.ORDERED,
    val paymentMethod: PaymentMethod = PaymentMethod.UNKNOWN,
    val paymentStatus: PaymentStatus = PaymentStatus.UNKNOWN,
    val isCod: Boolean = false,
    val currencyCode: String = "PHP",
    val itemSubtotalMinor: Long? = null,
    val shippingFeeMinor: Long? = null,
    val discountMinor: Long? = null,
    val totalAmountMinor: Long? = null,
    val courier: String? = null,
    val trackingNumber: String? = null,
    val orderedDate: Instant? = null,
    val estimatedDeliveryStart: LocalDate? = null,
    val estimatedDeliveryEnd: LocalDate? = null,
    val deliveredAt: Instant? = null,
    val notes: String? = null,
    val productImageUri: String? = null,
    val archivedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val items: List<OrderItem> = emptyList(),
) {
    val isArchived: Boolean get() = archivedAt != null
}
