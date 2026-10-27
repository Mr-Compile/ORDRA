package com.ordra.feature.orderedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.app.navigation.OrdraRoutes
import com.ordra.core.common.AppResult
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.Shipment
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.usecase.ArchiveOrderUseCase
import com.ordra.domain.usecase.CreateOrderUseCase
import com.ordra.domain.usecase.DeleteOrderUseCase
import com.ordra.domain.usecase.GetOrderDetailsUseCase
import com.ordra.domain.usecase.RestoreOrderUseCase
import com.ordra.domain.usecase.UpdateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorStep { FORM, REVIEW }

sealed interface AddEditEffect {
    data object Saved : AddEditEffect
    data object Deleted : AddEditEffect
    data object ShowSaveError : AddEditEffect
}

data class AddEditUiState(
    val form: OrderFormState = OrderFormState(),
    val step: EditorStep = EditorStep.FORM,
    val loaded: Boolean = false,
    val isSaving: Boolean = false,
    val attemptedSave: Boolean = false,
    val saveError: Boolean = false,
    val isArchived: Boolean = false,
) {
    val fieldErrors: Map<String, String> get() =
        if (attemptedSave) form.fieldErrors() else emptyMap()
}

@HiltViewModel
class AddEditOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrderDetails: GetOrderDetailsUseCase,
    private val createOrder: CreateOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    private val archiveOrder: ArchiveOrderUseCase,
    private val restoreOrder: RestoreOrderUseCase,
    private val deleteOrder: DeleteOrderUseCase,
    private val shipmentRepository: ShipmentRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val orderId: String? = savedStateHandle[OrdraRoutes.ARG_ORDER_ID]
    private var existing: Order? = null
    private var existingShipment: Shipment? = null

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AddEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val defaultCurrency = settingsRepository.settings.first().currencyCode
            if (orderId != null) {
                when (val result = getOrderDetails(orderId)) {
                    is AppResult.Success -> {
                        existing = result.value
                        existingShipment = shipmentRepository.getPrimaryShipment(orderId)
                        _uiState.update {
                            it.copy(
                                form = OrderFormState.fromOrder(result.value, existingShipment),
                                loaded = true,
                                isArchived = result.value.isArchived,
                            )
                        }
                    }
                    is AppResult.Failure -> _uiState.update { it.copy(loaded = true) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        form = it.form.copy(currencyCode = defaultCurrency),
                        loaded = true,
                    )
                }
            }
        }
    }

    fun updateForm(transform: (OrderFormState) -> OrderFormState) {
        _uiState.update { state ->
            state.copy(form = transform(state.form).copy(isDirty = true))
        }
    }

    fun goToReview() {
        _uiState.update { it.copy(attemptedSave = true) }
        if (_uiState.value.form.isValid()) {
            _uiState.update { it.copy(step = EditorStep.REVIEW) }
        }
    }

    fun backToForm() {
        _uiState.update { it.copy(step = EditorStep.FORM) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(attemptedSave = true) }
        if (!state.form.isValid()) {
            _uiState.update { it.copy(step = EditorStep.FORM) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = false) }
            val order = state.form.toOrder(existing, Instant.now())
            val result = if (existing == null) createOrder(order) else updateOrder(order)
            when (result) {
                is AppResult.Success -> {
                    // Courier/tracking persist on the primary Shipment, not the Order.
                    state.form.toShipment(order.id, existingShipment, Instant.now())
                        ?.let {
                            existingShipment = it
                            shipmentRepository.saveShipment(it)
                        }
                    _uiState.update { it.copy(isSaving = false) }
                    _effects.send(AddEditEffect.Saved)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isSaving = false, saveError = true) }
                    _effects.send(AddEditEffect.ShowSaveError)
                }
            }
        }
    }

    fun archive() = mutateArchived(archive = true)
    fun restore() = mutateArchived(archive = false)

    private fun mutateArchived(archive: Boolean) {
        val id = orderId ?: return
        viewModelScope.launch {
            val result = if (archive) archiveOrder(id) else restoreOrder(id)
            if (result is AppResult.Success) {
                _uiState.update { it.copy(isArchived = archive) }
            }
        }
    }

    fun delete() {
        val id = orderId ?: return
        viewModelScope.launch {
            when (deleteOrder(id)) {
                is AppResult.Success -> _effects.send(AddEditEffect.Deleted)
                is AppResult.Failure -> _effects.send(AddEditEffect.ShowSaveError)
            }
        }
    }
}
