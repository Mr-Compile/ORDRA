package com.ordra.app.navigation

/**
 * Route contracts for the app. Destinations are added to OrdraNavHost as their
 * feature phases arrive — the route patterns are stable from Phase 1A.
 */
object OrdraRoutes {
    const val HOME = "home"
    const val ORDER_DETAIL = "order/{orderId}"
    const val ORDER_EDITOR = "order/edit?orderId={orderId}"
    const val SETTINGS = "settings"

    const val ARG_ORDER_ID = "orderId"

    fun orderDetail(orderId: String) = "order/$orderId"
    fun orderEditor(orderId: String? = null) =
        if (orderId == null) "order/edit" else "order/edit?orderId=$orderId"
}
