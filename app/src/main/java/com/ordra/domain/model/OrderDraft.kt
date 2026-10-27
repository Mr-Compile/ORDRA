package com.ordra.domain.model

import java.time.LocalDate

enum class ExtractionSource { OCR, LOCAL_PARSER, GROQ, USER }

data class ExtractionField<T>(
    val value: T? = null,
    val confidence: Float = 0f,
    val source: ExtractionSource = ExtractionSource.OCR,
    val evidence: String? = null,
    val needsReview: Boolean = true,
)

data class OrderDraft(
    val title: ExtractionField<String> = ExtractionField(),
    val platform: ExtractionField<Platform> = ExtractionField(),
    val customPlatformName: ExtractionField<String> = ExtractionField(),
    val sellerName: ExtractionField<String> = ExtractionField(),
    val totalMinor: ExtractionField<Long> = ExtractionField(),
    val shippingMinor: ExtractionField<Long> = ExtractionField(),
    val discountMinor: ExtractionField<Long> = ExtractionField(),
    val paymentMethod: ExtractionField<PaymentMethod> = ExtractionField(),
    val courier: ExtractionField<Courier> = ExtractionField(),
    val customCourierName: ExtractionField<String> = ExtractionField(),
    val trackingNumber: ExtractionField<String> = ExtractionField(),
    val shipmentStatus: ExtractionField<ShipmentStatus> = ExtractionField(),
    val orderedDate: ExtractionField<LocalDate> = ExtractionField(),
    val etaStart: ExtractionField<LocalDate> = ExtractionField(),
    val etaEnd: ExtractionField<LocalDate> = ExtractionField(),
    val itemNames: List<ExtractionField<String>> = emptyList(),
    val rawOcrText: String? = null,
    val warnings: List<String> = emptyList(),
)

data class SmartImportResult(
    val detectedPlatform: Platform?,
    val draft: OrderDraft,
    val warnings: List<String>,
    val overallConfidence: Float,
    val usedGroq: Boolean,
)
