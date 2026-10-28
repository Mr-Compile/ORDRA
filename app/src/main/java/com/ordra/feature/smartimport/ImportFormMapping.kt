package com.ordra.feature.smartimport

import com.ordra.core.common.MoneyFormat
import com.ordra.domain.model.SmartImportResult
import com.ordra.feature.orderedit.OrderFormState

/** Maps a parsed extraction draft into the editable order form (shared by
 *  SmartImportViewModel and the order editor's Import tab). */
internal fun orderFormStateFrom(result: SmartImportResult): OrderFormState {
    val d = result.draft
    fun money(v: Long?) = v?.let { MoneyFormat.formatMinor(it, "PHP") }.orEmpty()
    return OrderFormState(
        title = d.title.value.orEmpty(),
        platform = d.platform.value,
        customPlatformName = d.customPlatformName.value.orEmpty(),
        sellerName = d.sellerName.value.orEmpty(),
        total = money(d.totalMinor.value),
        shipping = money(d.shippingMinor.value),
        discount = money(d.discountMinor.value),
        paymentMethod = d.paymentMethod.value ?: com.ordra.domain.model.PaymentMethod.UNKNOWN,
        courier = d.courier.value ?: com.ordra.domain.model.Courier.UNKNOWN,
        customCourierName = d.customCourierName.value.orEmpty(),
        trackingNumber = d.trackingNumber.value.orEmpty(),
        orderedDate = d.orderedDate.value,
        etaStart = d.etaStart.value,
        etaEnd = d.etaEnd.value,
    )
}
