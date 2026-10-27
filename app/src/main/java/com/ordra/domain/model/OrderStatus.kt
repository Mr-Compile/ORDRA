package com.ordra.domain.model

/**
 * Order lifecycle only — courier logistics states (e.g. out-for-delivery)
 * live on [ShipmentStatus], not here.
 */
enum class OrderStatus {
    DRAFT,
    ORDERED,
    PROCESSING,
    PACKED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED,
    REFUNDED,
}
