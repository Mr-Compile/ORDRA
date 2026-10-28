package com.ordra.data.repository

import com.ordra.data.local.dao.ChatDao
import com.ordra.data.local.entity.ChatMessageEntity
import com.ordra.data.local.entity.ChatThreadEntity
import com.ordra.domain.model.ChatMessage
import com.ordra.domain.model.ChatRole
import com.ordra.domain.model.ChatThread
import com.ordra.domain.repository.ChatRepository
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.OrderRepository
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.service.OrdiResponder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val orderRepository: OrderRepository,
    private val shipmentRepository: ShipmentRepository,
    private val detectionRepository: DetectionRepository,
    private val settingsRepository: SettingsRepository,
) : ChatRepository {

    override fun observeThreads(): Flow<List<ChatThread>> =
        chatDao.observeThreads().map { list -> list.map(::toThread) }

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        chatDao.observeMessages(threadId).map { list -> list.map(::toMessage) }

    override suspend fun createThread(firstPrompt: String): String {
        val now = Instant.now().toEpochMilli()
        val id = UUID.randomUUID().toString()
        val title = firstPrompt.trim().take(40).ifBlank { "Chat" }
        chatDao.insertThread(ChatThreadEntity(id, title, now, now))
        return id
    }

    override suspend fun send(threadId: String, text: String) {
        val now = Instant.now()
        chatDao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                role = ChatRole.USER.name,
                text = text.trim(),
                createdAt = now.toEpochMilli(),
            ),
        )
        val reply = OrdiResponder.reply(text, buildContext(now))
        chatDao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                role = ChatRole.ORDI.name,
                text = reply,
                createdAt = Instant.now().toEpochMilli(),
            ),
        )
        chatDao.getThread(threadId)?.let {
            chatDao.updateThread(it.copy(updatedAt = Instant.now().toEpochMilli()))
        }
    }

    override suspend fun deleteThread(threadId: String) {
        chatDao.deleteMessages(threadId)
        chatDao.deleteThread(threadId)
    }

    private suspend fun buildContext(now: Instant): OrdiResponder.Context {
        val orders = orderRepository.observeActiveOrders().first()
        val shipments = orders.mapNotNull { o ->
            shipmentRepository.getPrimaryShipment(o.id)?.let { o.id to it }
        }.toMap()
        return OrdiResponder.Context(
            orders = orders,
            shipments = shipments,
            pendingDetections = detectionRepository.observePending().first().size,
            currencyCode = settingsRepository.settings.first().currencyCode,
            today = LocalDate.now(ZoneId.systemDefault()),
            now = now,
        )
    }

    private fun toThread(e: ChatThreadEntity) = ChatThread(
        id = e.id,
        title = e.title,
        createdAt = Instant.ofEpochMilli(e.createdAt),
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
    )

    private fun toMessage(e: ChatMessageEntity) = ChatMessage(
        id = e.id,
        threadId = e.threadId,
        role = ChatRole.valueOf(e.role),
        text = e.text,
        createdAt = Instant.ofEpochMilli(e.createdAt),
    )
}
