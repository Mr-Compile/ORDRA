package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Upsert
    suspend fun upsertOrder(order: OrderEntity)

    @Upsert
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: String)

    @Transaction
    suspend fun upsertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        upsertOrder(order)
        deleteItemsForOrder(order.id)
        upsertItems(items.map { it.copy(orderId = order.id) })
    }

    @Transaction
    @Query("SELECT * FROM orders WHERE archivedAt IS NULL ORDER BY updatedAt DESC")
    fun observeActiveOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE archivedAt IS NOT NULL ORDER BY updatedAt DESC")
    fun observeArchivedOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun observeOrder(orderId: String): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrder(orderId: String): OrderWithItems?

    @Query("UPDATE orders SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun setArchivedAt(orderId: String, archivedAt: Long?, updatedAt: Long)

    @Query("UPDATE orders SET status = :status, deliveredAt = :deliveredAt, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, status: String, deliveredAt: Long?, updatedAt: Long)

    @Query("UPDATE orders SET rating = :rating, isFavorite = :isFavorite WHERE id = :orderId")
    suspend fun updateRatingFavorite(orderId: String, rating: Int?, isFavorite: Boolean)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: String)

    /**
     * Combined search/filter/sort. SQL is built by the repository from a
     * domain OrderQuery — the DAO stays persistence-only.
     */
    @Transaction
    @RawQuery(observedEntities = [OrderEntity::class, OrderItemEntity::class])
    fun observeOrdersRaw(query: SupportSQLiteQuery): Flow<List<OrderWithItems>>
}
