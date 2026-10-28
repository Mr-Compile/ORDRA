package com.ordra.data.groq

import com.ordra.BuildConfig
import com.ordra.domain.repository.SettingsRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

sealed interface GroqResult {
    data class Success(val extraction: GroqExtractionDto) : GroqResult
    data object Unavailable : GroqResult
}

/**
 * Optional online interpretation of REDACTED OCR text. Never authoritative —
 * output is candidate fields only; the user always reviews before saving.
 */
@Singleton
class GroqExtractor @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    private val json = Json { ignoreUnknownKeys = true }

    private val api: GroqApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GroqApi::class.java)
    }

    /** User-entered key (Settings/onboarding) takes precedence; BuildConfig key is the fallback. */
    private suspend fun resolveKey(): String =
        settingsRepository.settings.first().groqApiKey.ifBlank { BuildConfig.GROQ_API_KEY }

    fun isConfigured(userKey: String): Boolean =
        userKey.isNotBlank() || BuildConfig.GROQ_API_KEY.isNotBlank()

    private val SYSTEM_PROMPT = """
You extract order details from OCR text of a marketplace order screenshot.
Return ONLY a JSON object with these fields (null when not found):
{"title","platform","sellerName","totalAmountMinor","shippingMinor","discountMinor",
"paymentMethod","courier","trackingNumber","shipmentStatus","orderedDate","etaStart","etaEnd","itemNames"}.
Rules: platform in [SHOPEE,TIKTOK_SHOP,LAZADA,TEMU,AMAZON,FACEBOOK,INSTAGRAM,OTHER];
paymentMethod in [COD,EWALLET,CARD,BANK_TRANSFER,CASH,OTHER,UNKNOWN];
courier in [SPX_EXPRESS,JNT_EXPRESS,NINJA_VAN,FLASH_EXPRESS,LBC,JRS_EXPRESS,GOGO_XPRESS,ENTREGO,TWO_GO,DHL,OTHER,UNKNOWN];
shipmentStatus in [PREPARING,PICKED_UP,IN_TRANSIT,ARRIVED_AT_HUB,OUT_FOR_DELIVERY,DELIVERED,DELIVERY_FAILED,RETURNING,RETURNED,UNKNOWN];
amounts are integers in minor units (centavos); dates ISO yyyy-MM-dd; itemNames is a string array.
Never invent values. If unsure, return null.
""".trimIndent()

    suspend fun extract(redactedOcrText: String): GroqResult {
        val key = resolveKey()
        if (key.isBlank()) return GroqResult.Unavailable
        return runCatching {
            val response = api.chat(
                authorization = "Bearer $key",
                request = GroqChatRequest(
                    messages = listOf(
                        GroqMessage("system", SYSTEM_PROMPT),
                        GroqMessage("user", redactedOcrText),
                    ),
                ),
            )
            val content = response.choices.firstOrNull()?.message?.content
                ?: return@runCatching GroqResult.Unavailable
            val dto = runCatching { json.decodeFromString<GroqExtractionDto>(content) }
                .getOrNull() ?: return@runCatching GroqResult.Unavailable
            GroqResult.Success(dto)
        }.getOrDefault(GroqResult.Unavailable)
    }
}

/** Removes emails, phones, URLs and long digit runs before any transmission. */
object OcrRedactor {
    private val patterns = listOf(
        Regex("[\\w.+-]+@[\\w-]+\\.[\\w.]+"),
        Regex("(\\+?\\d[\\d\\s\\-()]{7,}\\d)"),
        Regex("https?://\\S+"),
    )

    fun redact(text: String): String =
        patterns.fold(text) { acc, re -> re.replace(acc, "[redacted]") }
}
