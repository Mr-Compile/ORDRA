package com.ordra.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ordra.R
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform

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

@Composable
fun orderStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.DRAFT -> stringResource(R.string.status_draft)
    OrderStatus.ORDERED -> stringResource(R.string.status_ordered)
    OrderStatus.PROCESSING -> stringResource(R.string.status_processing)
    OrderStatus.PACKED -> stringResource(R.string.status_packed)
    OrderStatus.SHIPPED -> stringResource(R.string.status_shipped)
    OrderStatus.OUT_FOR_DELIVERY -> stringResource(R.string.status_out_for_delivery)
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
