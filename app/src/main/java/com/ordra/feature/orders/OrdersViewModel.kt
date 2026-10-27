package com.ordra.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.domain.model.Category
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderSort
import com.ordra.domain.repository.CategoryRepository
import com.ordra.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val categories: List<Category> = emptyList(),
    val query: OrderQuery = OrderQuery(),
    val loaded: Boolean = false,
) {
    val isEmpty: Boolean get() = orders.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow(OrderQuery())
    val query: StateFlow<OrderQuery> = _query.asStateFlow()

    private val ordersFlow = _query.flatMapLatest { orderRepository.observeOrders(it) }
    private val categoriesFlow = categoryRepository.observeCategories()

    val uiState: StateFlow<OrdersUiState> = combine(
        ordersFlow,
        categoriesFlow,
        _query,
    ) { orders, categories, q ->
        OrdersUiState(orders = orders, categories = categories, query = q, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())

    fun updateQuery(transform: (OrderQuery) -> OrderQuery) {
        _query.value = transform(_query.value)
    }

    fun resetQuery() {
        _query.value = OrderQuery()
    }
}
