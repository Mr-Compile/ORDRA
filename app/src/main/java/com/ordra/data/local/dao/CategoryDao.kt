package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        "SELECT categories.* FROM categories " +
            "INNER JOIN order_categories ON order_categories.categoryId = categories.id " +
            "WHERE order_categories.orderId = :orderId ORDER BY name ASC",
    )
    fun observeForOrder(orderId: String): Flow<List<CategoryEntity>>

    @Query("SELECT categoryId FROM order_categories WHERE orderId = :orderId")
    suspend fun getCategoryIdsForOrder(orderId: String): List<String>

    @Query("DELETE FROM order_categories WHERE orderId = :orderId")
    suspend fun clearForOrder(orderId: String)

    @Upsert
    suspend fun upsertLinks(links: List<OrderCategoryEntity>)

    @Transaction
    suspend fun setForOrder(orderId: String, categoryIds: List<String>) {
        clearForOrder(orderId)
        upsertLinks(categoryIds.map { OrderCategoryEntity(orderId = orderId, categoryId = it) })
    }
}
