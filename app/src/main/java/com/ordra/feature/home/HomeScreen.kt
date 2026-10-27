package com.ordra.feature.home

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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.components.OrderCard
import com.ordra.domain.model.Order
import java.time.format.DateTimeFormatter

private val ETA_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
private fun etaText(order: Order): String? {
    val start = order.estimatedDeliveryStart
    val end = order.estimatedDeliveryEnd
    return when {
        start != null && end != null && start != end ->
            stringResource(R.string.home_eta_range, start.format(ETA_FMT), end.format(ETA_FMT))
        start != null || end != null ->
            stringResource(R.string.home_eta_single, (start ?: end)!!.format(ETA_FMT))
        else -> null
    }
}

@Composable
fun HomeScreen(
    onAddOrder: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCod: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeTopBar(
                showingArchived = uiState.showingArchived,
                onToggleArchived = viewModel::toggleArchived,
                onOpenOrders = onOpenOrders,
                onOpenSettings = onOpenSettings,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddOrder) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_add_order),
                )
            }
        },
    ) { innerPadding ->
        if (uiState.showingArchived) {
            ArchivedContent(
                orders = uiState.archived,
                onOpenOrder = onOpenOrder,
                modifier = Modifier.padding(innerPadding),
            )
        } else if (uiState.isEmpty && uiState.loaded) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            HomeContent(
                state = uiState,
                onOpenOrder = onOpenOrder,
                onOpenCod = onOpenCod,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    showingArchived: Boolean,
    onToggleArchived: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                stringResource(
                    if (showingArchived) R.string.home_archived_heading else R.string.app_name,
                ),
            )
        },
        actions = {
            IconButton(onClick = onOpenOrders) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.home_orders),
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.home_settings),
                )
            }
            IconButton(onClick = onToggleArchived) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(
                        if (showingArchived) R.string.home_show_active else R.string.home_show_archived,
                    ),
                )
            }
        },
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenOrder: (String) -> Unit,
    onOpenCod: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SummaryRow(state = state, onOpenCod = onOpenCod)
            Spacer(Modifier.height(8.dp))
        }
        if (state.upcoming.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_section_upcoming),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(state.upcoming, key = { "up_${it.id}" }) { order ->
                OrderCard(order = order, onClick = { onOpenOrder(order.id) }, etaText = etaText(order))
            }
        }
        if (state.recent.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_section_recent),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(state.recent, key = { "re_${it.id}" }) { order ->
                OrderCard(order = order, onClick = { onOpenOrder(order.id) }, etaText = etaText(order))
            }
        }
    }
}

@Composable
private fun SummaryRow(state: HomeUiState, onOpenCod: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard(
            label = stringResource(R.string.home_metric_active),
            value = state.activeCount.toString(),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = stringResource(R.string.home_metric_arriving),
            value = state.arrivingSoonCount.toString(),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = stringResource(R.string.home_metric_cod),
            value = MoneyFormat.formatMinor(state.codToPrepareMinor ?: 0L, state.codCurrency)
                .ifBlank { "0" },
            modifier = Modifier.weight(1f),
            onClick = onOpenCod,
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ArchivedContent(
    orders: List<Order>,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (orders.isEmpty()) {
        EmptyState(showingArchived = true, modifier = modifier)
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(orders, key = { it.id }) { order ->
            OrderCard(order = order, onClick = { onOpenOrder(order.id) })
        }
    }
}

@Composable
private fun EmptyState(showingArchived: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.ordra_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(
                        if (showingArchived) R.string.home_archived_empty else R.string.home_empty_title,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (!showingArchived) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
