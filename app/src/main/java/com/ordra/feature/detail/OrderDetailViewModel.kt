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
    val loaded: Boolean = false,
)

sealed interface OrderDetailEffect {
    data object Deleted : OrderDetailEffect
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
                OrderDetailUiState(order = order, shipment = shipment, loaded = true)
            }.collect { _uiState.value = it }
        }
    }

    fun setOrderStatus(status: OrderStatus) {
        viewModelScope.launch {
            updateOrderStatus(orderId, status)
            if (status == OrderStatus.DELIVERED) {
                _uiState.value.shipment?.let {
                    shipmentRepository.updateShipmentStatus(it.id, ShipmentStatus.DELIVERED)
                }
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
