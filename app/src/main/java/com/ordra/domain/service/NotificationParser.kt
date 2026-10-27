package com.ordra.domain.service

import com.ordra.core.common.MoneyFormat
import com.ordra.domain.model.Courier
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus

/**
 * Deterministic notification-text -> signals. English + Filipino delivery
 * wording; irrelevant notifications yield isDeliveryRelevant=false and are
 * never stored.
 */
object NotificationParser {

    private val RELEVANT = listOf(
        "parcel", "package", "delivery", "deliver", "delivered", "shipment",
        "tracking", "courier", "rider", "out for delivery", "pickup", "picked up",
        "cod", "cash on delivery", "order", "nasa daan", "padala", "darating",
        "ihatid", "nagdeliver", "in transit", "on the way", "arrived",
    )

    private val COURIER_KEYWORDS = mapOf(
        Courier.SPX_EXPRESS to listOf("spx", "shopee express"),
        Courier.JNT_EXPRESS to listOf("j&t", "jnt"),
        Courier.NINJA_VAN to listOf("ninja van", "ninjavan"),
        Courier.FLASH_EXPRESS to listOf("flash express", "flash"),
        Courier.LBC to listOf("lbc"),
        Courier.JRS_EXPRESS to listOf("jrs"),
        Courier.GOGO_XPRESS to listOf("gogo"),
        Courier.ENTREGO to listOf("entrego"),
        Courier.TWO_GO to listOf("2go"),
        Courier.DHL to listOf("dhl"),
    )

    private val PLATFORM_KEYWORDS = mapOf(
        Platform.SHOPEE to listOf("shopee"),
        Platform.TIKTOK_SHOP to listOf("tiktok"),
        Platform.LAZADA to listOf("lazada"),
        Platform.TEMU to listOf("temu"),
        Platform.AMAZON to listOf("amazon"),
        Platform.FACEBOOK to listOf("facebook", "messenger"),
        Platform.INSTAGRAM to listOf("instagram"),
    )

    private val TRACKING_RE = Regex(
        "\\b([A-Z]{2,5}[0-9]{5,}[A-Z0-9]*|[A-Z]+PH[0-9]+|SPX[A-Z0-9]{5,})\\b",
    )
    private val AMOUNT_RE = Regex(
        "(?:PHP|₱|\\$|P)\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{1,2})?|\\d+\\.\\d{1,2})",
    )
    private val RIDER = listOf("rider", "kuya", "driver", "delivery person", "paparating")

    fun isRelevant(text: String): Boolean {
        val l = text.lowercase()
        return RELEVANT.any { l.contains(it) }
    }

    fun parse(text: String): NotificationSignals {
        if (!isRelevant(text)) return NotificationSignals(isDeliveryRelevant = false)
        val l = text.lowercase()
        return NotificationSignals(
            isDeliveryRelevant = true,
            trackingNumber = TRACKING_RE.find(text)?.value,
            courier = COURIER_KEYWORDS.entries
                .firstOrNull { (_, keys) -> keys.any { l.contains(it) } }?.key,
            platform = PLATFORM_KEYWORDS.entries
                .firstOrNull { (_, keys) -> keys.any { l.contains(it) } }?.key,
            statusText = StatusNormalizer.normalize(text)
                .takeIf { it != ShipmentStatus.UNKNOWN }?.name,
            codAmountMinor = AMOUNT_RE.find(text)?.let { MoneyFormat.parseToMinor(it.value) },
            productHint = null,
            riderIndicator = RIDER.any { l.contains(it) },
        )
    }
}
