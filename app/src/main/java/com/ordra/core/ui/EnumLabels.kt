package com.ordra.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ordra.R
import com.ordra.domain.model.Courier
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus

@Composable
fun platformLabel(platform: Platform, customName: String? = null): String =
    when (platform) {
        Platform.SHOPEE -> stringResource(R.string.platform_shopee)
        Platform.TIKTOK_SHOP -> stringResource(R.string.platform_tiktok)
        Platform.LAZADA -> stringResource(R.string.platform_lazada)
        Platform.TEMU -> stringResource(R.string.platform_temu)
        Platform.AMAZON -> stringResource(R.string.platform_amazon)
        Platform.FACEBOOK -> stringResource(R.string.platform_facebook)
        Platform.INSTAGRAM -> stringResource(R.string.platform_instagram)
        Platform.OTHER -> customName?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.platform_other)
    }

/** Brand tint per platform — mirrors the PLATFORMS palette in mockup/app.js. */
fun platformColor(platform: Platform): Color = when (platform) {
    Platform.SHOPEE -> Color(0xFFEE4D2D)
    Platform.TIKTOK_SHOP -> Color(0xFF171821)
    Platform.LAZADA -> Color(0xFF1A0DAB)
    Platform.TEMU -> Color(0xFFFB7701)
    Platform.AMAZON -> Color(0xFF232F3E)
    Platform.FACEBOOK -> Color(0xFF1877F2)
    Platform.INSTAGRAM -> Color(0xFFC13584)
    Platform.OTHER -> Color(0xFF666979)
}

@Composable
fun orderStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.DRAFT -> stringResource(R.string.status_draft)
    OrderStatus.ORDERED -> stringResource(R.string.status_ordered)
    OrderStatus.PROCESSING -> stringResource(R.string.status_processing)
    OrderStatus.PACKED -> stringResource(R.string.status_packed)
    OrderStatus.SHIPPED -> stringResource(R.string.status_shipped)
    OrderStatus.DELIVERED -> stringResource(R.string.status_delivered)
    OrderStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    OrderStatus.RETURNED -> stringResource(R.string.status_returned)
    OrderStatus.REFUNDED -> stringResource(R.string.status_refunded)
}

@Composable
fun paymentMethodLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.COD -> stringResource(R.string.payment_cod)
    PaymentMethod.EWALLET -> stringResource(R.string.payment_ewallet)
    PaymentMethod.CARD -> stringResource(R.string.payment_card)
    PaymentMethod.BANK_TRANSFER -> stringResource(R.string.payment_bank)
    PaymentMethod.CASH -> stringResource(R.string.payment_cash)
    PaymentMethod.OTHER -> stringResource(R.string.payment_other)
    PaymentMethod.UNKNOWN -> stringResource(R.string.payment_unknown)
}

@Composable
fun paymentStatusLabel(status: PaymentStatus): String = when (status) {
    PaymentStatus.UNPAID -> stringResource(R.string.paystatus_unpaid)
    PaymentStatus.PARTIALLY_PAID -> stringResource(R.string.paystatus_partial)
    PaymentStatus.PAID -> stringResource(R.string.paystatus_paid)
    PaymentStatus.REFUNDED -> stringResource(R.string.paystatus_refunded)
    PaymentStatus.UNKNOWN -> stringResource(R.string.payment_unknown)
}

@Composable
fun courierLabel(courier: Courier, customName: String? = null): String = when (courier) {
    Courier.SPX_EXPRESS -> "SPX Express"
    Courier.JNT_EXPRESS -> "J&T Express"
    Courier.NINJA_VAN -> "Ninja Van"
    Courier.FLASH_EXPRESS -> "Flash Express"
    Courier.LBC -> "LBC"
    Courier.JRS_EXPRESS -> "JRS Express"
    Courier.GOGO_XPRESS -> "GOGO Xpress"
    Courier.ENTREGO -> "Entrego"
    Courier.TWO_GO -> "2GO"
    Courier.DHL -> "DHL"
    Courier.OTHER -> customName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.platform_other)
    Courier.UNKNOWN -> stringResource(R.string.payment_unknown)
}

@Composable
fun shipmentStatusLabel(status: ShipmentStatus): String = when (status) {
    ShipmentStatus.UNKNOWN -> stringResource(R.string.ship_unknown)
    ShipmentStatus.PREPARING -> stringResource(R.string.ship_preparing)
    ShipmentStatus.PICKED_UP -> stringResource(R.string.ship_picked_up)
    ShipmentStatus.IN_TRANSIT -> stringResource(R.string.ship_in_transit)
    ShipmentStatus.ARRIVED_AT_HUB -> stringResource(R.string.ship_arrived_hub)
    ShipmentStatus.OUT_FOR_DELIVERY -> stringResource(R.string.ship_out_for_delivery)
    ShipmentStatus.DELIVERED -> stringResource(R.string.ship_delivered)
    ShipmentStatus.DELIVERY_FAILED -> stringResource(R.string.ship_delivery_failed)
    ShipmentStatus.RETURNING -> stringResource(R.string.ship_returning)
    ShipmentStatus.RETURNED -> stringResource(R.string.ship_returned)
}
