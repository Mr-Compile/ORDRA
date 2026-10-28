package com.ordra.core.ui.components

import com.ordra.domain.model.OrderStatus

enum class OrderSwipeDirection {
    StartToEnd,
    EndToStart,
}

enum class OrderSwipeAction {
    Archive,
    MarkDelivered,
    Restore,
}

private val SWIPE_TERMINAL_STATUSES = setOf(
    OrderStatus.DELIVERED,
    OrderStatus.CANCELLED,
    OrderStatus.RETURNED,
    OrderStatus.REFUNDED,
)

/**
 * Maps a card swipe to the allowed list action.
 * Active orders: start→end marks delivered (when the status allows it),
 * end→start archives. Archived orders only allow start→end restore —
 * there is deliberately no destructive swipe action.
 */
fun resolveSwipeAction(
    isArchived: Boolean,
    status: OrderStatus,
    direction: OrderSwipeDirection,
): OrderSwipeAction? = when {
    isArchived ->
        if (direction == OrderSwipeDirection.StartToEnd) OrderSwipeAction.Restore else null
    direction == OrderSwipeDirection.EndToStart -> OrderSwipeAction.Archive
    status in SWIPE_TERMINAL_STATUSES -> null
    else -> OrderSwipeAction.MarkDelivered
}
