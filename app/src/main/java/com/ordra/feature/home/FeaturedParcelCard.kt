package com.ordra.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.components.StatusChip
import com.ordra.core.ui.courierLabel
import com.ordra.core.ui.orderStatusLabel
import com.ordra.core.ui.platformColor
import com.ordra.core.ui.platformLabel
import com.ordra.core.ui.shipmentStatusLabel
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FEATURED_DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
private const val SHIP_STEP_COUNT = 6

/** Shipment pipeline position — mirrors SHIP_STATUSES.step in mockup/app.js (-1 = off track). */
private fun shipStep(status: ShipmentStatus): Int = when (status) {
    ShipmentStatus.PREPARING -> 0
    ShipmentStatus.PICKED_UP -> 1
    ShipmentStatus.IN_TRANSIT -> 2
    ShipmentStatus.ARRIVED_AT_HUB -> 3
    ShipmentStatus.OUT_FOR_DELIVERY -> 4
    ShipmentStatus.DELIVERED -> 5
    else -> -1
}

/**
 * Home's hero parcel — the most important current shipment (platform badge, COD chip,
 * courier/tracking, order + shipment chips, pipeline progress, ETA, total). Mirrors
 * the `parcel-featured` card in mockup/app.js.
 */
@Composable
fun FeaturedParcelCard(
    order: Order,
    shipment: Shipment?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlatformChip(order)
                    if (order.isCod && order.paymentStatus != PaymentStatus.PAID) CodChip()
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    order.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                shipment?.let { ship ->
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            courierLabel(ship.courier, ship.customCourierName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ship.trackingNumber?.let {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(order.status)
                    shipment?.let { ShipmentChip(it.shipmentStatus) }
                }
                shipment?.let { ship ->
                    val step = shipStep(ship.shipmentStatus)
                    if (step >= 0) {
                        Spacer(Modifier.height(8.dp))
                        ShipProgressDots(step)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        featuredEtaText(order),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                order.totalAmountMinor?.let {
                    Text(
                        MoneyFormat.formatMinor(it, order.currencyCode),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlatformChip(order: Order) {
    val tint = platformColor(order.platform)
    Surface(color = tint.copy(alpha = 0.11f), shape = MaterialTheme.shapes.small) {
        Text(
            text = platformLabel(order.platform, order.customPlatformName),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun CodChip() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(
                Icons.Default.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = stringResource(R.string.payment_cod),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun ShipmentChip(status: ShipmentStatus) {
    val (container, content) = when (status) {
        ShipmentStatus.DELIVERED ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ShipmentStatus.DELIVERY_FAILED, ShipmentStatus.RETURNING, ShipmentStatus.RETURNED ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ShipmentStatus.UNKNOWN ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Text(
            text = shipmentStatusLabel(status),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ShipProgressDots(step: Int) {
    val done = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.surfaceVariant
    val progressLabel = stringResource(R.string.featured_shipment_progress)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            contentDescription = progressLabel
        },
    ) {
        for (i in 0 until SHIP_STEP_COUNT) {
            if (i > 0) {
                Box(
                    Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(if (i <= step) done else off),
                )
            }
            Box(
                Modifier
                    .size(if (i == step) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (i <= step) done else off),
            )
        }
    }
}

@Composable
private fun featuredEtaText(order: Order): String = when {
    order.status == OrderStatus.DELIVERED -> {
        val date = order.deliveredAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
        if (date != null) stringResource(R.string.featured_delivered_on, date.format(FEATURED_DATE_FMT))
        else orderStatusLabel(order.status)
    }
    order.status in setOf(OrderStatus.CANCELLED, OrderStatus.RETURNED, OrderStatus.REFUNDED) ->
        orderStatusLabel(order.status)
    order.estimatedDeliveryStart != null && order.estimatedDeliveryEnd != null &&
        order.estimatedDeliveryStart != order.estimatedDeliveryEnd ->
        stringResource(
            R.string.featured_eta_range,
            order.estimatedDeliveryStart.format(FEATURED_DATE_FMT),
            order.estimatedDeliveryEnd.format(FEATURED_DATE_FMT),
        )
    order.estimatedDeliveryStart != null || order.estimatedDeliveryEnd != null ->
        stringResource(
            R.string.featured_eta_single,
            (order.estimatedDeliveryStart ?: order.estimatedDeliveryEnd)!!.format(FEATURED_DATE_FMT),
        )
    else -> stringResource(R.string.featured_no_eta)
}
