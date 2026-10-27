package com.ordra.feature.smartimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.core.common.AppResult
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ocr.OcrEngine
import com.ordra.domain.model.OrderDraft
import com.ordra.domain.model.SmartImportResult
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.service.SmartImportParser
import com.ordra.domain.usecase.CreateOrderUseCase
import com.ordra.feature.orderedit.OrderFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ImportStep { PICK, PROCESSING, REVIEW }

data class SmartImportUiState(
    val step: ImportStep = ImportStep.PICK,
    val imageUri: String? = null,
    val result: SmartImportResult? = null,
    val ocrFailed: Boolean = false,
    val form: OrderFormState = OrderFormState(),
    val isSaving: Boolean = false,
)

sealed interface SmartImportEffect {
    data object Saved : SmartImportEffect
}

@HiltViewModel
class SmartImportViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val createOrder: CreateOrderUseCase,
    private val shipmentRepository: ShipmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartImportUiState())
    val uiState: StateFlow<SmartImportUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SmartImportEffect>()
    val effects: SharedFlow<SmartImportEffect> = _effects

    fun onImagePicked(uri: String) {
        _uiState.update { it.copy(step = ImportStep.PROCESSING, imageUri = uri) }
        viewModelScope.launch {
            val text = ocrEngine.recognize(uri)
            val result = if (text == null) {
                SmartImportResult(
                    detectedPlatform = null,
                    draft = OrderDraft(),
                    warnings = listOf("no_text"),
                    overallConfidence = 0f,
                    usedGroq = false,
                )
            } else {
                SmartImportParser.parse(text)
            }
            _uiState.update {
                it.copy(
                    step = ImportStep.REVIEW,
                    ocrFailed = text == null,
                    result = result,
                    form = formFrom(result),
                )
            }
        }
    }

    fun updateForm(transform: (OrderFormState) -> OrderFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    private fun formFrom(result: SmartImportResult): OrderFormState {
        val d = result.draft
        fun money(v: Long?) = v?.let { MoneyFormat.formatMinor(it, "PHP") }.orEmpty()
        return OrderFormState(
            title = d.title.value.orEmpty(),
            platform = d.platform.value,
            sellerName = d.sellerName.value.orEmpty(),
            total = money(d.totalMinor.value),
            shipping = money(d.shippingMinor.value),
            discount = money(d.discountMinor.value),
            paymentMethod = d.paymentMethod.value
                ?: com.ordra.domain.model.PaymentMethod.UNKNOWN,
            courier = d.courier.value ?: com.ordra.domain.model.Courier.UNKNOWN,
            trackingNumber = d.trackingNumber.value.orEmpty(),
            orderedDate = d.orderedDate.value,
            etaStart = d.etaStart.value,
            etaEnd = d.etaEnd.value,
        )
    }

    fun confirm() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = Instant.now()
            val order = state.form.toOrder(null, now)
            when (val result = createOrder(order)) {
                is AppResult.Success -> {
                    state.form.toShipment(result.value, null, now)?.let { shipment ->
                        shipmentRepository.saveShipment(shipment)
                        state.result?.draft?.shipmentStatus?.value?.let { status ->
                            shipmentRepository.updateShipmentStatus(
                                shipment.id, status,
                                TrackingStatusSource.SCREENSHOT_OCR,
                            )
                        }
                    }
                    _effects.emit(SmartImportEffect.Saved)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }
}
