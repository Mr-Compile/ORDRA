package com.ordra.domain.repository

import com.ordra.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeCategoriesForOrder(orderId: String): Flow<List<Category>>
    suspend fun getCategoryIdsForOrder(orderId: String): List<String>
    suspend fun createCategory(name: String): Category
    suspend fun setOrderCategories(orderId: String, categoryIds: List<String>)
}
