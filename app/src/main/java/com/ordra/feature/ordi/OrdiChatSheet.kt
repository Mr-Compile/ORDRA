package com.ordra.feature.ordi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.ui.ai.CompanionAvatar
import com.ordra.core.ui.ai.CompanionState
import com.ordra.domain.model.ChatMessage
import com.ordra.domain.model.ChatRole
import com.ordra.domain.model.ChatThread

private val QUICK_PROMPTS = listOf(
    "What's arriving?",
    "COD to prepare",
    "Anything late?",
    "Detected updates",
    "What have I spent?",
)

/**
 * Ordi chat sheet — opened from the floating chat head. Answers are generated
 * locally from real order data only; no network involved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdiChatSheet(
    onDismiss: () -> Unit,
    viewModel: OrdiChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(560.dp),
        ) {
            ChatHeader(
                showingHistory = uiState.showHistory,
                onToggleHistory = viewModel::toggleHistory,
                onNewChat = viewModel::newThread,
            )
            if (uiState.showHistory) {
                ThreadList(
                    threads = uiState.threads,
                    onOpen = viewModel::openThread,
                    onDelete = viewModel::deleteThread,
                )
            } else {
                MessageList(
                    messages = uiState.messages,
                    modifier = Modifier.weight(1f),
                    onQuickPrompt = viewModel::send,
                )
                InputRow(onSend = viewModel::send)
            }
        }
    }
}

@Composable
private fun ChatHeader(
    showingHistory: Boolean,
    onToggleHistory: () -> Unit,
    onNewChat: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanionAvatar(state = CompanionState.HAPPY, size = 36.dp, animated = false)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.ordi_chat_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.ordi_chat_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onToggleHistory) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.ordi_chat_history),
                tint = if (showingHistory) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onNewChat) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.ordi_new_chat),
            )
        }
    }
}

@Composable
private fun ThreadList(
    threads: List<ChatThread>,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (threads.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.ordi_no_chats),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(threads, key = { it.id }) { thread ->
            Surface(
                onClick = { onOpen(thread.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(thread.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            thread.updatedAt.toString().take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onDelete(thread.id) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.ordi_delete_chat),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    modifier: Modifier,
    onQuickPrompt: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OrdiBubble(
                text = stringResource(R.string.ordi_greeting),
            )
        }
        if (messages.isEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QUICK_PROMPTS) { prompt ->
                        Surface(
                            onClick = { onQuickPrompt(prompt) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                prompt,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
        }
        items(messages, key = { it.id }) { msg ->
            when (msg.role) {
                ChatRole.USER -> UserBubble(msg.text)
                ChatRole.ORDI -> OrdiBubble(msg.text)
            }
        }
    }
}

@Composable
private fun OrdiBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        CompanionAvatar(
            state = CompanionState.IDLE,
            size = 28.dp,
            animated = false,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(
                text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(
                text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun InputRow(onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    fun submit() {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            onSend(trimmed)
            text = ""
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ordi_input_hint)) },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = ::submit) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.ordi_send),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
