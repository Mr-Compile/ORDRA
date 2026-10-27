package com.ordra.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ordra.data.local.OrderQuerySql
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderSort
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.Platform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrderQueryTest {

    private lateinit var db: OrdraDatabase

    private fun order(
        id: String,
        title: String = "Order $id",
        platform: String = "SHOPEE",
        status: String = "ORDERED",
        isCod: Boolean = false,
        paymentStatus: String = "UNPAID",
        total: Long? = null,
        seller: String? = null,
        notes: String? = null,
        createdAt: Long = 0,
        eta: Long? = null,
    ) = OrderEntity(
        id = id,
        title = title,
        platform = platform,
        customPlatformName = null,
        sellerName = seller,
        status = status,
        paymentMethod = if (isCod) "COD" else "EWALLET",
        paymentStatus = paymentStatus,
        isCod = isCod,
        currencyCode = "PHP",
        itemSubtotalMinor = null,
        shippingFeeMinor = null,
        discountMinor = null,
        totalAmountMinor = total,
        orderedDate = null,
        estimatedDeliveryStart = eta,
        estimatedDeliveryEnd = null,
        deliveredAt = null,
        notes = notes,
        productImageUri = null,
        archivedAt = null,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    private suspend fun query(q: OrderQuery) =
        db.orderDao().observeOrdersRaw(OrderQuerySql.build(q)).first().map { it.order.id }

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OrdraDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `text search matches title seller and notes case-insensitively`() = runBlocking {
        db.orderDao().upsertOrder(order("o1", title = "Anker earbuds"))
        db.orderDao().upsertOrder(order("o2", seller = "GadgetHub"))
        db.orderDao().upsertOrder(order("o3", notes = "gate code 1234"))

        assertEquals(listOf("o1"), query(OrderQuery(text = "ANKER")))
        assertEquals(listOf("o2"), query(OrderQuery(text = "gadget")))
        assertEquals(listOf("o3"), query(OrderQuery(text = "gate")))
    }

    @Test
    fun `text search matches item names and tracking numbers`() = runBlocking {
        db.orderDao().upsertOrder(order("o1"))
        db.orderDao().upsertItems(
            listOf(
                com.ordra.data.local.entity.OrderItemEntity(
                    id = "i1", orderId = "o1", name = "Wireless Mouse",
                    variant = null, quantity = 1, unitPriceMinor = null,
                    lineTotalMinor = null, imageUri = null, notes = null,
                ),
            ),
        )
        db.orderDao().upsertOrder(order("o2"))
        db.shipmentDao().upsertShipment(
            ShipmentEntity(
                id = "s1", orderId = "o2", courier = "SPX_EXPRESS",
                customCourierName = null, trackingNumber = "SPXPH998877",
                shipmentStatus = "IN_TRANSIT", statusSource = "MANUAL",
                statusConfidence = "HIGH", lastStatusAt = null, lastDetectedAt = null,
                createdAt = 0, updatedAt = 0,
            ),
        )

        assertEquals(listOf("o1"), query(OrderQuery(text = "mouse")))
        assertEquals(listOf("o2"), query(OrderQuery(text = "998877")))
    }

    @Test
    fun `platform and status filters combine`() = runBlocking {
        db.orderDao().upsertOrder(order("o1", platform = "SHOPEE", status = "SHIPPED"))
        db.orderDao().upsertOrder(order("o2", platform = "LAZADA", status = "SHIPPED"))
        db.orderDao().upsertOrder(order("o3", platform = "SHOPEE", status = "DELIVERED"))

        assertEquals(
            listOf("o1"),
            query(
                OrderQuery(
                    platforms = setOf(Platform.SHOPEE),
                    statuses = setOf(OrderStatus.SHIPPED),
                ),
            ),
        )
    }

    @Test
    fun `codOnly excludes paid and terminal orders`() = runBlocking {
        db.orderDao().upsertOrder(order("open", isCod = true, status = "SHIPPED"))
        db.orderDao().upsertOrder(order("paid", isCod = true, status = "SHIPPED", paymentStatus = "PAID"))
        db.orderDao().upsertOrder(order("done", isCod = true, status = "DELIVERED"))
        db.orderDao().upsertOrder(order("ewallet", isCod = false, status = "SHIPPED"))

        assertEquals(listOf("open"), query(OrderQuery(codOnly = true)))
    }

    @Test
    fun `category filter uses junction table`() = runBlocking {
        db.orderDao().upsertOrder(order("o1"))
        db.orderDao().upsertOrder(order("o2"))
        db.categoryDao().upsert(CategoryEntity("c1", "Electronics", 0, 0))
        db.categoryDao().setForOrder("o1", listOf("c1"))

        assertEquals(listOf("o1"), query(OrderQuery(categoryIds = setOf("c1"))))
    }

    @Test
    fun `sorts order results correctly`() = runBlocking {
        db.orderDao().upsertOrder(order("new", createdAt = 300, total = 100))
        db.orderDao().upsertOrder(order("old", createdAt = 100, total = 300))
        db.orderDao().upsertOrder(order("mid", createdAt = 200, total = 200))

        assertEquals(listOf("new", "mid", "old"), query(OrderQuery(sort = OrderSort.NEWEST)))
        assertEquals(listOf("old", "mid", "new"), query(OrderQuery(sort = OrderSort.OLDEST)))
        assertEquals(listOf("old", "mid", "new"), query(OrderQuery(sort = OrderSort.HIGHEST_TOTAL)))
        assertEquals(listOf("new", "mid", "old"), query(OrderQuery(sort = OrderSort.LOWEST_TOTAL)))
    }

    @Test
    fun `archived orders only appear when requested`() = runBlocking {
        db.orderDao().upsertOrder(order("live"))
        db.orderDao().upsertOrder(order("archived").copy(archivedAt = 5L))

        assertEquals(listOf("live"), query(OrderQuery()))
        assertEquals(listOf("archived"), query(OrderQuery(includeArchived = true)))
    }
}
