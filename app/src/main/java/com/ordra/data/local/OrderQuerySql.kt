package com.ordra.data.local

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderSort

/**
 * Builds the SQL for combined order search/filter/sort.
 * Search covers title, seller, notes, item names and shipment tracking numbers.
 * LIKE is case-insensitive for ASCII text (SQLite default).
 */
object OrderQuerySql {

    fun build(query: OrderQuery): SupportSQLiteQuery {
        val args = mutableListOf<Any>()
        val where = StringBuilder(
            if (query.includeArchived) "archivedAt IS NOT NULL" else "archivedAt IS NULL",
        )

        val text = query.text.trim()
        if (text.isNotEmpty()) {
            val like = "%${text.replace("%", "")}%"
            where.append(
                " AND (orders.title LIKE ? OR orders.sellerName LIKE ? OR orders.notes LIKE ?" +
                    " OR EXISTS (SELECT 1 FROM order_items oi WHERE oi.orderId = orders.id AND oi.name LIKE ?)" +
                    " OR EXISTS (SELECT 1 FROM shipments s WHERE s.orderId = orders.id AND s.trackingNumber LIKE ?))",
            )
            repeat(5) { args += like }
        }

        if (query.platforms.isNotEmpty()) {
            where.append(" AND orders.platform IN (${placeholders(query.platforms.size)})")
            query.platforms.forEach { args += it.name }
        }
        if (query.statuses.isNotEmpty()) {
            where.append(" AND orders.status IN (${placeholders(query.statuses.size)})")
            query.statuses.forEach { args += it.name }
        }
        if (query.codOnly) {
            where.append(" AND orders.isCod = 1 AND orders.paymentStatus != 'PAID'")
            where.append(" AND orders.status NOT IN ('DELIVERED','CANCELLED','RETURNED','REFUNDED')")
        }
        if (query.categoryIds.isNotEmpty()) {
            where.append(
                " AND EXISTS (SELECT 1 FROM order_categories oc" +
                    " WHERE oc.orderId = orders.id AND oc.categoryId IN" +
                    " (${placeholders(query.categoryIds.size)}))",
            )
            query.categoryIds.forEach { args += it }
        }

        val orderBy = when (query.sort) {
            OrderSort.NEWEST -> "orders.createdAt DESC"
            OrderSort.OLDEST -> "orders.createdAt ASC"
            OrderSort.DELIVERY_SOONEST ->
                "CASE WHEN orders.estimatedDeliveryStart IS NULL THEN 1 ELSE 0 END," +
                    " orders.estimatedDeliveryStart ASC"
            OrderSort.HIGHEST_TOTAL -> "orders.totalAmountMinor DESC"
            OrderSort.LOWEST_TOTAL ->
                "CASE WHEN orders.totalAmountMinor IS NULL THEN 1 ELSE 0 END," +
                    " orders.totalAmountMinor ASC"
        }

        return SimpleSQLiteQuery(
            "SELECT orders.* FROM orders WHERE $where ORDER BY $orderBy",
            args.toTypedArray(),
        )
    }

    private fun placeholders(count: Int) = List(count) { "?" }.joinToString(",")
}
