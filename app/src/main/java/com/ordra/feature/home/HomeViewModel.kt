package com.ordra.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.core.ui.ai.CompanionBannerData
import com.ordra.core.ui.ai.CompanionBannerKind
import com.ordra.core.ui.ai.resolveCompanionBanner
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.repository.OrderRepository
import com.ordra.domain.repository.ShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private val TERMINAL_STATUSES = setOf(
    OrderStatus.DELIVERED,
    OrderStatus.CANCELLED,
    OrderStatus.RETURNED,
    OrderStatus.REFUNDED,
)

private val RECENT_DELIVERY_WINDOW: Duration = Duration.ofHours(48)

data class HomeUiState(
    val activeCount: Int = 0,
    val arrivingSoonCount: Int = 0,
    val codToPrepareMinor: Long? = null,
    val codCurrency: String = "PHP",
    val upcoming: List<Order> = emptyList(),
    val recent: List<Order> = emptyList(),
    val archived: List<Order> = emptyList(),
    val showingArchived: Boolean = false,
    val banner: CompanionBannerData = CompanionBannerData(CompanionBannerKind.EMPTY),
    val loaded: Boolean = false,
) {
    val isEmpty: Boolean get() = upcoming.isEmpty() && recent.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    orderRepository: OrderRepository,
    shipmentRepository: ShipmentRepository,
) : ViewModel() {

    private val showingArchived = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        orderRepository.observeActiveOrders(),
        orderRepository.observeArchivedOrders(),
        shipmentRepository.observeAttentionShipmentCount(),
        showingArchived,
    ) { active, archived, attentionShipments, showArchived ->
        val today = LocalDate.now()
        val soonWindowEnd = today.plusDays(3)

        fun hasEta(order: Order) =
            order.estimatedDeliveryStart != null || order.estimatedDeliveryEnd != null

        fun etaWithin(order: Order, end: LocalDate): Boolean {
            val eta = order.estimatedDeliveryStart ?: order.estimatedDeliveryEnd ?: return false
            return !eta.isBefore(today) && !eta.isAfter(end)
        }

        val upcoming = active
            .filter { it.status !in TERMINAL_STATUSES && hasEta(it) }
            .sortedBy { it.estimatedDeliveryStart ?: it.estimatedDeliveryEnd }
        val arrivingSoon = upcoming.count { etaWithin(it, soonWindowEnd) }

        val codOrders = active.filter {
            it.isCod && it.paymentStatus != PaymentStatus.PAID &&
                it.status !in TERMINAL_STATUSES && it.totalAmountMinor != null
        }

        // Companion banner inputs — every count comes from persisted order/shipment data.
        val arrivingToday = upcoming.count { etaWithin(it, today) }
        val overdueEta = active.count {
            it.status !in TERMINAL_STATUSES &&
                (it.estimatedDeliveryEnd ?: it.estimatedDeliveryStart)?.isBefore(today) == true
        }
        val deliveredRecently = active.count {
            it.status == OrderStatus.DELIVERED &&
                it.deliveredAt?.isAfter(Instant.now().minus(RECENT_DELIVERY_WINDOW)) == true
        }
        val banner = resolveCompanionBanner(
            activeCount = active.count { it.status !in TERMINAL_STATUSES },
            arrivingTodayCount = arrivingToday,
            attentionCount = attentionShipments + overdueEta,
            recentlyDeliveredCount = deliveredRecently,
        )

        HomeUiState(
            activeCount = active.size,
            arrivingSoonCount = arrivingSoon,
            codToPrepareMinor = codOrders.sumOf { it.totalAmountMinor ?: 0L }
                .takeIf { codOrders.isNotEmpty() },
            codCurrency = codOrders.firstOrNull()?.currencyCode ?: "PHP",
            upcoming = upcoming,
            recent = active.sortedByDescending { it.updatedAt }.take(10),
            archived = archived,
            showingArchived = showArchived,
            banner = banner,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleArchived() {
        showingArchived.value = !showingArchived.value
    }
}
