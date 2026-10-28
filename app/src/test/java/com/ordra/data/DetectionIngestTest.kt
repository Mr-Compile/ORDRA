package com.ordra.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.data.repository.DetectionRepositoryImpl
import com.ordra.data.repository.ShipmentRepositoryImpl
import com.ordra.domain.model.Courier
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.SuggestionState
import com.ordra.domain.model.TrackingStatusSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DetectionIngestTest {

    private lateinit var db: OrdraDatabase
    private lateinit var repository: DetectionRepositoryImpl

    private fun orderEntity(id: String) = OrderEntity(
        id = id,
        title = "Order $id",
        platform = "SHOPEE",
        customPlatformName = null,
        sellerName = "Seller",
        status = "ORDERED",
        paymentMethod = "COD",
        paymentStatus = "UNPAID",
        isCod = true,
        currencyCode = "PHP",
        itemSubtotalMinor = 64900,
        shippingFeeMinor = 0,
        discountMinor = null,
        totalAmountMinor = 64900,
        orderedDate = 1_700_000_000_000,
        estimatedDeliveryStart = null,
        estimatedDeliveryEnd = null,
        deliveredAt = null,
        notes = null,
        productImageUri = null,
        rating = null,
        isFavorite = false,
        archivedAt = null,
        createdAt = 1_700_000_000_000,
        updatedAt = 1_700_000_000_000,
    )

    private fun shipmentEntity(
        id: String,
        orderId: String,
        tracking: String?,
        status: String = "IN_TRANSIT",
        courier: String = "SPX_EXPRESS",
    ) = ShipmentEntity(
        id = id,
        orderId = orderId,
        courier = courier,
        customCourierName = null,
        trackingNumber = tracking,
        shipmentStatus = status,
        statusSource = "MANUAL",
        statusConfidence = "HIGH",
        lastStatusAt = null,
        lastDetectedAt = null,
        createdAt = 1_700_000_000_000,
        updatedAt = 1_700_000_000_000,
    )

    private fun signals(
        tracking: String? = null,
        courier: Courier? = Courier.SPX_EXPRESS,
        status: String = "OUT_FOR_DELIVERY",
        cod: Long? = null,
    ) = NotificationSignals(
        isDeliveryRelevant = true,
        trackingNumber = tracking,
        courier = courier,
        statusText = status,
        codAmountMinor = cod,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OrdraDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = DetectionRepositoryImpl(
            db.detectionDao(),
            db.orderDao(),
            db.shipmentDao(),
            ShipmentRepositoryImpl(db.shipmentDao(), db.trackingEventDao()),
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `high-confidence match auto-applies when enabled`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.shipmentDao().upsertShipment(shipmentEntity("s1", "o1", "SPXPH999"))

        val suggestion = repository.ingest(
            signals(tracking = "SPXPH999"),
            TrackingStatusSource.MARKETPLACE_NOTIFICATION,
            autoApplyHigh = true,
        )

        assertNotNull(suggestion)
        assertEquals(SuggestionState.CONFIRMED, suggestion!!.state)
        assertNotNull(suggestion.appliedAt)
        val shipment = db.shipmentDao().getShipment("s1")!!
        assertEquals("OUT_FOR_DELIVERY", shipment.shipmentStatus)
        assertEquals("MARKETPLACE_NOTIFICATION", shipment.statusSource)
        assertEquals(1, db.trackingEventDao().countEventsWithStatus("s1", "OUT_FOR_DELIVERY"))
    }

    @Test
    fun `high-confidence match stays pending when auto-apply disabled`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.shipmentDao().upsertShipment(shipmentEntity("s1", "o1", "SPXPH999"))

        val suggestion = repository.ingest(
            signals(tracking = "SPXPH999"),
            TrackingStatusSource.MARKETPLACE_NOTIFICATION,
            autoApplyHigh = false,
        )

        assertNotNull(suggestion)
        assertEquals(SuggestionState.PENDING, suggestion!!.state)
        assertNull(suggestion.appliedAt)
        assertEquals("IN_TRANSIT", db.shipmentDao().getShipment("s1")!!.shipmentStatus)
    }

    @Test
    fun `medium single match never auto-applies`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.shipmentDao().upsertShipment(shipmentEntity("s1", "o1", null))

        val suggestion = repository.ingest(
            signals(tracking = null, cod = 64900),
            TrackingStatusSource.COURIER_APP_NOTIFICATION,
            autoApplyHigh = true,
        )

        assertNotNull(suggestion)
        assertEquals(SuggestionState.PENDING, suggestion!!.state)
        assertEquals("IN_TRANSIT", db.shipmentDao().getShipment("s1")!!.shipmentStatus)
    }

    @Test
    fun `ambiguous match never auto-applies`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.orderDao().upsertOrder(orderEntity("o2"))
        db.shipmentDao().upsertShipment(shipmentEntity("s1", "o1", null))
        db.shipmentDao().upsertShipment(shipmentEntity("s2", "o2", null))

        val suggestion = repository.ingest(
            signals(tracking = null, cod = null),
            TrackingStatusSource.COURIER_APP_NOTIFICATION,
            autoApplyHigh = true,
        )

        assertNotNull(suggestion)
        assertTrue(suggestion!!.needsParcelPick)
        assertEquals(SuggestionState.PENDING, suggestion.state)
        assertEquals("IN_TRANSIT", db.shipmentDao().getShipment("s1")!!.shipmentStatus)
        assertEquals("IN_TRANSIT", db.shipmentDao().getShipment("s2")!!.shipmentStatus)
    }

    @Test
    fun `stale detection cannot regress a delivered parcel`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.shipmentDao().upsertShipment(
            shipmentEntity("s1", "o1", "SPXPH999", status = "DELIVERED"),
        )

        val suggestion = repository.ingest(
            signals(tracking = "SPXPH999", status = "IN_TRANSIT"),
            TrackingStatusSource.MARKETPLACE_NOTIFICATION,
            autoApplyHigh = true,
        )

        assertNotNull(suggestion)
        assertNull(suggestion!!.appliedAt)
        assertEquals("DELIVERED", db.shipmentDao().getShipment("s1")!!.shipmentStatus)
    }

    @Test
    fun `reposted notification is deduped after auto-apply`() = runBlocking {
        db.orderDao().upsertOrder(orderEntity("o1"))
        db.shipmentDao().upsertShipment(shipmentEntity("s1", "o1", "SPXPH999"))

        val first = repository.ingest(
            signals(tracking = "SPXPH999"),
            TrackingStatusSource.MARKETPLACE_NOTIFICATION,
            autoApplyHigh = true,
        )
        val second = repository.ingest(
            signals(tracking = "SPXPH999"),
            TrackingStatusSource.MARKETPLACE_NOTIFICATION,
            autoApplyHigh = true,
        )

        assertNotNull(first)
        assertNull(second)
        assertEquals(0, db.detectionDao().observePending().first().size)
    }
}
