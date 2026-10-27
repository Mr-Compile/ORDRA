package com.ordra.feature.detections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ordra.R
import com.ordra.domain.model.DetectionSuggestion
import com.ordra.domain.model.Order
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetectionsUiState(
    val pending: List<DetectionSuggestion> = emptyList(),
    val orders: List<Order> = emptyList(),
    val loaded: Boolean = false,
)

@HiltViewModel
class DetectionsViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository,
    orderRepository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<DetectionsUiState> = combine(
        detectionRepository.observePending(),
        orderRepository.observeActiveOrders(),
    ) { pending, orders -> DetectionsUiState(pending, orders, true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetectionsUiState())

    fun confirm(id: String, orderId: String? = null) = viewModelScope.launch {
        detectionRepository.confirm(id, orderId)
    }

    fun dismiss(id: String) = viewModelScope.launch {
        detectionRepository.dismiss(id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionsScreen(
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: DetectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pickFor by remember { mutableStateOf<DetectionSuggestion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detections_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (state.loaded && state.pending.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.detections_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.pending, key = { it.id }) { s ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                s.status.name.lowercase().replace('_', ' ')
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            s.summary?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(
                                    R.string.detections_meta,
                                    s.confidence.name.lowercase(),
                                    s.detectedAt.atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("MMM d, HH:mm")),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(onClick = { viewModel.dismiss(s.id) }) {
                                    Text(stringResource(R.string.detections_ignore))
                                }
                                if (s.needsParcelPick) {
                                    TextButton(onClick = { pickFor = s }) {
                                        Text(stringResource(R.string.detections_pick))
                                    }
                                } else {
                                    TextButton(onClick = { viewModel.confirm(s.id) }) {
                                        Text(stringResource(R.string.detections_apply))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pickFor?.let { suggestion ->
        AlertDialog(
            onDismissRequest = { pickFor = null },
            title = { Text(stringResource(R.string.detections_pick_title)) },
            text = {
                LazyColumn {
                    items(state.orders, key = { it.id }) { order ->
                        TextButton(
                            onClick = {
                                viewModel.confirm(suggestion.id, order.id)
                                pickFor = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(order.title) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickFor = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
