package com.ordra.feature.orders

import androidx.annotation.StringRes
import com.ordra.R
import com.ordra.core.common.AppResult
import com.ordra.core.ui.components.OrderSwipeAction
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.usecase.ArchiveOrderUseCase
import com.ordra.domain.usecase.RestoreOrderUseCase
import com.ordra.domain.usecase.UpdateOrderStatusUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface OrderActionEvent {
    val messageRes: Int

    data class Success(
        @param:StringRes override val messageRes: Int,
        val delivered: Boolean = false,
    ) : OrderActionEvent

    data class Failure(
        @param:StringRes override val messageRes: Int,
    ) : OrderActionEvent
}

/**
 * Executes list-level order actions (archive / restore / mark delivered)
 * through the domain use cases and remembers the matching reverse
 * operation so the snackbar can offer a real Undo. Unscoped — each
 * ViewModel gets its own instance, keeping undo and events per-screen.
 */
class OrderActionDelegate @Inject constructor(
    private val archiveOrder: ArchiveOrderUseCase,
    private val restoreOrder: RestoreOrderUseCase,
    private val updateOrderStatus: UpdateOrderStatusUseCase,
) {

    private val _events = MutableSharedFlow<OrderActionEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<OrderActionEvent> = _events.asSharedFlow()

    private var undo: (suspend () -> AppResult<Unit>)? = null

    suspend fun perform(order: Order, action: OrderSwipeAction) {
        when (action) {
            OrderSwipeAction.Archive -> run(archiveOrder(order.id)) {
                undo = { restoreOrder(order.id) }
                OrderActionEvent.Success(R.string.swipe_order_archived)
            }
            OrderSwipeAction.Restore -> run(restoreOrder(order.id)) {
                undo = { archiveOrder(order.id) }
                OrderActionEvent.Success(R.string.swipe_order_restored)
            }
            OrderSwipeAction.MarkDelivered -> {
                val previousStatus = order.status
                run(updateOrderStatus(order.id, OrderStatus.DELIVERED)) {
                    // updateStatus clears deliveredAt for non-DELIVERED values,
                    // so restoring the previous status is a clean undo.
                    undo = { updateOrderStatus(order.id, previousStatus) }
                    OrderActionEvent.Success(
                        R.string.swipe_order_delivered,
                        delivered = true,
                    )
                }
            }
        }
    }

    suspend fun undoLast() {
        val op = undo ?: return
        undo = null
        if (op() is AppResult.Failure) {
            _events.emit(OrderActionEvent.Failure(R.string.swipe_action_failed))
        }
    }

    private suspend fun run(
        result: AppResult<Unit>,
        onSuccess: () -> OrderActionEvent,
    ) {
        when (result) {
            is AppResult.Success -> _events.emit(onSuccess())
            is AppResult.Failure ->
                _events.emit(OrderActionEvent.Failure(R.string.swipe_action_failed))
        }
    }
}
