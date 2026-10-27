package com.ordra.domain.repository

import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeActiveOrders(): Flow<List<Order>>
    fun observeOrders(query: OrderQuery): Flow<List<Order>>
    fun observeArchivedOrders(): Flow<List<Order>>
    fun observeOrder(orderId: String): Flow<Order?>
    suspend fun getOrder(orderId: String): Order?
    suspend fun createOrder(order: Order)
    suspend fun updateOrder(order: Order)
    suspend fun archiveOrder(orderId: String)
    suspend fun restoreOrder(orderId: String)
    suspend fun updateStatus(orderId: String, status: OrderStatus)
    suspend fun deleteOrderPermanently(orderId: String)
}
