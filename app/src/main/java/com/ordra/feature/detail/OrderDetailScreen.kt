package com.ordra.feature.detail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.components.SkeletonList
import com.ordra.core.ui.components.StatusChip
import com.ordra.core.ui.courierLabel
import com.ordra.core.ui.orderStatusLabel
import com.ordra.core.ui.paymentMethodLabel
import com.ordra.core.ui.paymentStatusLabel
import com.ordra.core.ui.platformLabel
import com.ordra.core.ui.shipmentStatusLabel
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.TrackingStatusSource
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val DATE_TIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")

private val TERMINAL = setOf(
    OrderStatus.DELIVERED,
    OrderStatus.CANCELLED,
    OrderStatus.RETURNED,
    OrderStatus.REFUNDED,
)

@Composable
fun OrderDetailScreen(
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    onViewTimeline: (String) -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeliveredDialog by remember { mutableStateOf(false) }
    var showStatusPicker by remember { mutableStateOf(false) }
    var showShipmentPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OrderDetailEffect.Deleted -> onNavigateBack()
                is OrderDetailEffect.OpenUrl ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
            }
        }
    }

    val order = uiState.order

    Scaffold(
        topBar = {
            DetailTopBar(
                isArchived = order?.isArchived == true,
                onBack = onNavigateBack,
                onEdit = order?.let { { onEdit(it.id) } },
                onArchive = viewModel::archive,
                onRestore = viewModel::restore,
                onDelete = { showDeleteDialog = true },
            )
        },
    ) { padding ->
        when {
            !uiState.loaded -> SkeletonList(modifier = Modifier.padding(padding))
            order == null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.detail_not_found),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            else -> DetailContent(
                order = order,
                shipment = uiState.shipment,
                uiState = uiState,
                onViewTimeline = onViewTimeline,
                onUpdateOrderStatus = { showStatusPicker = true },
                onUpdateShipment = { showShipmentPicker = true },
                onTrackExternally = viewModel::trackExternally,
                onMarkDelivered = { showDeliveredDialog = true },
                onRating = viewModel::setRating,
                onFavorite = viewModel::toggleFavorite,
                onAddReturn = viewModel::addReturn,
                onAddRefund = viewModel::addRefund,
                onAddWarranty = viewModel::addWarranty,
                onAddReceipt = viewModel::addReceipt,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showDeliveredDialog) {
        AlertDialog(
            onDismissRequest = { showDeliveredDialog = false },
            title = { Text(stringResource(R.string.detail_mark_delivered_title)) },
            text = { Text(stringResource(R.string.detail_mark_delivered_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeliveredDialog = false
                    viewModel.setOrderStatus(OrderStatus.DELIVERED)
                }) { Text(stringResource(R.string.detail_mark_delivered)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveredDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showStatusPicker && order != null) {
        AlertDialog(
            onDismissRequest = { showStatusPicker = false },
            title = { Text(stringResource(R.string.detail_update_status)) },
            text = {
                Column {
                    OrderStatus.entries.forEach { status ->
                        TextButton(
                            onClick = {
                                showStatusPicker = false
                                viewModel.setOrderStatus(status)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(orderStatusLabel(status)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showShipmentPicker) {
        AlertDialog(
            onDismissRequest = { showShipmentPicker = false },
            title = { Text(stringResource(R.string.detail_update_shipment)) },
            text = {
                Column {
                    ShipmentStatus.entries.forEach { status ->
                        TextButton(
                            onClick = {
                                showShipmentPicker = false
                                viewModel.setShipmentStatus(status)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(shipmentStatusLabel(status)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShipmentPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.editor_delete_title)) },
            text = { Text(stringResource(R.string.editor_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) {
                    Text(
                        stringResource(R.string.editor_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
    isArchived: Boolean,
    onBack: () -> Unit,
    onEdit: (() -> Unit)?,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_cancel),
                )
            }
        },
        actions = {
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.detail_edit))
                }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    if (isArchived) R.string.editor_restore else R.string.editor_archive,
                                ),
                            )
                        },
                        onClick = {
                            menuOpen = false
                            if (isArchived) onRestore() else onArchive()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.editor_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun DetailContent(
    order: Order,
    shipment: Shipment?,
    uiState: OrderDetailUiState,
    onViewTimeline: (String) -> Unit,
    onUpdateOrderStatus: () -> Unit,
    onUpdateShipment: () -> Unit,
    onTrackExternally: () -> Unit,
    onMarkDelivered: () -> Unit,
    onRating: (Int?) -> Unit,
    onFavorite: () -> Unit,
    onAddReturn: (String?) -> Unit,
    onAddRefund: (Long?, String?) -> Unit,
    onAddWarranty: (String?, java.time.LocalDate, java.time.LocalDate) -> Unit,
    onAddReceipt: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(order.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        listOfNotNull(
                            platformLabel(order.platform, order.customPlatformName),
                            order.sellerName?.takeIf { it.isNotBlank() },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        StatusChip(order.status)
                        Spacer(Modifier.weight(1f))
                        order.totalAmountMinor?.let {
                            Text(
                                MoneyFormat.formatMinor(it, order.currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    if (order.status !in TERMINAL) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onUpdateOrderStatus) {
                                Text(stringResource(R.string.detail_update_status))
                            }
                            OutlinedButton(onClick = onMarkDelivered) {
                                Text(stringResource(R.string.detail_mark_delivered))
                            }
                        }
                    }
                }
            }
        }

        item {
            DetailSection(title = stringResource(R.string.detail_section_shipment)) {
                if (shipment == null) {
                    Text(
                        stringResource(R.string.detail_no_shipment),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            shipmentStatusLabel(shipment.shipmentStatus),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (shipment.statusSource == TrackingStatusSource.MANUAL) {
                                stringResource(R.string.detail_detected_manual)
                            } else {
                                shipment.lastDetectedAt
                                    ?.atZone(ZoneId.systemDefault())
                                    ?.format(DATE_TIME_FMT)
                                    ?.let { stringResource(R.string.timeline_detected, it) }
                                    ?: ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DetailRow(
                        stringResource(R.string.editor_courier),
                        courierLabel(shipment.courier, shipment.customCourierName),
                    )
                    shipment.trackingNumber?.let {
                        DetailRow(stringResource(R.string.editor_tracking), it)
                    }
                    order.estimatedDeliveryStart?.let { start ->
                        val end = order.estimatedDeliveryEnd
                        DetailRow(
                            stringResource(R.string.editor_eta_start),
                            if (end != null && end != start) {
                                "${start.format(DATE_FMT)} – ${end.format(DATE_FMT)}"
                            } else {
                                start.format(DATE_FMT)
                            },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onUpdateShipment) {
                            Text(stringResource(R.string.detail_update_shipment))
                        }
                        if (shipment.trackingNumber != null || shipment.courier != com.ordra.domain.model.Courier.UNKNOWN) {
                            OutlinedButton(onClick = onTrackExternally) {
                                Text(stringResource(R.string.detail_track_external))
                            }
                        }
                    }
                    OutlinedButton(onClick = { onViewTimeline(shipment.id) }) {
                        Text(stringResource(R.string.detail_view_timeline))
                    }
                }
            }
        }

        if (order.items.isNotEmpty()) {
            item {
                DetailSection(title = stringResource(R.string.detail_section_items)) {
                    order.items.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                item.variant?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(
                                stringResource(R.string.detail_qty_price, "", item.quantity).trim(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            item.lineTotalMinor?.let {
                                Spacer(Modifier.padding(4.dp))
                                Text(
                                    MoneyFormat.formatMinor(it, order.currencyCode),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        if (index < order.items.lastIndex) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    order.itemSubtotalMinor?.let {
                        DetailRow(
                            stringResource(R.string.detail_subtotal),
                            MoneyFormat.formatMinor(it, order.currencyCode),
                        )
                    }
                    order.shippingFeeMinor?.let {
                        DetailRow(
                            stringResource(R.string.editor_shipping),
                            MoneyFormat.formatMinor(it, order.currencyCode),
                        )
                    }
                    order.discountMinor?.let {
                        DetailRow(
                            stringResource(R.string.editor_discount),
                            "-${MoneyFormat.formatMinor(it, order.currencyCode)}",
                        )
                    }
                    order.totalAmountMinor?.let {
                        DetailRow(
                            stringResource(R.string.editor_total),
                            MoneyFormat.formatMinor(it, order.currencyCode),
                        )
                    }
                }
            }
        }

        item {
            DetailSection(title = stringResource(R.string.detail_section_payment)) {
                DetailRow(
                    stringResource(R.string.editor_payment_method),
                    paymentMethodLabel(order.paymentMethod),
                )
                DetailRow(
                    stringResource(R.string.editor_payment_status),
                    paymentStatusLabel(order.paymentStatus),
                )
                if (order.isCod && order.totalAmountMinor != null) {
                    Text(
                        stringResource(
                            R.string.editor_cod_note,
                            MoneyFormat.formatMinor(order.totalAmountMinor, order.currencyCode),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                order.orderedDate?.let {
                    DetailRow(
                        stringResource(R.string.editor_ordered_date),
                        it.atZone(ZoneId.systemDefault()).format(DATE_FMT),
                    )
                }
            }
        }

        if (!order.notes.isNullOrBlank()) {
            item {
                DetailSection(title = stringResource(R.string.detail_section_notes)) {
                    Text(order.notes.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            PostPurchaseSection(
                state = uiState,
                onRating = onRating,
                onFavorite = onFavorite,
                onAddReturn = onAddReturn,
                onAddRefund = onAddRefund,
                onAddWarranty = onAddWarranty,
                onAddReceipt = onAddReceipt,
            )
        }
    }
}

@Composable
private fun PostPurchaseSection(
    state: OrderDetailUiState,
    onRating: (Int?) -> Unit,
    onFavorite: () -> Unit,
    onAddReturn: (String?) -> Unit,
    onAddRefund: (Long?, String?) -> Unit,
    onAddWarranty: (String?, java.time.LocalDate, java.time.LocalDate) -> Unit,
    onAddReceipt: (String) -> Unit,
) {
    var dialog by remember { mutableStateOf<String?>(null) }
    val receiptPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { onAddReceipt(it.toString()) } }

    DetailSection(title = stringResource(R.string.pp_title)) {
        // Rating stars + favorite
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { i ->
                TextButton(onClick = { onRating(if (state.order?.rating == i + 1) null else i + 1) }) {
                    Text(
                        if ((state.order?.rating ?: 0) > i) "★" else "☆",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.FilterChip(
                selected = state.order?.isFavorite == true,
                onClick = onFavorite,
                label = { Text(stringResource(R.string.pp_favorite)) },
            )
        }

        if (state.returns.isNotEmpty() || state.refunds.isNotEmpty() ||
            state.warranties.isNotEmpty() || state.receipts.isNotEmpty()
        ) {
            state.returns.forEach {
                Text(
                    stringResource(R.string.pp_return_line, it.status.name.lowercase()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            state.refunds.forEach {
                Text(
                    stringResource(
                        R.string.pp_refund_line,
                        MoneyFormat.formatMinor(it.amountMinor, "PHP"),
                        it.status.name.lowercase(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            state.warranties.forEach {
                Text(
                    stringResource(
                        R.string.pp_warranty_line,
                        it.provider ?: "—", it.startDate, it.endDate,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (state.receipts.isNotEmpty()) {
                Text(
                    stringResource(R.string.pp_receipts_count, state.receipts.size),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { dialog = "return" }) {
                Text(stringResource(R.string.pp_add_return))
            }
            TextButton(onClick = { dialog = "refund" }) {
                Text(stringResource(R.string.pp_add_refund))
            }
            TextButton(onClick = { dialog = "warranty" }) {
                Text(stringResource(R.string.pp_add_warranty))
            }
            TextButton(
                onClick = {
                    receiptPicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts
                                .PickVisualMedia.ImageOnly,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.pp_add_receipt)) }
        }
    }

    when (dialog) {
        "return" -> TextDialog(
            title = stringResource(R.string.pp_add_return),
            label = stringResource(R.string.pp_reason),
            onConfirm = { onAddReturn(it); dialog = null },
            onDismiss = { dialog = null },
        )
        "refund" -> TextDialog(
            title = stringResource(R.string.pp_add_refund),
            label = stringResource(R.string.pp_amount),
            secondLabel = stringResource(R.string.pp_reason),
            onConfirm2 = { amount, reason ->
                onAddRefund(MoneyFormat.parseToMinor(amount), reason.ifBlank { null })
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        "warranty" -> WarrantyDialog(
            onConfirm = { p, s, e -> onAddWarranty(p, s, e); dialog = null },
            onDismiss = { dialog = null },
        )
    }
}

@Composable
private fun TextDialog(
    title: String,
    label: String,
    onConfirm: (String) -> Unit = {},
    secondLabel: String? = null,
    onConfirm2: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    label = { Text(label) }, singleLine = true,
                )
                if (secondLabel != null) {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = second, onValueChange = { second = it },
                        label = { Text(secondLabel) }, singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (secondLabel != null) onConfirm2(text, second) else onConfirm(text)
                },
            ) { Text(stringResource(R.string.pp_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun WarrantyDialog(
    onConfirm: (String?, java.time.LocalDate, java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var provider by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pp_add_warranty)) },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = provider, onValueChange = { provider = it },
                    label = { Text(stringResource(R.string.pp_provider)) }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = start, onValueChange = { start = it },
                    label = { Text(stringResource(R.string.pp_start_date)) }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = end, onValueChange = { end = it },
                    label = { Text(stringResource(R.string.pp_end_date)) }, singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val s = runCatching { java.time.LocalDate.parse(start) }.getOrNull()
                    val e = runCatching { java.time.LocalDate.parse(end) }.getOrNull()
                    if (s != null && e != null) onConfirm(provider.ifBlank { null }, s, e)
                },
            ) { Text(stringResource(R.string.pp_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
