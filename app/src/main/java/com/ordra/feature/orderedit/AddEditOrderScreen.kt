package com.ordra.feature.orderedit

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.orderStatusLabel
import com.ordra.core.ui.paymentMethodLabel
import com.ordra.core.ui.paymentStatusLabel
import com.ordra.core.ui.platformLabel
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun AddEditOrderScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditOrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val saveErrorText = stringResource(R.string.editor_saved_error)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddEditEffect.Saved, AddEditEffect.Deleted -> onNavigateBack()
                AddEditEffect.ShowSaveError -> snackbarHostState.showSnackbar(saveErrorText)
            }
        }
    }

    BackHandler(enabled = uiState.step == EditorStep.FORM && uiState.form.isDirty) {
        showDiscardDialog = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditorTopBar(
                isEdit = uiState.form.isEdit,
                isArchived = uiState.isArchived,
                step = uiState.step,
                onBack = {
                    if (uiState.step == EditorStep.REVIEW) viewModel.backToForm()
                    else if (uiState.form.isDirty) showDiscardDialog = true
                    else onNavigateBack()
                },
                onArchive = viewModel::archive,
                onRestore = viewModel::restore,
                onDelete = { showDeleteDialog = true },
            )
        },
        bottomBar = {
            EditorBottomBar(
                step = uiState.step,
                isSaving = uiState.isSaving,
                onPrimary = {
                    if (uiState.step == EditorStep.FORM) viewModel.goToReview()
                    else viewModel.save()
                },
                onSecondary = viewModel::backToForm,
            )
        },
    ) { padding ->
        if (uiState.step == EditorStep.FORM) {
            OrderFormContent(
                state = uiState,
                onFormChange = viewModel::updateForm,
                modifier = Modifier.padding(padding),
            )
        } else {
            OrderReviewContent(
                form = uiState.form,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_body)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onNavigateBack() }) {
                    Text(stringResource(R.string.editor_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.editor_keep_editing))
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
private fun EditorTopBar(
    isEdit: Boolean,
    isArchived: Boolean,
    step: EditorStep,
    onBack: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                if (step == EditorStep.REVIEW) stringResource(R.string.editor_review_title)
                else stringResource(if (isEdit) R.string.editor_title_edit else R.string.editor_title_add),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_cancel),
                )
            }
        },
        actions = {
            if (isEdit && step == EditorStep.FORM) {
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
private fun EditorBottomBar(
    step: EditorStep,
    isSaving: Boolean,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (step == EditorStep.REVIEW) {
            OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.editor_back_to_edit))
            }
            Button(onClick = onPrimary, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.editor_save))
            }
        } else {
            Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.editor_review))
            }
        }
    }
}

@Composable
private fun OrderFormContent(
    state: AddEditUiState,
    onFormChange: ((OrderFormState) -> OrderFormState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    val errors = state.fieldErrors
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                stringResource(R.string.editor_required_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_platform)) {
                OutlinedTextField(
                    value = form.title,
                    onValueChange = { v -> onFormChange { it.copy(title = v) } },
                    label = { Text(stringResource(R.string.editor_order_title)) },
                    isError = errors.containsKey("title"),
                    supportingText = { FieldError(errors["title"]) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                EnumDropdown(
                    label = stringResource(R.string.editor_platform),
                    options = Platform.entries,
                    selected = form.platform,
                    optionLabel = { platformLabel(it) },
                    onSelect = { p -> onFormChange { it.copy(platform = p) } },
                    error = errors["platform"],
                )
                if (form.platform == Platform.OTHER) {
                    OutlinedTextField(
                        value = form.customPlatformName,
                        onValueChange = { v -> onFormChange { it.copy(customPlatformName = v) } },
                        label = { Text(stringResource(R.string.editor_custom_platform)) },
                        isError = errors.containsKey("customPlatformName"),
                        supportingText = { FieldError(errors["customPlatformName"]) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = form.sellerName,
                    onValueChange = { v -> onFormChange { it.copy(sellerName = v) } },
                    label = { Text(stringResource(R.string.editor_seller)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_items)) {
                form.items.forEachIndexed { index, item ->
                    ItemFields(
                        index = index,
                        item = item,
                        errors = errors,
                        onChange = { updated ->
                            onFormChange { f ->
                                f.copy(items = f.items.toMutableList().apply { set(index, updated) })
                            }
                        },
                        onRemove = if (form.items.size > 1) {
                            {
                                onFormChange { f ->
                                    f.copy(items = f.items.toMutableList().apply { removeAt(index) })
                                }
                            }
                        } else null,
                    )
                    if (index < form.items.lastIndex) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
                TextButton(onClick = {
                    onFormChange { it.copy(items = it.items + OrderItemForm()) }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.editor_add_item))
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_pricing)) {
                if (form.itemsSubtotalMinor > 0) {
                    Text(
                        "${stringResource(R.string.editor_items_subtotal)}: " +
                            MoneyFormat.formatMinor(form.itemsSubtotalMinor, form.currencyCode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoneyField(
                        value = form.shipping,
                        label = stringResource(R.string.editor_shipping),
                        error = errors["shipping"],
                        modifier = Modifier.weight(1f),
                        onChange = { v -> onFormChange { it.copy(shipping = v) } },
                    )
                    MoneyField(
                        value = form.discount,
                        label = stringResource(R.string.editor_discount),
                        error = errors["discount"],
                        modifier = Modifier.weight(1f),
                        onChange = { v -> onFormChange { it.copy(discount = v) } },
                    )
                }
                MoneyField(
                    value = form.total,
                    label = stringResource(R.string.editor_total) + " *",
                    error = errors["total"],
                    supporting = if (form.total.isBlank() && form.suggestedTotalMinor != 0L) {
                        stringResource(
                            R.string.editor_total_hint,
                            MoneyFormat.formatMinor(form.suggestedTotalMinor, form.currencyCode),
                        )
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    onChange = { v -> onFormChange { it.copy(total = v) } },
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_payment)) {
                EnumDropdown(
                    label = stringResource(R.string.editor_payment_method),
                    options = PaymentMethod.entries,
                    selected = form.paymentMethod,
                    optionLabel = { paymentMethodLabel(it) },
                    onSelect = { m ->
                        onFormChange {
                            it.copy(
                                paymentMethod = m,
                                paymentStatus = if (m == PaymentMethod.COD && it.paymentStatus == PaymentStatus.UNKNOWN) {
                                    PaymentStatus.UNPAID
                                } else it.paymentStatus,
                            )
                        }
                    },
                )
                EnumDropdown(
                    label = stringResource(R.string.editor_payment_status),
                    options = PaymentStatus.entries,
                    selected = form.paymentStatus,
                    optionLabel = { paymentStatusLabel(it) },
                    onSelect = { s -> onFormChange { it.copy(paymentStatus = s) } },
                )
                if (form.paymentMethod == PaymentMethod.COD) {
                    MoneyFormat.parseToMinor(form.total)?.let { cod ->
                        Text(
                            stringResource(
                                R.string.editor_cod_note,
                                MoneyFormat.formatMinor(cod, form.currencyCode),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_delivery)) {
                EnumDropdown(
                    label = stringResource(R.string.editor_status),
                    options = OrderStatus.entries,
                    selected = form.status,
                    optionLabel = { orderStatusLabel(it) },
                    onSelect = { s -> onFormChange { it.copy(status = s) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = form.courier,
                        onValueChange = { v -> onFormChange { it.copy(courier = v) } },
                        label = { Text(stringResource(R.string.editor_courier)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.trackingNumber,
                        onValueChange = { v -> onFormChange { it.copy(trackingNumber = v) } },
                        label = { Text(stringResource(R.string.editor_tracking)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                DateField(
                    label = stringResource(R.string.editor_ordered_date),
                    value = form.orderedDate,
                    onChange = { d -> onFormChange { it.copy(orderedDate = d) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateField(
                        label = stringResource(R.string.editor_eta_start),
                        value = form.etaStart,
                        onChange = { d -> onFormChange { it.copy(etaStart = d) } },
                        modifier = Modifier.weight(1f),
                    )
                    DateField(
                        label = stringResource(R.string.editor_eta_end),
                        value = form.etaEnd,
                        onChange = { d -> onFormChange { it.copy(etaEnd = d) } },
                        modifier = Modifier.weight(1f),
                    )
                }
                FieldError(errors["etaEnd"])
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_attachment)) {
                ImagePickerField(
                    imageUri = form.productImageUri,
                    onPick = { uri -> onFormChange { it.copy(productImageUri = uri) } },
                    onRemove = { onFormChange { it.copy(productImageUri = null) } },
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_notes)) {
                OutlinedTextField(
                    value = form.notes,
                    onValueChange = { v -> onFormChange { it.copy(notes = v) } },
                    label = { Text(stringResource(R.string.editor_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun FieldError(message: String?) {
    if (message != null) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun MoneyField(
    value: String,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { FieldError(error ?: supporting) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { optionLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = { FieldError(error) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: LocalDate?,
    onChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value?.format(DATE_FMT) ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { dialogOpen = true }) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.editor_pick_date))
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
    if (dialogOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        onChange(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    dialogOpen = false
                }) { Text(stringResource(android.R.string.ok).replaceFirstChar { it.uppercase() }) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onChange(null)
                    dialogOpen = false
                }) { Text(stringResource(R.string.editor_clear_date)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun ImagePickerField(
    imageUri: String?,
    onPick: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onPick(uri.toString())
        }
    }
    if (imageUri == null) {
        OutlinedButton(onClick = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }) {
            Text(stringResource(R.string.editor_pick_image))
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            coil3.compose.AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.editor_image_set),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.editor_remove_image))
            }
        }
    }
}

@Composable
private fun ItemFields(
    index: Int,
    item: OrderItemForm,
    errors: Map<String, String>,
    onChange: (OrderItemForm) -> Unit,
    onRemove: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = item.name,
                onValueChange = { onChange(item.copy(name = it)) },
                label = { Text(stringResource(R.string.editor_item_name)) },
                isError = errors.containsKey("item_name_$index"),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.editor_remove_item))
                }
            }
        }
        FieldError(errors["item_name_$index"])
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.variant,
                onValueChange = { onChange(item.copy(variant = it)) },
                label = { Text(stringResource(R.string.editor_item_variant)) },
                modifier = Modifier.weight(2f),
                singleLine = true,
            )
            OutlinedTextField(
                value = item.quantity,
                onValueChange = { onChange(item.copy(quantity = it.filter { c -> c.isDigit() })) },
                label = { Text(stringResource(R.string.editor_item_qty)) },
                isError = errors.containsKey("item_qty_$index"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = item.unitPrice,
                onValueChange = { onChange(item.copy(unitPrice = it)) },
                label = { Text(stringResource(R.string.editor_item_price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1.5f),
                singleLine = true,
            )
        }
        FieldError(errors["item_qty_$index"])
    }
}

@Composable
private fun OrderReviewContent(form: OrderFormState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FormSection(title = stringResource(R.string.editor_review_title)) {
                ReviewRow(stringResource(R.string.editor_order_title), form.title.ifBlank { "-" })
                ReviewRow(
                    stringResource(R.string.editor_platform),
                    platformLabel(
                        form.platform ?: Platform.OTHER,
                        form.customPlatformName.ifBlank { null },
                    ),
                )
                if (form.sellerName.isNotBlank()) {
                    ReviewRow(stringResource(R.string.editor_seller), form.sellerName)
                }
            }
        }

        val filledItems = form.items.filterNot { it.isCompletelyEmpty }
        if (filledItems.isNotEmpty()) {
            item {
                FormSection(title = stringResource(R.string.editor_section_items)) {
                    filledItems.forEach { item ->
                        val qty = item.quantity.toIntOrNull() ?: 1
                        val price = MoneyFormat.parseToMinor(item.unitPrice)
                        Text(
                            buildString {
                                append(item.name)
                                if (item.variant.isNotBlank()) append(" (${item.variant})")
                                append(" ×$qty")
                                if (price != null) {
                                    append(" — ")
                                    append(MoneyFormat.formatMinor(price * qty, form.currencyCode))
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_pricing)) {
                MoneyFormat.parseToMinor(form.shipping)?.let {
                    ReviewRow(stringResource(R.string.editor_shipping), MoneyFormat.formatMinor(it, form.currencyCode))
                }
                MoneyFormat.parseToMinor(form.discount)?.let {
                    ReviewRow(stringResource(R.string.editor_discount), MoneyFormat.formatMinor(it, form.currencyCode))
                }
                ReviewRow(
                    stringResource(R.string.editor_total),
                    MoneyFormat.formatMinor(MoneyFormat.parseToMinor(form.total), form.currencyCode),
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_payment)) {
                ReviewRow(stringResource(R.string.editor_payment_method), paymentMethodLabel(form.paymentMethod))
                ReviewRow(stringResource(R.string.editor_payment_status), paymentStatusLabel(form.paymentStatus))
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_delivery)) {
                ReviewRow(stringResource(R.string.editor_status), orderStatusLabel(form.status))
                if (form.courier.isNotBlank()) ReviewRow(stringResource(R.string.editor_courier), form.courier)
                if (form.trackingNumber.isNotBlank()) {
                    ReviewRow(stringResource(R.string.editor_tracking), form.trackingNumber)
                }
                form.orderedDate?.let {
                    ReviewRow(stringResource(R.string.editor_ordered_date), it.format(DATE_FMT))
                }
                if (form.etaStart != null || form.etaEnd != null) {
                    ReviewRow(
                        stringResource(R.string.editor_eta_start),
                        listOfNotNull(form.etaStart, form.etaEnd)
                            .joinToString(" – ") { it.format(DATE_FMT) },
                    )
                }
            }
        }

        if (form.notes.isNotBlank()) {
            item {
                FormSection(title = stringResource(R.string.editor_section_notes)) {
                    Text(form.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
