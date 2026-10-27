package com.ordra.domain.model

enum class OrderStatus {
    DRAFT,
    ORDERED,
    PROCESSING,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    REFUNDED,
}
