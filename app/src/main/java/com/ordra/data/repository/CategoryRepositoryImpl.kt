package com.ordra.data.repository

import com.ordra.data.local.dao.CategoryDao
import com.ordra.data.local.mapper.CategoryMapper
import com.ordra.domain.model.Category
import com.ordra.domain.repository.CategoryRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map(CategoryMapper::toDomain) }

    override fun observeCategoriesForOrder(orderId: String): Flow<List<Category>> =
        categoryDao.observeForOrder(orderId).map { list -> list.map(CategoryMapper::toDomain) }

    override suspend fun getCategoryIdsForOrder(orderId: String): List<String> =
        categoryDao.getCategoryIdsForOrder(orderId)

    override suspend fun createCategory(name: String): Category {
        val now = Instant.now()
        return Category(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            createdAt = now,
            updatedAt = now,
        ).also { categoryDao.upsert(CategoryMapper.toEntity(it)) }
    }

    override suspend fun setOrderCategories(orderId: String, categoryIds: List<String>) =
        categoryDao.setForOrder(orderId, categoryIds)
}
