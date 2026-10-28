package com.ordra.feature.orders

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.ui.ai.CompanionAvatar
import com.ordra.core.ui.ai.CompanionState
import com.ordra.core.ui.ai.CompanionVariant
import com.ordra.core.ui.components.DeliveredCelebrationOverlay
import com.ordra.core.ui.components.SkeletonList
import com.ordra.core.ui.components.SwipeableOrderCard
import com.ordra.core.ui.orderStatusLabel
import com.ordra.core.ui.platformLabel
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderQuery
import com.ordra.domain.model.OrderSort
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.Platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filtersOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var actionOrder by remember { mutableStateOf<Order?>(null) }

    var showDelivered by remember { mutableStateOf(false) }

    OrderActionEffects(
        events = viewModel.actionEvents,
        snackbarHostState = snackbarHostState,
        onUndo = viewModel::undoOrderAction,
        onDelivered = { showDelivered = true },
    )

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.orders_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.orders_sort),
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                    ) {
                        OrderSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sortLabel(sort)) },
                                onClick = {
                                    viewModel.updateQuery { it.copy(sort = sort) }
                                    sortMenuOpen = false
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query.text,
                onValueChange = { v -> viewModel.updateQuery { it.copy(text = v) } },
                placeholder = { Text(stringResource(R.string.orders_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.text.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateQuery { it.copy(text = "") } }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = uiState.query.hasActiveFilters,
                    onClick = { filtersOpen = true },
                    label = {
                        Text(
                            if (uiState.query.activeFilterCount > 0) {
                                stringResource(R.string.orders_filters_count, uiState.query.activeFilterCount)
                            } else {
                                stringResource(R.string.orders_filters)
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    },
                )
                if (uiState.query.hasActiveFilters) {
                    TextButton(onClick = viewModel::resetQuery) {
                        Text(stringResource(R.string.orders_clear_all))
                    }
                }
            }

            when {
                !uiState.loaded -> SkeletonList()
                uiState.isEmpty -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CompanionAvatar(
                        state = CompanionState.EMPTY,
                        variant = CompanionVariant.EMPTY_STATE,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(
                            if (uiState.query.hasActiveFilters) R.string.orders_no_results
                            else R.string.home_empty_title,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        SwipeableOrderCard(
                            order = order,
                            onClick = { onOpenOrder(order.id) },
                            onAction = { viewModel.performOrderAction(order, it) },
                            onLongClick = { actionOrder = order },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
    if (showDelivered) {
        DeliveredCelebrationOverlay(onFinished = { showDelivered = false })
    }
    }

    actionOrder?.let { order ->
        OrderActionsSheet(
            order = order,
            onDismiss = { actionOrder = null },
            onOpen = { onOpenOrder(order.id) },
            onAction = { viewModel.performOrderAction(order, it) },
        )
    }

    if (filtersOpen) {
        FilterDialog(
            query = uiState.query,
            categories = uiState.categories.map { it.id to it.name },
            onQueryChange = viewModel::updateQuery,
            onDismiss = { filtersOpen = false },
        )
    }
}

@Composable
private fun sortLabel(sort: OrderSort): String = when (sort) {
    OrderSort.NEWEST -> stringResource(R.string.sort_newest)
    OrderSort.OLDEST -> stringResource(R.string.sort_oldest)
    OrderSort.DELIVERY_SOONEST -> stringResource(R.string.sort_delivery)
    OrderSort.HIGHEST_TOTAL -> stringResource(R.string.sort_highest)
    OrderSort.LOWEST_TOTAL -> stringResource(R.string.sort_lowest)
}

@Composable
private fun FilterDialog(
    query: OrderQuery,
    categories: List<Pair<String, String>>,
    onQueryChange: ((OrderQuery) -> OrderQuery) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.orders_filters)) },
        text = {
            LazyColumn {
                item {
                    Text(
                        stringResource(R.string.orders_filter_platform),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Platform.entries.forEach { platform ->
                        CheckRow(
                            label = platformLabel(platform),
                            checked = platform in query.platforms,
                            onToggle = {
                                onQueryChange {
                                    it.copy(platforms = it.platforms.toggle(platform))
                                    }
                            },
                        )
                    }
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        stringResource(R.string.orders_filter_status),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    OrderStatus.entries.forEach { status ->
                        CheckRow(
                            label = orderStatusLabel(status),
                            checked = status in query.statuses,
                            onToggle = {
                                onQueryChange {
                                    it.copy(statuses = it.statuses.toggle(status))
                                }
                            },
                        )
                    }
                    if (categories.isNotEmpty()) {
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            stringResource(R.string.orders_filter_category),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        categories.forEach { (id, name) ->
                            CheckRow(
                                label = name,
                                checked = id in query.categoryIds,
                                onToggle = {
                                    onQueryChange {
                                        it.copy(categoryIds = it.categoryIds.toggle(id))
                                    }
                                },
                            )
                        }
                    }
                    Spacer(Modifier.padding(4.dp))
                    CheckRow(
                        label = stringResource(R.string.orders_filter_cod),
                        checked = query.codOnly,
                        onToggle = { onQueryChange { it.copy(codOnly = !it.codOnly) } },
                    )
                    CheckRow(
                        label = stringResource(R.string.orders_filter_archived),
                        checked = query.includeArchived,
                        onToggle = { onQueryChange { it.copy(includeArchived = !it.includeArchived) } },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.orders_done)) }
        },
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> =
    if (item in this) this - item else this + item
