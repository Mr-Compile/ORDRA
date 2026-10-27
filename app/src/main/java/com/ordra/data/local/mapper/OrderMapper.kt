package com.ordra.data.local.mapper

import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.OrderWithItems
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderItem
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import java.time.Instant
import java.time.LocalDate

object OrderMapper {

    fun toDomain(entity: OrderWithItems): Order = with(entity.order) {
        Order(
            id = id,
            title = title,
            platform = Platform.valueOf(platform),
            customPlatformName = customPlatformName,
            sellerName = sellerName,
            status = OrderStatus.valueOf(status),
            paymentMethod = PaymentMethod.valueOf(paymentMethod),
            paymentStatus = PaymentStatus.valueOf(paymentStatus),
            isCod = isCod,
            currencyCode = currencyCode,
            itemSubtotalMinor = itemSubtotalMinor,
            shippingFeeMinor = shippingFeeMinor,
            discountMinor = discountMinor,
            totalAmountMinor = totalAmountMinor,
            orderedDate = orderedDate?.let(Instant::ofEpochMilli),
            estimatedDeliveryStart = estimatedDeliveryStart?.let(LocalDate::ofEpochDay),
            estimatedDeliveryEnd = estimatedDeliveryEnd?.let(LocalDate::ofEpochDay),
            deliveredAt = deliveredAt?.let(Instant::ofEpochMilli),
            notes = notes,
            productImageUri = productImageUri,
            archivedAt = archivedAt?.let(Instant::ofEpochMilli),
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt),
            items = entity.items.map(::toDomain),
        )
    }

    fun toEntity(domain: Order): OrderEntity = with(domain) {
        OrderEntity(
            id = id,
            title = title,
            platform = platform.name,
            customPlatformName = customPlatformName,
            sellerName = sellerName,
            status = status.name,
            paymentMethod = paymentMethod.name,
            paymentStatus = paymentStatus.name,
            isCod = isCod,
            currencyCode = currencyCode,
            itemSubtotalMinor = itemSubtotalMinor,
            shippingFeeMinor = shippingFeeMinor,
            discountMinor = discountMinor,
            totalAmountMinor = totalAmountMinor,
            orderedDate = orderedDate?.toEpochMilli(),
            estimatedDeliveryStart = estimatedDeliveryStart?.toEpochDay(),
            estimatedDeliveryEnd = estimatedDeliveryEnd?.toEpochDay(),
            deliveredAt = deliveredAt?.toEpochMilli(),
            notes = notes,
            productImageUri = productImageUri,
            archivedAt = archivedAt?.toEpochMilli(),
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli(),
        )
    }

    fun toDomain(entity: OrderItemEntity): OrderItem = OrderItem(
        id = entity.id,
        orderId = entity.orderId,
        name = entity.name,
        variant = entity.variant,
        quantity = entity.quantity,
        unitPriceMinor = entity.unitPriceMinor,
        lineTotalMinor = entity.lineTotalMinor,
        imageUri = entity.imageUri,
        notes = entity.notes,
    )

    fun toEntity(domain: OrderItem): OrderItemEntity = OrderItemEntity(
        id = domain.id,
        orderId = domain.orderId,
        name = domain.name,
        variant = domain.variant,
        quantity = domain.quantity,
        unitPriceMinor = domain.unitPriceMinor,
        lineTotalMinor = domain.lineTotalMinor,
        imageUri = domain.imageUri,
        notes = domain.notes,
    )
}
