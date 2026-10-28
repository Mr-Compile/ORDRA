package com.ordra.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ordra.R
import com.ordra.core.ui.components.OrderCard
import com.ordra.domain.model.Order
import com.ordra.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CalendarUiState(
    val ordersByDay: Map<LocalDate, List<Order>> = emptyMap(),
    val month: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate = LocalDate.now(),
)

@HiltViewModel
class CalendarViewModel @Inject constructor(orderRepository: OrderRepository) : ViewModel() {
    val monthAnchor: YearMonth = YearMonth.now()
    private val month = MutableStateFlow(monthAnchor)
    private val selected = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        orderRepository.observeActiveOrders(), month, selected,
    ) { orders, m, sel ->
        val byDay = mutableMapOf<LocalDate, MutableList<Order>>()
        orders.forEach { o ->
            listOfNotNull(o.estimatedDeliveryStart, o.estimatedDeliveryEnd).distinct().forEach { d ->
                byDay.getOrPut(d) { mutableListOf() }.add(o)
            }
        }
        CalendarUiState(byDay, m, sel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun selectDay(d: LocalDate) { selected.value = d }
    fun setMonth(m: YearMonth) { month.value = m }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryCalendarScreen(
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val anchor = viewModel.monthAnchor
    val pagerState = rememberPagerState(
        initialPage = CALENDAR_CENTER_PAGE,
    ) { CALENDAR_PAGER_COUNT }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            viewModel.setMonth(monthForPage(anchor, page))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.calendar_prev_month),
                    )
                }
                Text(
                    state.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                        " " + state.month.year,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.calendar_next_month),
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(260.dp),
            ) { page ->
                MonthGrid(
                    month = monthForPage(anchor, page),
                    ordersByDay = state.ordersByDay,
                    selectedDay = state.selectedDay,
                    onSelectDay = viewModel::selectDay,
                )
            }
            Spacer(Modifier.height(8.dp))
            val dayOrders = state.ordersByDay[state.selectedDay].orEmpty()
            if (dayOrders.isEmpty()) {
                Text(
                    stringResource(R.string.calendar_empty_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOpenOrder(order.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    ordersByDay: Map<LocalDate, List<Order>>,
    selectedDay: LocalDate,
    onSelectDay: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val offset = (first.dayOfWeek.value - 1)
    val days = (1 - offset)..month.lengthOfMonth()
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(260.dp),
        userScrollEnabled = false,
    ) {
        items(days.toList()) { day ->
            val date = month.atDay(day.coerceIn(1, month.lengthOfMonth()))
            val inMonth = day in 1..month.lengthOfMonth()
            val hasOrders = inMonth && ordersByDay.containsKey(date)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clickable(enabled = inMonth) { onSelectDay(date) },
                contentAlignment = Alignment.Center,
            ) {
                if (inMonth) {
                    Surface(
                        color = if (date == selectedDay) {
                            MaterialTheme.colorScheme.primary
                        } else if (hasOrders) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                day.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (date == selectedDay) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else MaterialTheme.colorScheme.onSurface,
                            )
                            if (hasOrders) {
                                Text(
                                    "•",
                                    color = if (date == selectedDay) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
