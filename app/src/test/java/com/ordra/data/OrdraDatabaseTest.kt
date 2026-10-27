package com.ordra.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrdraDatabaseTest {

    private lateinit var db: OrdraDatabase

    private fun orderEntity(
        id: String,
        archivedAt: Long? = null,
        status: String = "ORDERED",
    ) = OrderEntity(
        id = id,
        title = "Order $id",
        platform = "SHOPEE",
        customPlatformName = null,
        sellerName = "Seller",
        status = status,
        paymentMethod = "COD",
        paymentStatus = "UNPAID",
        isCod = true,
        currencyCode = "PHP",
        itemSubtotalMinor = 10000,
        shippingFeeMinor = 0,
        discountMinor = null,
        totalAmountMinor = 10000,
        orderedDate = 1_700_000_000_000,
        estimatedDeliveryStart = null,
        estimatedDeliveryEnd = null,
        deliveredAt = null,
        notes = null,
        productImageUri = null,
        rating = null,
        isFavorite = false,
        archivedAt = archivedAt,
        createdAt = 1_700_000_000_000,
        updatedAt = 1_700_000_000_000,
    )

    private fun itemEntity(id: String, orderId: String) = OrderItemEntity(
        id = id,
        orderId = orderId,
        name = "Item $id",
        variant = null,
        quantity = 2,
        unitPriceMinor = 5000,
        lineTotalMinor = 10000,
        imageUri = null,
        notes = null,
    )

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
    fun `insert order with items then read back`() = runBlocking {
        db.orderDao().upsertOrderWithItems(
            orderEntity("o1"),
            listOf(itemEntity("i1", "o1"), itemEntity("i2", "o1")),
        )
        val loaded = db.orderDao().getOrder("o1")
        assertNotNull(loaded)
        assertEquals("o1", loaded!!.order.id)
        assertEquals(2, loaded.items.size)
    }

    @Test
    fun `active and archived lists are partitioned`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("a1"))
        db.orderDao().upsertOrder(orderEntity("a2", archivedAt = 7L))

        val active = db.orderDao().observeActiveOrders().first()
        val archived = db.orderDao().observeArchivedOrders().first()

        assertEquals(listOf("a1"), active.map { it.order.id })
        assertEquals(listOf("a2"), archived.map { it.order.id })
    }

    @Test
    fun `setArchivedAt moves order between lists`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.orderDao().setArchivedAt("o1", archivedAt = 42L, updatedAt = 2L)

        assertEquals(0, db.orderDao().observeActiveOrders().first().size)
        assertEquals(1, db.orderDao().observeArchivedOrders().first().size)

        db.orderDao().setArchivedAt("o1", archivedAt = null, updatedAt = 3L)
        assertEquals(1, db.orderDao().observeActiveOrders().first().size)
    }

    @Test
    fun `updateStatus changes status and stamps deliveredAt`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.orderDao().updateStatus("o1", "DELIVERED", deliveredAt = 99L, updatedAt = 100L)

        val loaded = db.orderDao().getOrder("o1")!!
        assertEquals("DELIVERED", loaded.order.status)
        assertEquals(99L, loaded.order.deliveredAt)
    }

    @Test
    fun `delete order cascades to items`() = runBlocking {
        db.orderDao().upsertOrderWithItems(
            orderEntity("o1"),
            listOf(itemEntity("i1", "o1")),
        )
        db.orderDao().deleteOrder("o1")

        assertNull(db.orderDao().getOrder("o1"))
    }

    @Test
    fun `upsertOrderWithItems replaces stale items`() = runBlocking {
        db.orderDao().upsertOrderWithItems(orderEntity("o1"), listOf(itemEntity("i1", "o1")))
        db.orderDao().upsertOrderWithItems(orderEntity("o1"), listOf(itemEntity("i2", "o1")))

        val loaded = db.orderDao().getOrder("o1")!!
        assertEquals(listOf("i2"), loaded.items.map { it.id })
    }
}
