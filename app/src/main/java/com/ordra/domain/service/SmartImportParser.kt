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
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
    )
    private val DATE_TEXT_RE = Regex(
        "\\b(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{4}|\\d{4}-\\d{2}-\\d{2}|" +
            "\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}|[A-Za-z]+\\s+\\d{1,2},?\\s+\\d{4})\\b",
    )
    private val TIME_RE = Regex("\\d{1,2}:\\d{2}")
    private val PHONE_RE = Regex("(\\+?\\d{2,4}[\\s\\-()]*)?\\d[\\d\\s\\-]{7,}\\d")
    private val AMOUNT_ONLY_RE = Regex("^[₱PHP$\\-€£\\s\\d.,]+$")
    private val DOMAIN_RE = Regex("\\.(ph|com|net|shop|store|co)\\b", RegexOption.IGNORE_CASE)
    private val QTY_MARKER_RE = Regex(
        "(?:^|\\s)[x×✕]\\s*(\\d{1,3})\\b|\\bqty[:\\s]*(\\d{1,3})\\b",
        RegexOption.IGNORE_CASE,
    )
    /** Address vocabulary — recipient/address lines never become a title. */
    private val ADDRESS_RE = Regex(
        "\\b(street|st\\.|avenue|ave\\.|road|rd\\.|blvd|boulevard|drive|dr\\.|lane|purok|" +
            "barangay|brgy\\.?|poblacion|sitio|subdivision|subd\\.?|village|city|province|" +
            "block|blk\\.?|lot|unit|bldg\\.?|building|floor|flr\\.?|phase|zone|zip|postal)\\b",
        RegexOption.IGNORE_CASE,
    )
    private val POSTCODE_END_RE = Regex(",\\s*\\d{4,6}\\s*$")
    /** Section headers — generic money labels must skip these occurrences. */
    private val HEADER_WORD_RE = Regex(
        "\\b(information|history|details|status|method|options|updates|summary)\\b",
        RegexOption.IGNORE_CASE,
    )

    /** Lines that are page chrome / sections / actions — never a product title. */
    private val TITLE_BLOCK = listOf(
        "order details", "order id", "order #", "shipping information", "delivery information",
        "estimated delivery", "guaranteed", "support center", "request for refund",
        "request refund", "track order", "buy again", "view more", "view all",
        "rate product", "receipt", "invoice", "cash on delivery", "total", "subtotal",
        "fee", "sold by", "payment", "quantity",
    )
    private val SELLER_BADGE = listOf(
        "preferred", "mall", "star seller", "official store", "official", "flagship",
    )
    private val SOLD_BY_RE = Regex("sold\\s+by[:\\s]+(.+)", RegexOption.IGNORE_CASE)

    /** Discount keywords — amounts on these lines count toward the discount total,
     *  but promo/earning lines ("Get a ₱40 voucher if…") are excluded. */
    private val DISCOUNT_RE = Regex("discount|voucher|promo|rebate|coins?\\s+(?:used|redeemed)")
    private val DISCOUNT_EARN_RE = Regex("get a|earn|win\\b|collect|if no|cashback offer|free voucher")

    private val ETA_LABEL_RE = Regex(
        "estimated\\s+delivery(?:\\s+date)?|est\\.?\\s+delivery|delivery\\s+date|" +
            "arrival\\s+date|expected\\s+delivery|deliver\\s+(?:by|within)",
        RegexOption.IGNORE_CASE,
    )
    private val MONTHS = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
    )

    /** "29 Oct - 1 Nov" | "29 Oct – 31" | "Oct 29 - Nov 1, 2026" | "Oct 29 - 31" */
    private val ETA_RANGE_RE = Regex(
        "(\\d{1,2})\\s*([A-Za-z]{3,9})\\.?\\s*[-–—]\\s*(\\d{1,2})(?:\\s*([A-Za-z]{3,9})\\.?)?(?:,?\\s*(\\d{4}))?|" +
            "([A-Za-z]{3,9})\\.?\\s*(\\d{1,2})\\s*[-–—]\\s*(?:([A-Za-z]{3,9})\\.?\\s*)?(\\d{1,2})(?:,?\\s*(\\d{4}))?",
        RegexOption.IGNORE_CASE,
    )

    fun parse(rawText: String): SmartImportResult {
        val text = rawText.trim()
        val lower = text.lowercase()
        val warnings = mutableListOf<String>()
        if (text.isEmpty()) warnings += "no_text"
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()

        val platform = PLATFORM_HINTS
            .firstOrNull { (_, keys) -> keys.any { lower.contains(it) } }?.first
        val courier = COURIER_HINTS
            .firstOrNull { (_, keys) -> keys.any { lower.contains(it) } }?.first

        fun amountNear(vararg labels: String): Pair<Long?, String?> {
            for (label in labels) {
                val labelRe = Regex(
                    "\\b" + Regex.escape(label) + "\\b",
                    RegexOption.IGNORE_CASE,
                )
                for (lm in labelRe.findAll(lower)) {
                    val lineEnd = lower.indexOf('\n', lm.range.first)
                        .let { if (it < 0) lower.length else it }
                    val restOfLine = lower.substring(lm.range.last + 1, lineEnd)
                    // "Shipping Information" / "Total summary" are headers, not values.
                    if (HEADER_WORD_RE.containsMatchIn(restOfLine)) continue
                    val winStart = lm.range.first
                    val window = text.substring(
                        winStart,
                        (lm.range.last + 60).coerceAtMost(text.length),
                    )
                    for (m in AMOUNT_RE.findAll(window)) {
                        val marked = m.value.trimStart().let {
                            it.startsWith("₱") || it.startsWith("PHP", ignoreCase = true) ||
                                it.startsWith("$") || it.startsWith("P")
                        }
                        if (!marked) {
                            // Reject digits embedded in tracking ids / model codes
                            // ("PH2613…" -> 261, "ESP32" -> 32).
                            val before = text.getOrNull(winStart + m.range.first - 1)
                            val after = text.getOrNull(winStart + m.range.last + 1)
                            if (before?.isLetterOrDigit() == true ||
                                after?.isLetterOrDigit() == true
                            ) {
                                continue
                            }
                        }
                        val minor = MoneyFormat.parseToMinor(m.value)
                        if (minor != null) return minor to m.value
                    }
                }
            }
            return null to null
        }
        val (total, totalEvidence) = amountNear(
            "grand total", "order total", "total", "amount due",
        )
        val (shipping, shipEvidence) = amountNear(
            "shipping fee", "shipping", "delivery fee",
        )
        val (merchSubtotal, merchEvidence) = amountNear(
            "merchandise subtotal", "items subtotal", "item subtotal",
        )

        /* Discount = SUM of amounts on discount/voucher/promo lines — but promo
         * teasers ("Get a ₱40 voucher if no delivery…") never count. */
        var discountSum = 0L
        var discEvidence: String? = null
        DISCOUNT_RE.findAll(lower).forEach { m ->
            val lineStart = lower.lastIndexOf('\n', m.range.first).let { if (it < 0) 0 else it + 1 }
            val lineEnd = lower.indexOf('\n', m.range.first).let { if (it < 0) lower.length else it }
            val line = lower.substring(lineStart, lineEnd)
            if (!DISCOUNT_EARN_RE.containsMatchIn(line)) {
                val window = text.substring(
                    m.range.first,
                    (m.range.first + 40).coerceAtMost(text.length),
                )
                val amt = AMOUNT_RE.find(window)?.let { MoneyFormat.parseToMinor(it.value) }
                if (amt != null) {
                    discountSum += amt
                    if (discEvidence == null) discEvidence = line.trim()
                }
            }
        }
        val discount = discountSum.takeIf { it > 0 }

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

        /* ETA label + date range ("Estimated delivery date: 29 Oct - 1 Nov").
         * Year comes from an explicit suffix, else the newest year in the text,
         * else rollover from today. */
        val textYear = dates.map { it.year }.maxOrNull() ?: today.year
        var etaStart: LocalDate? = null
        var etaEnd: LocalDate? = null
        var etaEvidence: String? = null
        var etaFromLabel = false
        ETA_LABEL_RE.find(text)?.let { label ->
            val window = text.substring(
                label.range.last,
                (label.range.last + 60).coerceAtMost(text.length),
            )
            ETA_RANGE_RE.find(window)?.let { m ->
                val g = m.groups
                fun monthOf(s: String?): Int? =
                    s?.lowercase()?.take(3)?.let { MONTHS[it] }
                val d1: Int; val m1: Int?; val d2: Int; val m2: Int?; val yr: Int?
                if (g[1] != null) {
                    // "29 Oct - 1 Nov[, 2026]" or "29 Oct - 31"
                    d1 = g[1]!!.value.toInt(); m1 = monthOf(g[2]?.value)
                    d2 = g[3]!!.value.toInt(); m2 = monthOf(g[4]?.value) ?: m1
                    yr = g[5]?.value?.toInt()
                } else {
                    // "Oct 29 - Nov 1[, 2026]" or "Oct 29 - 31"
                    m1 = monthOf(g[6]?.value); d1 = g[7]!!.value.toInt()
                    m2 = monthOf(g[8]?.value) ?: m1
                    d2 = g[9]!!.value.toInt(); yr = g[10]?.value?.toInt()
                }
                if (m1 != null && m2 != null) {
                    var year = yr ?: textYear
                    if (yr == null && m1 < today.monthValue && textYear <= today.year) year += 1
                    val s = runCatching { LocalDate.of(year, m1, d1) }.getOrNull()
                    val e = runCatching {
                        val ey = if (m2 < m1) year + 1 else year
                        LocalDate.of(ey, m2, d2)
                    }.getOrNull()
                    if (s != null && e != null && !e.isBefore(s)) {
                        etaStart = s; etaEnd = e
                        etaEvidence = m.value.trim(); etaFromLabel = true
                    }
                }
            }
        }
        if (etaStart == null) {
            etaStart = future.minOrNull()
            etaEnd = future.maxOrNull().takeIf { future.size > 1 }
            etaEvidence = etaStart?.toString()
        }

        /* Item name via qty marker ("… x1") — walk up ≤3 lines for a title
         * candidate; fallback = first candidate line overall. */
        fun isTitleCandidate(l: String): Boolean {
            val ll = l.lowercase()
            if (l.length !in 4..80 || !l.any { it.isLetter() }) return false
            if (TITLE_BLOCK.any { ll.contains(it) }) return false
            if (COURIER_HINTS.any { (_, k) -> k.any { ll.contains(it) } }) return false
            if (PLATFORM_HINTS.any { (_, k) -> k.any { ll.contains(it) } }) return false
            if (SELLER_BADGE.any { ll.contains(it) }) return false
            if (DOMAIN_RE.containsMatchIn(l)) return false
            if (PHONE_RE.containsMatchIn(l)) return false
            if (TIME_RE.containsMatchIn(l)) return false
            if (AMOUNT_ONLY_RE.matches(l)) return false
            if (TRACKING_RE.find(l) != null) return false
            if (StatusNormalizer.normalize(l) != ShipmentStatus.UNKNOWN) return false
            if (QTY_MARKER_RE.containsMatchIn(l)) return false
            if (ADDRESS_RE.containsMatchIn(l)) return false
            if (POSTCODE_END_RE.containsMatchIn(l)) return false
            return true
        }
        var titleCandidate: String? = null
        var titleConf = 0f
        var itemQty: Int? = null
        val qtyIdx = lines.indexOfFirst { QTY_MARKER_RE.containsMatchIn(it) }
        if (qtyIdx >= 0) {
            itemQty = QTY_MARKER_RE.find(lines[qtyIdx])?.let { m ->
                m.groupValues.drop(1).firstOrNull { it.isNotEmpty() }?.toIntOrNull()
            }
            for (up in 1..3) {
                val l = lines.getOrNull(qtyIdx - up) ?: break
                if (isTitleCandidate(l)) {
                    titleCandidate = l; titleConf = 0.7f; break
                }
            }
        }
        if (titleCandidate == null) {
            // Shopee lists the product right after the seller badge/store line.
            val badgeIdx = lines.indexOfFirst { l ->
                val ll = l.lowercase()
                SELLER_BADGE.any { ll.contains(it) } || DOMAIN_RE.containsMatchIn(l)
            }
            if (badgeIdx >= 0) {
                titleCandidate = (1..4)
                    .asSequence()
                    .mapNotNull { lines.getOrNull(badgeIdx + it) }
                    .firstOrNull { isTitleCandidate(it) }
                if (titleCandidate != null) titleConf = 0.6f
            }
        }
        if (titleCandidate == null) {
            titleCandidate = lines.firstOrNull { isTitleCandidate(it) }
            titleConf = if (titleCandidate != null) 0.45f else 0f
        }

        /* Seller: "Sold by X" else badge line (Preferred/Mall/…) → remainder or next line. */
        fun sellerGuess(): Pair<String?, String?> {
            SOLD_BY_RE.find(text)?.let {
                return it.groupValues[1].trim().trimEnd('>', ' ') to it.value
            }
            lines.forEachIndexed { i, l ->
                val ll = l.lowercase()
                val badge = SELLER_BADGE.firstOrNull { ll.contains(it) } ?: return@forEachIndexed
                val rest = l.substring(ll.indexOf(badge) + badge.length).trim(' ', '>', ':', '-')
                if (rest.length >= 3 && !rest.any { it.isDigit() }) {
                    return rest to l
                }
                val next = lines.getOrNull(i + 1)?.trim(' ', '>') ?: return@forEachIndexed
                if (next.length in 3..60) return next to l
            }
            return null to null
        }
        val (seller, sellerEvidence) = sellerGuess()

        val draft = OrderDraft(
            title = ExtractionField(
                titleCandidate,
                titleConf,
                ExtractionSource.OCR, titleCandidate,
            ),
            platform = ExtractionField(
                platform,
                if (platform != null) 0.8f else 0f,
                ExtractionSource.LOCAL_PARSER, platform?.name,
            ),
            sellerName = ExtractionField(
                seller,
                if (seller != null) 0.6f else 0f,
                ExtractionSource.OCR, sellerEvidence,
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
            merchandiseSubtotalMinor = ExtractionField(
                merchSubtotal,
                if (merchSubtotal != null) 0.7f else 0f,
                ExtractionSource.OCR, merchEvidence,
            ),
            itemQuantity = ExtractionField(
                itemQty,
                if (itemQty != null) 0.7f else 0f,
                ExtractionSource.OCR, itemQty?.toString(),
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
                etaStart,
                if (etaFromLabel) 0.7f else if (etaStart != null) 0.6f else 0f,
                ExtractionSource.OCR, etaEvidence,
            ),
            etaEnd = ExtractionField(
                etaEnd,
                if (etaStart != null && etaEnd != null) 0.6f else 0f,
                ExtractionSource.OCR, etaEnd?.toString(),
            ),
            itemNames = titleCandidate?.let {
                listOf(ExtractionField(it, titleConf, ExtractionSource.OCR, it))
            } ?: emptyList(),
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
