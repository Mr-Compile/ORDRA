package com.ordra.feature.cod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.components.OrderSwipeAction
import com.ordra.core.ui.components.SwipeableOrderCard
import com.ordra.domain.model.Order
import com.ordra.feature.orders.OrderActionEffects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodOverviewScreen(
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: CodViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    OrderActionEffects(
        events = viewModel.actionEvents,
        snackbarHostState = snackbarHostState,
        onUndo = viewModel::undoOrderAction,
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cod_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.loaded && uiState.outstanding.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.cod_empty),
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
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.cod_outstanding_total),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                MoneyFormat.formatMinor(
                                    uiState.outstandingTotalMinor,
                                    uiState.currency,
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                }

                if (uiState.dueToday.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.cod_today)) }
                    items(uiState.dueToday, key = { "t_${it.id}" }) {
                        CodRow(it, onOpenOrder, viewModel::performOrderAction, Modifier.animateItem())
                    }
                }
                if (uiState.nextThreeDays.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.cod_next_3_days)) }
                    items(uiState.nextThreeDays, key = { "n_${it.id}" }) {
                        CodRow(it, onOpenOrder, viewModel::performOrderAction, Modifier.animateItem())
                    }
                }
                if (uiState.outstanding.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.cod_all_pending)) }
                    items(uiState.outstanding, key = { "a_${it.id}" }) {
                        CodRow(it, onOpenOrder, viewModel::performOrderAction, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun CodRow(
    order: Order,
    onOpenOrder: (String) -> Unit,
    onOrderAction: (Order, OrderSwipeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeableOrderCard(
        order = order,
        onClick = { onOpenOrder(order.id) },
        onAction = { onOrderAction(order, it) },
        modifier = modifier,
    )
}
