package com.ordra.core.ui.components

import com.ordra.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderSwipeTest {

    @Test
    fun `active order swiped start-to-end marks delivered`() {
        assertEquals(
            OrderSwipeAction.MarkDelivered,
            resolveSwipeAction(
                isArchived = false,
                status = OrderStatus.SHIPPED,
                direction = OrderSwipeDirection.StartToEnd,
            ),
        )
    }

    @Test
    fun `active order swiped end-to-start archives`() {
        OrderStatus.entries.forEach { status ->
            assertEquals(
                OrderSwipeAction.Archive,
                resolveSwipeAction(
                    isArchived = false,
                    status = status,
                    direction = OrderSwipeDirection.EndToStart,
                ),
            )
        }
    }

    @Test
    fun `terminal statuses cannot be marked delivered`() {
        listOf(
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED,
            OrderStatus.RETURNED,
            OrderStatus.REFUNDED,
        ).forEach { status ->
            assertNull(
                resolveSwipeAction(
                    isArchived = false,
                    status = status,
                    direction = OrderSwipeDirection.StartToEnd,
                ),
            )
        }
    }

    @Test
    fun `non-terminal statuses can be marked delivered`() {
        listOf(
            OrderStatus.DRAFT,
            OrderStatus.ORDERED,
            OrderStatus.PROCESSING,
            OrderStatus.PACKED,
            OrderStatus.SHIPPED,
        ).forEach { status ->
            assertEquals(
                OrderSwipeAction.MarkDelivered,
                resolveSwipeAction(
                    isArchived = false,
                    status = status,
                    direction = OrderSwipeDirection.StartToEnd,
                ),
            )
        }
    }

    @Test
    fun `archived order restores on start-to-end only`() {
        assertEquals(
            OrderSwipeAction.Restore,
            resolveSwipeAction(
                isArchived = true,
                status = OrderStatus.SHIPPED,
                direction = OrderSwipeDirection.StartToEnd,
            ),
        )
        assertNull(
            resolveSwipeAction(
                isArchived = true,
                status = OrderStatus.SHIPPED,
                direction = OrderSwipeDirection.EndToStart,
            ),
        )
    }
}
