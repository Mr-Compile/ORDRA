package com.ordra.data.repository

import com.ordra.data.local.OrderQuerySql
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.mapper.OrderMapper
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.repository.OrderRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
) : OrderRepository {

    override fun observeActiveOrders(): Flow<List<Order>> =
        orderDao.observeActiveOrders().map { list -> list.map(OrderMapper::toDomain) }

    override fun observeOrders(query: OrderQuery): Flow<List<Order>> =
        orderDao.observeOrdersRaw(OrderQuerySql.build(query))
            .map { list -> list.map(OrderMapper::toDomain) }

    override fun observeArchivedOrders(): Flow<List<Order>> =
        orderDao.observeArchivedOrders().map { list -> list.map(OrderMapper::toDomain) }

    override fun observeOrder(orderId: String): Flow<Order?> =
        orderDao.observeOrder(orderId).map { it?.let(OrderMapper::toDomain) }

    override suspend fun getOrder(orderId: String): Order? =
        orderDao.getOrder(orderId)?.let(OrderMapper::toDomain)

    override suspend fun createOrder(order: Order) =
        orderDao.upsertOrderWithItems(
            OrderMapper.toEntity(order),
            order.items.map(OrderMapper::toEntity),
        )

    override suspend fun updateOrder(order: Order) =
        orderDao.upsertOrderWithItems(
            OrderMapper.toEntity(order),
            order.items.map(OrderMapper::toEntity),
        )

    override suspend fun archiveOrder(orderId: String) {
        val now = Instant.now().toEpochMilli()
        orderDao.setArchivedAt(orderId = orderId, archivedAt = now, updatedAt = now)
    }

    override suspend fun restoreOrder(orderId: String) {
        orderDao.setArchivedAt(
            orderId = orderId,
            archivedAt = null,
            updatedAt = Instant.now().toEpochMilli(),
        )
    }

    override suspend fun updateStatus(orderId: String, status: OrderStatus) =
        orderDao.updateStatus(
            orderId = orderId,
            status = status.name,
            deliveredAt = if (status == OrderStatus.DELIVERED) Instant.now().toEpochMilli() else null,
            updatedAt = Instant.now().toEpochMilli(),
        )

    override suspend fun setRatingFavorite(orderId: String, rating: Int?, isFavorite: Boolean) =
        orderDao.updateRatingFavorite(orderId, rating, isFavorite)

    override suspend fun deleteOrderPermanently(orderId: String) = orderDao.deleteOrder(orderId)
}
