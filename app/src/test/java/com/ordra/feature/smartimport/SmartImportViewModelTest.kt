package com.ordra.feature.smartimport

import com.ordra.core.ocr.OcrEngine
import com.ordra.data.groq.GroqExtractor
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.TrackingEvent
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.model.UserSettings
import com.ordra.domain.repository.OrderRepository
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.usecase.CreateOrderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmartImportViewModelTest {

    private class FakeOcrEngine(private val texts: Map<String, String?>) : OcrEngine {
        override suspend fun recognize(imageUri: String): String? = texts[imageUri]
    }

    private class FakeOrderRepository : OrderRepository {
        override fun observeActiveOrders(): Flow<List<Order>> = flowOf(emptyList())
        override fun observeOrders(query: OrderQuery): Flow<List<Order>> = flowOf(emptyList())
        override fun observeArchivedOrders(): Flow<List<Order>> = flowOf(emptyList())
        override fun observeOrder(orderId: String): Flow<Order?> = flowOf(null)
        override suspend fun getOrder(orderId: String): Order? = null
        override suspend fun createOrder(order: Order) {}
        override suspend fun updateOrder(order: Order) {}
        override suspend fun archiveOrder(orderId: String) {}
        override suspend fun restoreOrder(orderId: String) {}
        override suspend fun updateStatus(orderId: String, status: OrderStatus) {}
        override suspend fun setRatingFavorite(orderId: String, rating: Int?, isFavorite: Boolean) {}
        override suspend fun deleteOrderPermanently(orderId: String) {}
    }

    private class FakeShipmentRepository : ShipmentRepository {
        override fun observePrimaryShipment(orderId: String): Flow<Shipment?> = flowOf(null)
        override fun observeEvents(shipmentId: String): Flow<List<TrackingEvent>> =
            flowOf(emptyList())
        override fun observeAttentionShipmentCount(): Flow<Int> = flowOf(0)
        override suspend fun getPrimaryShipment(orderId: String): Shipment? = null
        override suspend fun saveShipment(shipment: Shipment) {}
        override suspend fun updateShipmentStatus(
            shipmentId: String,
            status: ShipmentStatus,
            source: TrackingStatusSource,
            confidence: DetectionConfidence,
            title: String?,
        ) {}
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val settings: Flow<UserSettings> = MutableStateFlow(UserSettings())
        override suspend fun setThemeMode(themeMode: ThemeMode) {}
        override suspend fun setCurrencyCode(currencyCode: String) {}
        override suspend fun setDefaultPlatform(platform: Platform?) {}
        override suspend fun setDefaultPaymentMethod(method: PaymentMethod?) {}
        override suspend fun setDetectionMarketplace(enabled: Boolean) {}
        override suspend fun setDetectionCourier(enabled: Boolean) {}
        override suspend fun setDetectionRider(enabled: Boolean) {}
        override suspend fun setAutoApplyHighConfidence(enabled: Boolean) {}
        override suspend fun setAiAssistedDetection(enabled: Boolean) {}
        override suspend fun setOnlineAnalysisEnabled(enabled: Boolean) {}
        override suspend fun setGroqApiKey(key: String) {}
        override suspend fun setDailySummaryEnabled(enabled: Boolean) {}
        override suspend fun setOnboardingComplete(complete: Boolean) {}
    }

    private fun viewModel(ocrEngine: OcrEngine): SmartImportViewModel {
        val settings = FakeSettingsRepository()
        return SmartImportViewModel(
            ocrEngine = ocrEngine,
            createOrder = CreateOrderUseCase(FakeOrderRepository()),
            shipmentRepository = FakeShipmentRepository(),
            groqExtractor = GroqExtractor(settings),
            settingsRepository = settings,
        )
    }

    @Before
    fun setUpMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun resetMain() = Dispatchers.resetMain()

    @Test
    fun `two screenshots merge into one parsed draft`() = runTest {
        val ocr = FakeOcrEngine(
            mapOf(
                "content://shot1" to """
                    Shopee
                    Soundcore R50i Wireless Earbuds
                    Total ₱649.00
                    Cash on Delivery
                """.trimIndent(),
                "content://shot2" to """
                    SPX Express
                    SPXPH123456789
                """.trimIndent(),
            ),
        )
        val vm = viewModel(ocr)

        vm.onImagesPicked(listOf("content://shot1", "content://shot2"))

        val state = vm.uiState.value
        assertEquals(ImportStep.REVIEW, state.step)
        assertFalse(state.ocrFailed)
        val draft = state.result!!.draft
        assertEquals(Platform.SHOPEE, state.result!!.detectedPlatform)
        assertEquals("Soundcore R50i Wireless Earbuds", draft.title.value)
        assertEquals(64900L, draft.totalMinor.value)
        assertEquals("SPXPH123456789", draft.trackingNumber.value)
        assertTrue(draft.rawOcrText!!.contains("SPX Express"))
    }

    @Test
    fun `all unreadable images yields ocr failure`() = runTest {
        val ocr = FakeOcrEngine(
            mapOf("content://a" to null, "content://b" to null),
        )
        val vm = viewModel(ocr)

        vm.onImagesPicked(listOf("content://a", "content://b"))

        val state = vm.uiState.value
        assertEquals(ImportStep.REVIEW, state.step)
        assertTrue(state.ocrFailed)
        assertTrue("no_text" in state.result!!.warnings)
    }

    @Test
    fun `partially readable images still parse what exists`() = runTest {
        val ocr = FakeOcrEngine(
            mapOf("content://a" to null, "content://b" to "Shopee\nTotal ₱100.00"),
        )
        val vm = viewModel(ocr)

        vm.onImagesPicked(listOf("content://a", "content://b"))

        val state = vm.uiState.value
        assertFalse(state.ocrFailed)
        assertEquals(10000L, state.result!!.draft.totalMinor.value)
    }

    @Test
    fun `empty selection stays on picker step`() = runTest {
        val vm = viewModel(FakeOcrEngine(emptyMap()))

        vm.onImagesPicked(emptyList())

        assertEquals(ImportStep.PICK, vm.uiState.value.step)
    }
}
