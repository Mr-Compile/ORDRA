package com.ordra.feature.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.app.navigation.OrdraRoutes
import com.ordra.domain.model.TrackingEvent
import com.ordra.domain.repository.ShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TimelineUiState(
    val events: List<TrackingEvent> = emptyList(),
    val loaded: Boolean = false,
)

@HiltViewModel
class ShipmentTimelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    shipmentRepository: ShipmentRepository,
) : ViewModel() {

    private val shipmentId: String = checkNotNull(savedStateHandle[OrdraRoutes.ARG_SHIPMENT_ID])

    val uiState: StateFlow<TimelineUiState> =
        shipmentRepository.observeEvents(shipmentId)
            .map { TimelineUiState(events = it, loaded = true) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineUiState())
}
