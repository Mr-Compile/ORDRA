package com.ordra.feature.detail

import com.ordra.domain.model.Courier
import com.ordra.domain.model.Shipment
import java.net.URLEncoder

/**
 * "Track Externally" links — official courier tracking pages only.
 * Never scraped; the user reads the page and updates Ordra manually.
 * Returns a web-search fallback for OTHER/UNKNOWN couriers when a tracking
 * number exists. Null when nothing useful can be opened.
 */
object CourierLinks {

    private val officialPages = mapOf(
        Courier.SPX_EXPRESS to "https://spx.ph/track",
        Courier.JNT_EXPRESS to "https://www.jtexpress.ph",
        Courier.NINJA_VAN to "https://www.ninjavan.co/en-ph/tracking",
        Courier.FLASH_EXPRESS to "https://www.flashexpress.ph",
        Courier.LBC to "https://www.lbcexpress.com/track/",
        Courier.JRS_EXPRESS to "https://www.jrs-express.com",
        Courier.GOGO_XPRESS to "https://gogoexpress.ph",
        Courier.ENTREGO to "https://entrego.com.ph",
        Courier.TWO_GO to "https://www.2go.com.ph",
        Courier.DHL to "https://www.dhl.com/ph-en/home/tracking.html",
    )

    fun trackingUrl(shipment: Shipment): String? {
        officialPages[shipment.courier]?.let { return it }
        val number = shipment.trackingNumber?.takeIf { it.isNotBlank() } ?: return null
        val courierName = shipment.customCourierName ?: "parcel"
        val query = URLEncoder.encode("$courierName tracking $number", Charsets.UTF_8.name())
        return "https://www.google.com/search?q=$query"
    }
}
