package com.ordra.core.ocr

/** On-device OCR boundary — ML Kit implementation lives in data layer. */
interface OcrEngine {
    /** Returns recognized text, or null when nothing readable was found. */
    suspend fun recognize(imageUri: String): String?
}
