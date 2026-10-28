package com.ordra.domain.service

import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import java.time.Instant
import java.time.LocalDate

/**
 * Deterministic local responder for Ordi chat. Answers are generated ONLY from
 * real on-device data — Ordi never invents tracking updates. Pure functions so
 * the whole surface is unit-testable.
 */
object OrdiResponder {

    /** Snapshot of local data the responder may cite. Built by the repository. */
    data class Context(
        val orders: List<Order>,
        val shipments: Map<String, Shipment>,
        val pendingDetections: Int,
        val currencyCode: String,
        val today: LocalDate,
        val now: Instant,
    )

    private val ACTIVE = setOf(
        OrderStatus.ORDERED, OrderStatus.PROCESSING, OrderStatus.PACKED,
        OrderStatus.SHIPPED, OrderStatus.DRAFT,
    )

    fun reply(rawPrompt: String, ctx: Context): String {
        val p = rawPrompt.lowercase()
        return when {
            p.isBlank() -> "I didn't catch that — try \"What's arriving?\" or \"COD to prepare\"."
            hasAny(p, "arriv", "coming", "deliver", "eta", "when") -> arriving(ctx)
            hasAny(p, "cod", "cash", "collect", "pay", "prepare") -> cod(ctx)
            hasAny(p, "late", "delay", "overdue", "stuck") -> late(ctx)
            hasAny(p, "update", "detect", "notif", "scan") -> detections(ctx)
            hasAny(p, "spend", "spent", "cost", "total", "money", "much") -> spend(ctx)
            hasAny(p, "order", "parcel", "package", "how many", "list") -> orders(ctx)
            hasAny(p, "hi", "hello", "hey", "help", "what can") -> greeting(ctx)
            else -> fallback(ctx)
        }
    }

    private fun hasAny(p: String, vararg keys: String) = keys.any { p.contains(it) }

    private fun titleOf(o: Order) =
        o.title.ifBlank { o.items.firstOrNull()?.name ?: "Order" }

    private fun money(minor: Long, code: String): String {
        val whole = minor / 100
        return "$code %,d".format(whole)
    }

    private fun active(ctx: Context) = ctx.orders.filter { it.status in ACTIVE }

    private fun arriving(ctx: Context): String {
        val soon = active(ctx)
            .filter { it.estimatedDeliveryEnd != null || it.estimatedDeliveryStart != null }
            .sortedBy { it.estimatedDeliveryEnd ?: it.estimatedDeliveryStart }
        if (soon.isEmpty()) {
            return "No delivery dates are set on your active orders. " +
                "Add an ETA in an order's Delivery section and I'll keep an eye on it."
        }
        val lines = soon.take(4).map { o ->
            val eta = o.estimatedDeliveryEnd ?: o.estimatedDeliveryStart!!
            val label = when {
                eta.isBefore(ctx.today) -> "was due $eta — overdue"
                eta == ctx.today -> "arriving today"
                else -> "arriving $eta"
            }
            val ship = ctx.shipments[o.id]?.shipmentStatus
                ?.takeIf { it != ShipmentStatus.UNKNOWN }
            val extra = ship?.let { " (${it.name.lowercase().replace('_', ' ')})" } ?: ""
            "• ${titleOf(o)} — $label$extra"
        }
        val more = if (soon.size > 4) "\n…and ${soon.size - 4} more." else ""
        return "Here's what's coming up:\n${lines.joinToString("\n")}$more"
    }

    private fun cod(ctx: Context): String {
        val pending = ctx.orders.filter {
            it.isCod && it.paymentStatus != PaymentStatus.PAID &&
                it.status != OrderStatus.CANCELLED && it.status != OrderStatus.REFUNDED
        }
        if (pending.isEmpty()) return "No COD to prepare — nothing is waiting on cash collection."
        val total = pending.sumOf { it.totalAmountMinor ?: 0L }
        val names = pending.take(3).joinToString(", ") { titleOf(it) }
        val suffix = if (pending.size > 3) " and ${pending.size - 3} more" else ""
        return "You have ${money(total, ctx.currencyCode)} in COD across " +
            "${pending.size} parcel${if (pending.size == 1) "" else "s"}: $names$suffix."
    }

    private fun late(ctx: Context): String {
        val overdue = active(ctx).filter {
            (it.estimatedDeliveryEnd ?: it.estimatedDeliveryStart)?.isBefore(ctx.today) == true
        }
        val failed = ctx.shipments.values.count { it.shipmentStatus == ShipmentStatus.DELIVERY_FAILED }
        return when {
            overdue.isEmpty() && failed == 0 ->
                "Nothing looks late right now. Everything with a delivery date is still on schedule."
            else -> buildString {
                overdue.forEach { o ->
                    val eta = o.estimatedDeliveryEnd ?: o.estimatedDeliveryStart!!
                    append("• ${titleOf(o)} — due $eta, still not delivered\n")
                }
                if (failed > 0) append("• $failed delivery attempt${if (failed > 1) "s" else ""} marked failed")
            }.trimEnd()
        }
    }

    private fun detections(ctx: Context): String =
        if (ctx.pendingDetections == 0) {
            "No updates are waiting for review. When a courier or marketplace " +
                "notification looks like a tracking update, I'll ask you here first."
        } else {
            "${ctx.pendingDetections} detected update${if (ctx.pendingDetections == 1) " is" else "s are"} " +
                "waiting for your review — open Detected updates to apply or ignore them."
        }

    private fun spend(ctx: Context): String {
        val orders = ctx.orders.filter { !it.isArchived }
        if (orders.none { it.totalAmountMinor != null }) {
            return "No spending recorded yet — add totals to your orders and I'll sum them up."
        }
        val total = SpendingAnalytics.totalSpend(orders)
        val delivered = SpendingAnalytics.deliveredSpend(orders)
        return "So far you've spent ${money(total, ctx.currencyCode)} across " +
            "${SpendingAnalytics.spendable(orders).size} orders " +
            "(${money(delivered, ctx.currencyCode)} delivered)."
    }

    private fun orders(ctx: Context): String {
        val count = active(ctx).size
        return if (count == 0) {
            "You have no active orders. Add one and I'll help you track it."
        } else {
            "You're tracking $count active parcel${if (count == 1) "" else "s"}. " +
                "Ask \"What's arriving?\" for the delivery rundown."
        }
    }

    private fun greeting(ctx: Context): String =
        "Hi! I'm Ordi — your parcel companion. I can tell you what's arriving, " +
            "how much COD to prepare, or what's running late — all from your local data."

    private fun fallback(ctx: Context): String {
        val base = "I can check what's arriving, COD to prepare, late parcels, " +
            "detected updates, or your spending."
        return if (ctx.pendingDetections > 0) {
            "$base\n\nBy the way — ${ctx.pendingDetections} detected update" +
                "${if (ctx.pendingDetections == 1) " needs" else "s need"} your review."
        } else base
    }
}
