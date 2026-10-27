package com.ordra.domain.model

import java.time.Instant

data class Category(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
