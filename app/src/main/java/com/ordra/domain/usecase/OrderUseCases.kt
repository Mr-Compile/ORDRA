package com.ordra.domain.usecase

import com.ordra.core.common.AppError
import com.ordra.core.common.AppResult
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.repository.OrderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

private fun Order.validationError(): AppError? = when {
    items.any { it.name.isBlank() } ->
        AppError.ValidationError("items", "Item name is required")
    items.any { it.quantity < 1 } ->
        AppError.ValidationError("items", "Quantity must be at least 1")
    estimatedDeliveryEnd != null && estimatedDeliveryStart != null &&
        estimatedDeliveryEnd.isBefore(estimatedDeliveryStart) ->
        AppError.ValidationError("estimatedDeliveryEnd", "Delivery end cannot be before start")
    totalAmountMinor != null && totalAmountMinor < 0 ->
        AppError.ValidationError("totalAmountMinor", "Total cannot be negative")
    currencyCode.length != 3 ->
        AppError.ValidationError("currencyCode", "Currency must be an ISO 4217 code")
    else -> null
}

private fun Order.withEffectiveTitle(): Order =
    if (title.isNotBlank()) this
    else copy(title = items.firstOrNull()?.name ?: "Untitled order")

private suspend fun <T> guard(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Failure(AppError.DatabaseFailure(e))
    }

class CreateOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(order: Order): AppResult<String> {
        order.validationError()?.let { return AppResult.Failure(it) }
        val effective = order.withEffectiveTitle()
        return guard { repository.createOrder(effective); effective.id }
    }
}

class UpdateOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(order: Order): AppResult<Unit> {
        order.validationError()?.let { return AppResult.Failure(it) }
        if (repository.getOrder(order.id) == null) {
            return AppResult.Failure(AppError.NotFound(order.id))
        }
        return guard { repository.updateOrder(order.withEffectiveTitle()) }
    }
}

class GetOrderDetailsUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String): AppResult<Order> =
        repository.getOrder(orderId)
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.NotFound(orderId))
}

class ObserveOrdersUseCase @Inject constructor(private val repository: OrderRepository) {
    operator fun invoke(includeArchived: Boolean = false): Flow<List<Order>> =
        if (includeArchived) repository.observeArchivedOrders() else repository.observeActiveOrders()
}

class ObserveOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    operator fun invoke(orderId: String): Flow<Order?> = repository.observeOrder(orderId)
}

class ArchiveOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String): AppResult<Unit> =
        guard { repository.archiveOrder(orderId) }
}

class RestoreOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String): AppResult<Unit> =
        guard { repository.restoreOrder(orderId) }
}

class DeleteOrderUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String): AppResult<Unit> =
        guard { repository.deleteOrderPermanently(orderId) }
}

class UpdateOrderStatusUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String, status: OrderStatus): AppResult<Unit> =
        guard { repository.updateStatus(orderId, status) }
}
