package com.ordra.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.platformLabel
import com.ordra.domain.model.Platform
import com.ordra.domain.repository.OrderRepository
import com.ordra.domain.service.SpendingAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val thisMonth: Long = 0,
    val thisWeek: Long = 0,
    val totalSpend: Long = 0,
    val pendingSpend: Long = 0,
    val deliveredSpend: Long = 0,
    val codOutstanding: Long = 0,
    val averageOrder: Long = 0,
    val orderCount: Int = 0,
    val currency: String = "PHP",
    val platformTotals: List<Pair<Platform, Long>> = emptyList(),
    val monthly: List<Pair<YearMonth, Long>> = emptyList(),
    val loaded: Boolean = false,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    orderRepository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> =
        orderRepository.observeOrders(com.ordra.domain.model.OrderQuery(includeArchived = true))
            .map { orders ->
                val now = YearMonth.now()
                AnalyticsUiState(
                    thisMonth = SpendingAnalytics.monthlyTotals(orders)
                        .firstOrNull { it.first == now }?.second ?: 0,
                    thisWeek = SpendingAnalytics.weeklyTotal(orders),
                    totalSpend = SpendingAnalytics.totalSpend(orders),
                    pendingSpend = SpendingAnalytics.pendingSpend(orders),
                    deliveredSpend = SpendingAnalytics.deliveredSpend(orders),
                    codOutstanding = SpendingAnalytics.codOutstanding(orders),
                    averageOrder = SpendingAnalytics.averageOrderValue(orders),
                    orderCount = orders.count {
                        it.status !in setOf(
                            com.ordra.domain.model.OrderStatus.CANCELLED,
                            com.ordra.domain.model.OrderStatus.REFUNDED,
                        )
                    },
                    currency = orders.firstOrNull()?.currencyCode ?: "PHP",
                    platformTotals = SpendingAnalytics.platformTotals(orders)
                        .entries.sortedByDescending { it.value }
                        .map { it.key to it.value },
                    monthly = SpendingAnalytics.monthlyTotals(orders).take(6),
                    loaded = true,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(
                        stringResource(R.string.analytics_this_month),
                        MoneyFormat.formatMinor(state.thisMonth, state.currency),
                        Modifier.weight(1f),
                    )
                    MetricCard(
                        stringResource(R.string.analytics_this_week),
                        MoneyFormat.formatMinor(state.thisWeek, state.currency),
                        Modifier.weight(1f),
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(
                        stringResource(R.string.analytics_total),
                        MoneyFormat.formatMinor(state.totalSpend, state.currency),
                        Modifier.weight(1f),
                    )
                    MetricCard(
                        stringResource(R.string.analytics_avg),
                        MoneyFormat.formatMinor(state.averageOrder, state.currency),
                        Modifier.weight(1f),
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(
                        stringResource(R.string.analytics_pending),
                        MoneyFormat.formatMinor(state.pendingSpend, state.currency),
                        Modifier.weight(1f),
                    )
                    MetricCard(
                        stringResource(R.string.analytics_cod),
                        MoneyFormat.formatMinor(state.codOutstanding, state.currency),
                        Modifier.weight(1f),
                    )
                }
            }

            item {
                SectionHeader(stringResource(R.string.analytics_by_platform))
            }
            val maxPlatform = state.platformTotals.maxOfOrNull { it.second } ?: 1L
            state.platformTotals.forEach { (platform, amount) ->
                item(key = "p_$platform") {
                    BreakdownRow(
                        label = platformLabel(platform),
                        amount = amount,
                        max = maxPlatform,
                        currency = state.currency,
                    )
                }
            }
            if (state.platformTotals.isEmpty() && state.loaded) {
                item {
                    Text(
                        stringResource(R.string.analytics_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionHeader(stringResource(R.string.analytics_monthly))
            }
            val maxMonth = state.monthly.maxOfOrNull { it.second } ?: 1L
            state.monthly.forEach { (month, amount) ->
                item(key = "m_$month") {
                    BreakdownRow(
                        label = month.month.getDisplayName(
                            TextStyle.FULL, Locale.getDefault(),
                        ) + " " + month.year,
                        amount = amount,
                        max = maxMonth,
                        currency = state.currency,
                    )
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
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Long, max: Long, currency: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                MoneyFormat.formatMinor(amount, currency),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LinearProgressIndicator(
            progress = { if (max > 0) amount.toFloat() / max else 0f },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}
