package com.ordra.domain.model

/** Physical parcel movement — distinct from the order lifecycle status. */
enum class ShipmentStatus {
    UNKNOWN,
    PREPARING,
    PICKED_UP,
    IN_TRANSIT,
    ARRIVED_AT_HUB,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNING,
    RETURNED,
}
