package com.ordra.app.navigation

/**
 * Route contracts for the app. Destinations are added to OrdraNavHost as their
 * feature phases arrive — the route patterns are stable from Phase 1A.
 */
object OrdraRoutes {
    const val HOME = "home"
    const val ORDER_DETAIL = "order/{orderId}"
    const val ORDER_EDITOR = "order/edit?orderId={orderId}"
    const val SHIPMENT_TIMELINE = "shipment/{shipmentId}/timeline"
    const val ORDERS = "orders"
    const val COD = "cod"
    const val CALENDAR = "calendar"
    const val SMART_IMPORT = "import"
    const val DETECTIONS = "detections"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    const val ARG_ORDER_ID = "orderId"
    const val ARG_SHIPMENT_ID = "shipmentId"

    fun orderDetail(orderId: String) = "order/$orderId"
    fun orderEditor(orderId: String? = null) =
        if (orderId == null) "order/edit" else "order/edit?orderId=$orderId"
    fun shipmentTimeline(shipmentId: String) = "shipment/$shipmentId/timeline"
}
