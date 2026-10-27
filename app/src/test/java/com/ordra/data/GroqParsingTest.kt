package com.ordra.data

import com.ordra.data.groq.GroqExtractionDto
import com.ordra.data.groq.OcrRedactor
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroqParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `valid response parses all fields`() {
        val dto = json.decodeFromString<GroqExtractionDto>(
            """{"title":"Earbuds","platform":"SHOPEE","totalAmountMinor":64900,
               "courier":"SPX_EXPRESS","trackingNumber":"SPXPH1","itemNames":["R50i"]}""",
        )
        assertEquals("Earbuds", dto.title)
        assertEquals(64900L, dto.totalAmountMinor)
        assertEquals(listOf("R50i"), dto.itemNames)
    }

    @Test
    fun `unknown keys are ignored and missing fields are null`() {
        val dto = json.decodeFromString<GroqExtractionDto>(
            """{"title":"x","madeUpField":42}""",
        )
        assertEquals("x", dto.title)
        assertNull(dto.trackingNumber)
    }

    @Test
    fun `redactor strips email phone and urls`() {
        val out = OcrRedactor.redact(
            "Ship to juan@mail.com call 0917-123-4567 see https://x.ph/order",
        )
        assertTrue("juan@mail.com" !in out)
        assertTrue("0917-123-4567" !in out)
        assertTrue("https://x.ph/order" !in out)
    }
}
