package com.ordra.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.domain.model.Order
import com.ordra.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val orders: List<Order> = emptyList(),
    val showingArchived: Boolean = false,
    val loaded: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    orderRepository: OrderRepository,
) : ViewModel() {

    private val showingArchived = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        orderRepository.observeActiveOrders(),
        orderRepository.observeArchivedOrders(),
        showingArchived,
    ) { active, archived, showArchived ->
        HomeUiState(
            orders = if (showArchived) archived else active,
            showingArchived = showArchived,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleArchived() {
        showingArchived.value = !showingArchived.value
    }
}
