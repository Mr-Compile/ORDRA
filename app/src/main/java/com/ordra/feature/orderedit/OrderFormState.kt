package com.ordra.feature.orderedit

import com.ordra.core.common.MoneyFormat
import com.ordra.domain.model.Courier
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderItem
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ReminderType
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.TrackingStatusSource
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class OrderItemForm(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val variant: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
) {
    val isCompletelyEmpty: Boolean
        get() = name.isBlank() && variant.isBlank() && unitPrice.isBlank() && quantity == "1"
}

/**
 * Editable form mirror of [Order]. Strings keep raw user input; conversion to
 * domain happens only in [toOrder] after validation.
 */
data class OrderFormState(
    val orderId: String? = null,
    val title: String = "",
    val platform: Platform? = null,
    val customPlatformName: String = "",
    val sellerName: String = "",
    val items: List<OrderItemForm> = listOf(OrderItemForm()),
    val status: OrderStatus = OrderStatus.ORDERED,
    val paymentMethod: PaymentMethod = PaymentMethod.UNKNOWN,
    val paymentStatus: PaymentStatus = PaymentStatus.UNKNOWN,
    val shipping: String = "",
    val discount: String = "",
    val total: String = "",
    // Shipment fields — persisted on the primary Shipment, never on Order.
    val courier: Courier = Courier.UNKNOWN,
    val customCourierName: String = "",
    val trackingNumber: String = "",
    val orderedDate: LocalDate? = null,
    val etaStart: LocalDate? = null,
    val etaEnd: LocalDate? = null,
    val notes: String = "",
    val productImageUri: String? = null,
    val currencyCode: String = "PHP",
    val selectedCategoryIds: Set<String> = emptySet(),
    val reminderTypes: Set<ReminderType> = emptySet(),
    val isDirty: Boolean = false,
) {
    val isEdit: Boolean get() = orderId != null

    /** Sum of item line totals (unitPrice x quantity) where both parse. */
    val itemsSubtotalMinor: Long
        get() = items.sumOf { item ->
            val unit = MoneyFormat.parseToMinor(item.unitPrice) ?: return@sumOf 0L
            val qty = item.quantity.toIntOrNull() ?: 0
            if (unit > 0 && qty > 0) unit * qty else 0L
        }

    /** Suggested total = items subtotal + shipping - discount. */
    val suggestedTotalMinor: Long
        get() = itemsSubtotalMinor +
            (MoneyFormat.parseToMinor(shipping) ?: 0L) -
            (MoneyFormat.parseToMinor(discount) ?: 0L)

    /** Field-key -> message. Minimum required: name-or-item, platform, valid total. */
    fun fieldErrors(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val hasName = title.isNotBlank() || items.any { it.name.isNotBlank() }
        if (!hasName) errors["title"] = "Add an order name or at least one item"
        if (platform == null) errors["platform"] = "Platform is required"
        if (platform == Platform.OTHER && customPlatformName.isBlank()) {
            errors["customPlatformName"] = "Name the platform or store"
        }
        if (MoneyFormat.parseToMinor(total) == null) errors["total"] = "Valid total is required"
        items.forEachIndexed { index, item ->
            if (!item.isCompletelyEmpty && item.name.isBlank()) {
                errors["item_name_$index"] = "Item name is required"
            }
            if (!item.isCompletelyEmpty) {
                val qty = item.quantity.toIntOrNull()
                if (qty == null || qty < 1) errors["item_qty_$index"] = "Quantity must be at least 1"
            }
        }
        if (etaStart != null && etaEnd != null && etaEnd.isBefore(etaStart)) {
            errors["etaEnd"] = "End date can't be before start"
        }
        if (shipping.isNotBlank() && MoneyFormat.parseToMinor(shipping) == null) {
            errors["shipping"] = "Invalid amount"
        }
        if (discount.isNotBlank() && MoneyFormat.parseToMinor(discount) == null) {
            errors["discount"] = "Invalid amount"
        }
        if (courier == Courier.OTHER && customCourierName.isBlank()) {
            errors["customCourierName"] = "Name the courier"
        }
        return errors
    }

    fun isValid(): Boolean = fieldErrors().isEmpty()

    fun toOrder(existing: Order?, now: Instant): Order {
        val domainItems = items
            .filterNot { it.isCompletelyEmpty }
            .map { item ->
                val qty = item.quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val unit = MoneyFormat.parseToMinor(item.unitPrice)
                OrderItem(
                    id = item.id,
                    orderId = orderId ?: existing?.id ?: "",
                    name = item.name.trim(),
                    variant = item.variant.ifBlank { null },
                    quantity = qty,
                    unitPriceMinor = unit,
                    lineTotalMinor = unit?.let { it * qty },
                )
            }
        val effectiveTitle = title.trim().ifBlank {
            domainItems.firstOrNull()?.name ?: "Untitled order"
        }
        return Order(
            id = orderId ?: existing?.id ?: UUID.randomUUID().toString(),
            title = effectiveTitle,
            platform = platform ?: Platform.OTHER,
            customPlatformName = customPlatformName.ifBlank { null },
            sellerName = sellerName.ifBlank { null },
            status = status,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            isCod = paymentMethod == PaymentMethod.COD,
            currencyCode = currencyCode,
            itemSubtotalMinor = itemsSubtotalMinor.takeIf { it > 0 },
            shippingFeeMinor = MoneyFormat.parseToMinor(shipping),
            discountMinor = MoneyFormat.parseToMinor(discount),
            totalAmountMinor = MoneyFormat.parseToMinor(total),
            orderedDate = orderedDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant(),
            estimatedDeliveryStart = etaStart,
            estimatedDeliveryEnd = etaEnd,
            deliveredAt = existing?.deliveredAt,
            rating = existing?.rating,
            isFavorite = existing?.isFavorite ?: false,
            notes = notes.ifBlank { null },
            productImageUri = productImageUri,
            archivedAt = existing?.archivedAt,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            items = domainItems,
        )
    }

    /**
     * Shipment fields map onto the order's primary [Shipment]. Null when the
     * user entered no shipment data and none existed — avoids empty rows.
     */
    fun toShipment(orderId: String, existing: Shipment?, now: Instant): Shipment? {
        val hasData = courier != Courier.UNKNOWN ||
            trackingNumber.isNotBlank() ||
            customCourierName.isNotBlank()
        if (!hasData && existing == null) return null
        return Shipment(
            id = existing?.id ?: UUID.randomUUID().toString(),
            orderId = orderId,
            courier = courier,
            customCourierName = customCourierName.ifBlank { null },
            trackingNumber = trackingNumber.ifBlank { null },
            shipmentStatus = existing?.shipmentStatus ?: ShipmentStatus.UNKNOWN,
            statusSource = existing?.statusSource ?: TrackingStatusSource.MANUAL,
            statusConfidence = existing?.statusConfidence ?: DetectionConfidence.HIGH,
            lastStatusAt = existing?.lastStatusAt,
            lastDetectedAt = existing?.lastDetectedAt,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
    }

    companion object {
        fun fromOrder(
            order: Order,
            shipment: Shipment? = null,
            categoryIds: Set<String> = emptySet(),
            reminderTypes: Set<ReminderType> = emptySet(),
        ): OrderFormState = OrderFormState(
            orderId = order.id,
            title = order.title,
            platform = order.platform,
            customPlatformName = order.customPlatformName.orEmpty(),
            sellerName = order.sellerName.orEmpty(),
            items = order.items.map {
                OrderItemForm(
                    id = it.id,
                    name = it.name,
                    variant = it.variant.orEmpty(),
                    quantity = it.quantity.toString(),
                    unitPrice = it.unitPriceMinor?.let { m ->
                        java.math.BigDecimal(m).movePointLeft(2).stripTrailingZeros().toPlainString()
                    }.orEmpty(),
                )
            }.ifEmpty { listOf(OrderItemForm()) },
            status = order.status,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            shipping = order.shippingFeeMinor?.let {
                java.math.BigDecimal(it).movePointLeft(2).stripTrailingZeros().toPlainString()
            }.orEmpty(),
            discount = order.discountMinor?.let {
                java.math.BigDecimal(it).movePointLeft(2).stripTrailingZeros().toPlainString()
            }.orEmpty(),
            total = order.totalAmountMinor?.let {
                java.math.BigDecimal(it).movePointLeft(2).stripTrailingZeros().toPlainString()
            }.orEmpty(),
            courier = shipment?.courier ?: Courier.UNKNOWN,
            customCourierName = shipment?.customCourierName.orEmpty(),
            trackingNumber = shipment?.trackingNumber.orEmpty(),
            orderedDate = order.orderedDate
                ?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate(),
            etaStart = order.estimatedDeliveryStart,
            etaEnd = order.estimatedDeliveryEnd,
            notes = order.notes.orEmpty(),
            productImageUri = order.productImageUri,
            currencyCode = order.currencyCode,
            selectedCategoryIds = categoryIds,
            reminderTypes = reminderTypes,
            isDirty = false,
        )
    }
}
