package com.ordra.data.local.mapper

import com.ordra.data.local.entity.CategoryEntity
import com.ordra.domain.model.Category
import java.time.Instant

object CategoryMapper {
    fun toDomain(entity: CategoryEntity): Category = Category(
        id = entity.id,
        name = entity.name,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
    )

    fun toEntity(domain: Category): CategoryEntity = CategoryEntity(
        id = domain.id,
        name = domain.name,
        createdAt = domain.createdAt.toEpochMilli(),
        updatedAt = domain.updatedAt.toEpochMilli(),
    )
}
