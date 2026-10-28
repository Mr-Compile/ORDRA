package com.ordra.feature.ordi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.domain.model.ChatMessage
import com.ordra.domain.model.ChatThread
import com.ordra.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrdiChatUiState(
    val threads: List<ChatThread> = emptyList(),
    val activeThreadId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val showHistory: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val activeThreadId = MutableStateFlow<String?>(null)
    private val showHistory = MutableStateFlow(false)

    val uiState: StateFlow<OrdiChatUiState> =
        combine(
            chatRepository.observeThreads(),
            activeThreadId,
            showHistory,
        ) { threads, activeId, history -> Triple(threads, activeId, history) }
            .flatMapLatest { (threads, activeId, history) ->
                val messagesFlow = activeId
                    ?.let(chatRepository::observeMessages)
                    ?: flowOf(emptyList())
                combine(messagesFlow, flowOf(threads)) { messages, _ ->
                    OrdiChatUiState(
                        threads = threads,
                        activeThreadId = activeId,
                        messages = messages,
                        showHistory = history,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdiChatUiState())

    fun openThread(threadId: String) {
        activeThreadId.value = threadId
        showHistory.value = false
    }

    fun newThread() {
        activeThreadId.value = null
        showHistory.value = false
    }

    fun toggleHistory() {
        showHistory.value = !showHistory.value
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val threadId = activeThreadId.value
                ?: chatRepository.createThread(trimmed).also { activeThreadId.value = it }
            chatRepository.send(threadId, trimmed)
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            chatRepository.deleteThread(threadId)
            if (activeThreadId.value == threadId) activeThreadId.value = null
        }
    }
}
