package com.ordra.domain.service

import com.ordra.core.common.MoneyFormat
import com.ordra.domain.model.Courier
import com.ordra.domain.model.ExtractionField
import com.ordra.domain.model.ExtractionSource
import com.ordra.domain.model.OrderDraft
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.SmartImportResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Deterministic OCR-text -> OrderDraft parser. Pure Kotlin — unit-testable. */
object SmartImportParser {

    private val PLATFORM_HINTS = listOf(
        Platform.SHOPEE to listOf("shopee", "spx", "shopee express"),
        Platform.TIKTOK_SHOP to listOf("tiktok shop", "tiktok"),
        Platform.LAZADA to listOf("lazada"),
        Platform.TEMU to listOf("temu"),
        Platform.AMAZON to listOf("amazon"),
        Platform.FACEBOOK to listOf("facebook", "messenger"),
        Platform.INSTAGRAM to listOf("instagram"),
    )

    private val COURIER_HINTS = listOf(
        Courier.SPX_EXPRESS to listOf("spx", "shopee express", "spx express"),
        Courier.JNT_EXPRESS to listOf("j&t", "jnt", "j&t express"),
        Courier.NINJA_VAN to listOf("ninja van", "ninjavan"),
        Courier.FLASH_EXPRESS to listOf("flash express", "flash"),
        Courier.LBC to listOf("lbc"),
        Courier.JRS_EXPRESS to listOf("jrs"),
        Courier.GOGO_XPRESS to listOf("gogo", "gogo xpress", "gogo express"),
        Courier.ENTREGO to listOf("entrego"),
        Courier.TWO_GO to listOf("2go"),
        Courier.DHL to listOf("dhl"),
    )

    private val AMOUNT_RE = Regex(
        "(?:PHP|₱|\\$|P)?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{1,2})?|\\d+\\.\\d{1,2})",
    )
    private val TRACKING_RE = Regex(
        "\\b([A-Z]{2,5}[0-9]{5,}[A-Z0-9]*|[A-Z]+PH[0-9]+|SPX[A-Z0-9]+)\\b",
    )
    private val DATE_RES = listOf(
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
    )
    private val DATE_TEXT_RE = Regex(
        "\\b(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{4}|\\d{4}-\\d{2}-\\d{2}|" +
            "\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}|[A-Za-z]+\\s+\\d{1,2},?\\s+\\d{4})\\b",
    )

    fun parse(rawText: String): SmartImportResult {
        val text = rawText.trim()
        val lower = text.lowercase()
        val warnings = mutableListOf<String>()
        if (text.isEmpty()) warnings += "no_text"

        val platform = PLATFORM_HINTS
            .firstOrNull { (_, keys) -> keys.any { lower.contains(it) } }?.first
        val courier = COURIER_HINTS
            .firstOrNull { (_, keys) -> keys.any { lower.contains(it) } }?.first

        fun amountNear(vararg labels: String): Pair<Long?, String?> {
            for (label in labels) {
                val idx = lower.indexOf(label)
                if (idx < 0) continue
                val window = text.substring(
                    idx,
                    (idx + label.length + 40).coerceAtMost(text.length),
                )
                val m = AMOUNT_RE.find(window) ?: continue
                val minor = MoneyFormat.parseToMinor(m.value)
                if (minor != null) return minor to m.value
            }
            return null to null
        }
        val (total, totalEvidence) = amountNear(
            "grand total", "order total", "total", "amount due",
        )
        val (shipping, shipEvidence) = amountNear(
            "shipping fee", "shipping", "delivery fee",
        )
        val (discount, discEvidence) = amountNear("discount", "voucher", "promo")

        val tracking = TRACKING_RE.find(text)?.value
        val status = StatusNormalizer.normalize(text)
        val isCod = lower.contains("cod") || lower.contains("cash on delivery")

        val dates = DATE_TEXT_RE.findAll(text).mapNotNull { m ->
            DATE_RES.firstNotNullOfOrNull { fmt ->
                runCatching { LocalDate.parse(m.value.trim(), fmt) }.getOrNull()
            }
        }.toList().distinct().sorted()
        val today = LocalDate.now()
        val future = dates.filter { !it.isBefore(today) }
        val past = dates.filter { it.isBefore(today) }

        // Title: first meaningful line that isn't an order-id line
        val titleCandidate = text.lineSequence()
            .map { it.trim() }
            .filter { it.length in 4..80 }
            .filterNot {
                val l = it.lowercase()
                (l.contains("order") && l.contains("#")) ||
                    l.contains("track") || l.contains("shopee") ||
                    l.contains("lazada") || l.contains("tiktok")
            }
            .firstOrNull()

        val draft = OrderDraft(
            title = ExtractionField(
                titleCandidate,
                if (titleCandidate != null) 0.5f else 0f,
                ExtractionSource.OCR, titleCandidate,
            ),
            platform = ExtractionField(
                platform,
                if (platform != null) 0.8f else 0f,
                ExtractionSource.LOCAL_PARSER, platform?.name,
            ),
            totalMinor = ExtractionField(
                total,
                if (total != null) 0.85f else 0f,
                ExtractionSource.OCR, totalEvidence,
            ),
            shippingMinor = ExtractionField(
                shipping,
                if (shipping != null) 0.7f else 0f,
                ExtractionSource.OCR, shipEvidence,
            ),
            discountMinor = ExtractionField(
                discount,
                if (discount != null) 0.7f else 0f,
                ExtractionSource.OCR, discEvidence,
            ),
            paymentMethod = ExtractionField(
                if (isCod) PaymentMethod.COD else null,
                if (isCod) 0.9f else 0f,
                ExtractionSource.LOCAL_PARSER, "cod keyword",
            ),
            courier = ExtractionField(
                courier,
                if (courier != null) 0.8f else 0f,
                ExtractionSource.LOCAL_PARSER, courier?.name,
            ),
            trackingNumber = ExtractionField(
                tracking,
                if (tracking != null) 0.9f else 0f,
                ExtractionSource.OCR, tracking,
            ),
            shipmentStatus = ExtractionField(
                status.takeIf { it != ShipmentStatus.UNKNOWN },
                if (status != ShipmentStatus.UNKNOWN) 0.8f else 0f,
                ExtractionSource.LOCAL_PARSER, status.name,
            ),
            orderedDate = ExtractionField(
                past.maxOrNull(),
                if (past.isNotEmpty()) 0.6f else 0f,
                ExtractionSource.OCR, past.maxOrNull()?.toString(),
            ),
            etaStart = ExtractionField(
                future.minOrNull(),
                if (future.isNotEmpty()) 0.6f else 0f,
                ExtractionSource.OCR, future.minOrNull()?.toString(),
            ),
            etaEnd = ExtractionField(
                future.maxOrNull(),
                if (future.size > 1) 0.6f else 0f,
                ExtractionSource.OCR, future.maxOrNull()?.toString(),
            ),
            itemNames = emptyList(),
            rawOcrText = text.take(2000),
            warnings = warnings,
        )
        val filled = listOf(
            draft.title.value != null,
            draft.platform.value != null,
            draft.totalMinor.value != null,
            draft.trackingNumber.value != null,
            draft.shipmentStatus.value != null,
        ).count { it }
        return SmartImportResult(
            detectedPlatform = platform,
            draft = draft,
            warnings = warnings,
            overallConfidence = filled / 5f,
            usedGroq = false,
        )
    }
}
