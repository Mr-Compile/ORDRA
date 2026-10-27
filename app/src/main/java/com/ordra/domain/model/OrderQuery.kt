package com.ordra.domain.model

enum class OrderSort {
    NEWEST,
    OLDEST,
    DELIVERY_SOONEST,
    HIGHEST_TOTAL,
    LOWEST_TOTAL,
}

/** Predictable, fully-resettable search/filter state for the Orders screen. */
data class OrderQuery(
    val text: String = "",
    val platforms: Set<Platform> = emptySet(),
    val statuses: Set<OrderStatus> = emptySet(),
    val codOnly: Boolean = false,
    val categoryIds: Set<String> = emptySet(),
    val sort: OrderSort = OrderSort.NEWEST,
    val includeArchived: Boolean = false,
) {
    val hasActiveFilters: Boolean
        get() = text.isNotBlank() || platforms.isNotEmpty() || statuses.isNotEmpty() ||
            codOnly || categoryIds.isNotEmpty()

    val activeFilterCount: Int
        get() = (if (text.isNotBlank()) 1 else 0) +
            platforms.size + statuses.size + categoryIds.size +
            (if (codOnly) 1 else 0)
}
