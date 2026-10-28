package com.ordra.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.ai.CompanionAvatar
import com.ordra.core.ui.ai.CompanionState
import com.ordra.core.ui.ai.CompanionVariant
import com.ordra.core.ui.ai.OrdraAiBanner
import com.ordra.core.ui.components.DeliveredCelebrationOverlay
import com.ordra.core.ui.components.OrderSwipeAction
import com.ordra.core.ui.components.SkeletonList
import com.ordra.core.ui.components.SwipeableOrderCard
import com.ordra.core.ui.theme.OrdraAmber
import com.ordra.core.ui.theme.OrdraIndigo
import com.ordra.core.ui.theme.StatusShipped
import com.ordra.domain.model.Order
import com.ordra.feature.ordi.OrdiChatSheet
import com.ordra.feature.orders.OrderActionEffects
import com.ordra.feature.orders.OrderActionsSheet
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
    onOpenCalendar: () -> Unit,
    onSmartImport: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var chatOpen by rememberSaveable { mutableStateOf(false) }
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
            HomeTopBar(
                showingArchived = uiState.showingArchived,
                onToggleArchived = viewModel::toggleArchived,
                onOpenOrders = onOpenOrders,
                onOpenSettings = onOpenSettings,
                onOpenCalendar = onOpenCalendar,
                onSmartImport = onSmartImport,
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            if (!uiState.loaded) {
                SkeletonList(modifier = Modifier.padding(innerPadding))
            } else if (uiState.showingArchived) {
                ArchivedContent(
                    orders = uiState.archived,
                    onOpenOrder = onOpenOrder,
                    onOrderAction = { order, action ->
                        viewModel.performOrderAction(order, action)
                    },
                    onOrderLongPress = { actionOrder = it },
                    modifier = Modifier.padding(innerPadding),
                )
            } else if (uiState.isEmpty && uiState.loaded) {
                EmptyState(modifier = Modifier.padding(innerPadding))
            } else {
                HomeContent(
                    state = uiState,
                    onOpenOrder = onOpenOrder,
                    onOpenOrders = onOpenOrders,
                    onOpenCod = onOpenCod,
                    onAddOrder = onAddOrder,
                    onOrderAction = { order, action ->
                        viewModel.performOrderAction(order, action)
                    },
                    onOrderLongPress = { actionOrder = it },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            OrdiChatHead(
                onClick = { chatOpen = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(innerPadding)
                    .padding(start = 20.dp, bottom = 8.dp),
            )
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
    if (chatOpen) {
        OrdiChatSheet(onDismiss = { chatOpen = false })
    }
}

/** Floating Ordi chat head — opens the Ordi chat sheet. */
@Composable
private fun OrdiChatHead(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.cd_ordi_chat_head)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = label
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
    ) {
        CompanionAvatar(
            state = CompanionState.IDLE,
            size = 56.dp,
            animated = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    showingArchived: Boolean,
    onToggleArchived: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    onSmartImport: () -> Unit,
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
            IconButton(onClick = onSmartImport) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_scan),
                )
            }
            IconButton(onClick = onOpenCalendar) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.home_calendar),
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
    onOpenOrders: () -> Unit,
    onOpenCod: () -> Unit,
    onAddOrder: () -> Unit,
    onOrderAction: (Order, OrderSwipeAction) -> Unit,
    onOrderLongPress: (Order) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OrdraAiBanner(
                data = state.banner,
                onViewParcels = onOpenOrders,
                onAddOrder = onAddOrder,
            )
            Spacer(Modifier.height(8.dp))
        }
        state.featured?.let { featured ->
            item(key = "featured") {
                FeaturedParcelCard(
                    order = featured,
                    shipment = state.featuredShipment,
                    onClick = { onOpenOrder(featured.id) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
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
                SwipeableOrderCard(
                    order = order,
                    onClick = { onOpenOrder(order.id) },
                    onAction = { onOrderAction(order, it) },
                    etaText = etaText(order),
                    onLongClick = { onOrderLongPress(order) },
                    modifier = Modifier.animateItem(),
                )
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
                SwipeableOrderCard(
                    order = order,
                    onClick = { onOpenOrder(order.id) },
                    onAction = { onOrderAction(order, it) },
                    etaText = etaText(order),
                    onLongClick = { onOrderLongPress(order) },
                    modifier = Modifier.animateItem(),
                )
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
            icon = Icons.Filled.Inventory2,
            iconTint = OrdraIndigo,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = stringResource(R.string.home_metric_arriving),
            value = state.arrivingSoonCount.toString(),
            icon = Icons.Filled.LocalShipping,
            iconTint = StatusShipped,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = stringResource(R.string.home_metric_cod),
            value = MoneyFormat.formatMinor(state.codToPrepareMinor ?: 0L, state.codCurrency)
                .ifBlank { "0" },
            icon = Icons.Filled.Payments,
            iconTint = OrdraAmber,
            modifier = Modifier.weight(1f),
            onClick = onOpenCod,
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
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
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
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
    onOrderAction: (Order, OrderSwipeAction) -> Unit,
    onOrderLongPress: (Order) -> Unit,
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
            SwipeableOrderCard(
                order = order,
                onClick = { onOpenOrder(order.id) },
                onAction = { onOrderAction(order, it) },
                onLongClick = { onOrderLongPress(order) },
                modifier = Modifier.animateItem(),
            )
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
                CompanionAvatar(
                    state = CompanionState.EMPTY,
                    variant = CompanionVariant.EMPTY_STATE,
                )
                Spacer(Modifier.height(12.dp))
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
