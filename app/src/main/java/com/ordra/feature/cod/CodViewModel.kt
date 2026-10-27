package com.ordra.feature.cod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CodUiState(
    val dueToday: List<Order> = emptyList(),
    val nextThreeDays: List<Order> = emptyList(),
    val outstanding: List<Order> = emptyList(),
    val outstandingTotalMinor: Long = 0L,
    val currency: String = "PHP",
    val loaded: Boolean = false,
)

@HiltViewModel
class CodViewModel @Inject constructor(
    orderRepository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<CodUiState> =
        orderRepository.observeOrders(OrderQuery(codOnly = true))
            .map { orders ->
                val today = LocalDate.now()
                val plus3 = today.plusDays(3)

                fun eta(o: Order) = o.estimatedDeliveryStart ?: o.estimatedDeliveryEnd

                val dueToday = orders.filter { eta(it) == today }
                val nextThree = orders.filter {
                    val e = eta(it)
                    e != null && e.isAfter(today) && !e.isAfter(plus3)
                }
                CodUiState(
                    dueToday = dueToday.sortedBy { it.title },
                    nextThreeDays = nextThree.sortedBy { eta(it) },
                    outstanding = orders.sortedByDescending { it.updatedAt },
                    outstandingTotalMinor = orders.sumOf { it.totalAmountMinor ?: 0L },
                    currency = orders.firstOrNull()?.currencyCode ?: "PHP",
                    loaded = true,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CodUiState())
}
