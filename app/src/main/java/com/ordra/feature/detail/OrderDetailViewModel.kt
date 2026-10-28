package com.ordra.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.app.navigation.OrdraRoutes
import com.ordra.core.common.AppResult
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.usecase.ArchiveOrderUseCase
import com.ordra.domain.usecase.DeleteOrderUseCase
import com.ordra.domain.usecase.RestoreOrderUseCase
import com.ordra.domain.usecase.UpdateOrderStatusUseCase
import com.ordra.domain.repository.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: Order? = null,
    val shipment: Shipment? = null,
    val returns: List<com.ordra.domain.model.PurchaseReturn> = emptyList(),
    val refunds: List<com.ordra.domain.model.Refund> = emptyList(),
    val warranties: List<com.ordra.domain.model.Warranty> = emptyList(),
    val receipts: List<com.ordra.domain.model.Receipt> = emptyList(),
    val loaded: Boolean = false,
)

sealed interface OrderDetailEffect {
    data object Deleted : OrderDetailEffect
    data object Delivered : OrderDetailEffect
    data class OpenUrl(val url: String) : OrderDetailEffect
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: com.ordra.domain.repository.OrderRepository,
    private val shipmentRepository: ShipmentRepository,
    private val updateOrderStatus: UpdateOrderStatusUseCase,
    private val archiveOrder: ArchiveOrderUseCase,
    private val restoreOrder: RestoreOrderUseCase,
    private val deleteOrder: DeleteOrderUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val postPurchaseRepository: com.ordra.domain.repository.PostPurchaseRepository,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle[OrdraRoutes.ARG_ORDER_ID])

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OrderDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                orderRepository.observeOrder(orderId),
                shipmentRepository.observePrimaryShipment(orderId),
            ) { order, shipment ->
                _uiState.value.copy(order = order, shipment = shipment, loaded = true)
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            combine(
                postPurchaseRepository.observeReturns(orderId),
                postPurchaseRepository.observeRefunds(orderId),
                postPurchaseRepository.observeWarranties(orderId),
                postPurchaseRepository.observeReceipts(orderId),
            ) { returns, refunds, warranties, receipts ->
                _uiState.value.copy(
                    returns = returns, refunds = refunds,
                    warranties = warranties, receipts = receipts,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setRating(rating: Int?) = viewModelScope.launch {
        orderRepository.setRatingFavorite(
            orderId, rating, _uiState.value.order?.isFavorite ?: false,
        )
    }

    fun toggleFavorite() = viewModelScope.launch {
        orderRepository.setRatingFavorite(
            orderId, _uiState.value.order?.rating,
            !(_uiState.value.order?.isFavorite ?: false),
        )
    }

    fun addReturn(reason: String?) = viewModelScope.launch {
        val now = java.time.Instant.now()
        postPurchaseRepository.saveReturn(
            com.ordra.domain.model.PurchaseReturn(
                id = java.util.UUID.randomUUID().toString(),
                orderId = orderId, reason = reason,
                status = com.ordra.domain.model.ReturnStatus.REQUESTED,
                requestedAt = now, resolvedAt = null, notes = null,
            ),
        )
    }

    fun addRefund(amountMinor: Long?, reason: String?) = viewModelScope.launch {
        postPurchaseRepository.saveRefund(
            com.ordra.domain.model.Refund(
                id = java.util.UUID.randomUUID().toString(),
                orderId = orderId, amountMinor = amountMinor,
                status = com.ordra.domain.model.RefundStatus.PENDING,
                reason = reason,
                requestedAt = java.time.Instant.now(), processedAt = null,
            ),
        )
    }

    fun addWarranty(provider: String?, start: java.time.LocalDate, end: java.time.LocalDate) =
        viewModelScope.launch {
            if (end.isBefore(start)) return@launch
            postPurchaseRepository.saveWarranty(
                com.ordra.domain.model.Warranty(
                    id = java.util.UUID.randomUUID().toString(),
                    orderId = orderId, provider = provider,
                    startDate = start, endDate = end, notes = null,
                ),
            )
            orderRepository.getOrder(orderId)
                ?.let { reminderScheduler.syncOrderReminders(it) }
        }

    fun addReceipt(imageUri: String) = viewModelScope.launch {
        postPurchaseRepository.saveReceipt(
            com.ordra.domain.model.Receipt(
                id = java.util.UUID.randomUUID().toString(),
                orderId = orderId, imageUri = imageUri, label = null,
                addedAt = java.time.Instant.now(),
            ),
        )
    }

    fun setOrderStatus(status: OrderStatus) {
        viewModelScope.launch {
            val result = updateOrderStatus(orderId, status)
            if (status == OrderStatus.DELIVERED && result is AppResult.Success) {
                _uiState.value.shipment?.let {
                    shipmentRepository.updateShipmentStatus(it.id, ShipmentStatus.DELIVERED)
                }
                _effects.send(OrderDetailEffect.Delivered)
            }
        }
    }

    fun setShipmentStatus(status: ShipmentStatus) {
        val shipmentId = _uiState.value.shipment?.id ?: return
        viewModelScope.launch {
            shipmentRepository.updateShipmentStatus(shipmentId, status)
        }
    }

    fun archive() = viewModelScope.launch {
        reminderScheduler.cancelOrderReminders(orderId)
        archiveOrder(orderId)
    }
    fun restore() = viewModelScope.launch { restoreOrder(orderId) }

    fun delete() {
        viewModelScope.launch {
            reminderScheduler.cancelOrderReminders(orderId)
            if (deleteOrder(orderId) is AppResult.Success) {
                _effects.send(OrderDetailEffect.Deleted)
            }
        }
    }

    fun trackExternally() {
        val url = _uiState.value.shipment?.let(CourierLinks::trackingUrl) ?: return
        viewModelScope.launch { _effects.send(OrderDetailEffect.OpenUrl(url)) }
    }
}
