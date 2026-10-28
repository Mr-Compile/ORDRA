package com.ordra.feature.orderedit

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.common.MoneyFormat
import com.ordra.core.ui.components.OrdraBanner
import com.ordra.core.ui.components.OrdraBannerKind
import com.ordra.core.ui.components.OrdraField
import com.ordra.core.ui.components.OrdraSegmented
import com.ordra.core.ui.components.OrdraSelect
import com.ordra.core.ui.courierLabel
import com.ordra.core.ui.orderStatusLabel
import com.ordra.core.ui.paymentMethodLabel
import com.ordra.core.ui.paymentStatusLabel
import com.ordra.core.ui.platformColor
import com.ordra.core.ui.platformLabel
import com.ordra.core.ui.theme.standardTween
import com.ordra.domain.model.Courier
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ReminderType
import com.ordra.domain.model.SmartImportResult
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
                onBack = {
                    if (uiState.entryMode == EntryMode.IMPORT) {
                        viewModel.setEntryMode(EntryMode.MANUAL)
                    } else if (uiState.step == EditorStep.REVIEW) {
                        viewModel.backToForm()
                    } else if (uiState.form.isDirty) {
                        showDiscardDialog = true
                    } else {
                        onNavigateBack()
                    }
                },
                onArchive = viewModel::archive,
                onRestore = viewModel::restore,
                onDelete = { showDeleteDialog = true },
            )
        },
        bottomBar = {
            if (uiState.entryMode == EntryMode.MANUAL) {
                EditorBottomBar(
                    step = uiState.step,
                    isSaving = uiState.isSaving,
                    onPrimary = {
                        if (uiState.step == EditorStep.FORM) viewModel.goToReview()
                        else viewModel.save()
                    },
                    onSecondary = viewModel::backToForm,
                    onCancel = {
                        if (uiState.form.isDirty) showDiscardDialog = true else onNavigateBack()
                    },
                )
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = { fadeIn(standardTween()) togetherWith fadeOut(standardTween()) },
            label = "editor_step",
        ) { step ->
        if (step == EditorStep.FORM) {
            OrderFormContent(
                state = uiState,
                onFormChange = viewModel::updateForm,
                onCreateCategory = viewModel::createCategory,
                onEntryMode = viewModel::setEntryMode,
                onImportImages = viewModel::onImportImages,
                onFieldTouched = viewModel::markFieldTouched,
                onDismissImportNotice = viewModel::dismissImportNotice,
                modifier = Modifier.padding(padding),
            )
        } else {
            OrderReviewContent(
                form = uiState.form,
                availableCategories = uiState.availableCategories,
                modifier = Modifier.padding(padding),
            )
        }
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
    onBack: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(stringResource(if (isEdit) R.string.editor_title_edit else R.string.editor_title_add))
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
            if (isEdit) {
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
    onCancel: () -> Unit,
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
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.editor_save))
            }
        } else {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(onClick = onPrimary, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.editor_review))
            }
        }
    }
}

@Composable
private fun OrderFormContent(
    state: AddEditUiState,
    onFormChange: ((OrderFormState) -> OrderFormState) -> Unit,
    onCreateCategory: (String) -> Unit,
    onEntryMode: (EntryMode) -> Unit,
    onImportImages: (List<String>) -> Unit,
    onFieldTouched: (String) -> Unit,
    onDismissImportNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    val errors = state.fieldErrors
    val importMode = state.entryMode == EntryMode.IMPORT && !form.isEdit
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(state.attemptedSave) {
        if (!state.attemptedSave || errors.isEmpty()) return@LaunchedEffect
        val shift = if (form.isEdit) -1 else 0
        val target = when {
            errors.keys.any { it == "title" || it == "platform" || it == "customPlatformName" } ->
                2 + shift
            errors.keys.any { it.startsWith("item_") } -> 3 + shift
            errors.keys.any { it == "shipping" || it == "discount" || it == "total" } -> 4 + shift
            else -> 6 + shift
        }.coerceAtLeast(0)
        listState.animateScrollToItem(target)
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!form.isEdit) {
            item {
                EntrySegment(mode = state.entryMode, onSelect = onEntryMode)
            }
        }
        if (importMode) {
            item {
                EditorImportPane(state = state, onPick = onImportImages)
            }
            return@LazyColumn
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.editor_required_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.importNotice && state.importResult != null) {
                    ImportNoticeCard(
                        result = state.importResult,
                        onDismiss = onDismissImportNotice,
                    )
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_platform)) {
                OrdraField(
                    label = stringResource(R.string.editor_order_title),
                    value = form.title,
                    onValueChange = { v -> onFormChange { it.copy(title = v) } },
                    required = true,
                    placeholder = stringResource(R.string.editor_ph_order_title),
                    error = errors["title"],
                    onBlur = { onFieldTouched("title") },
                )
                PlatformGrid(
                    selected = form.platform,
                    error = errors["platform"],
                    onSelect = { p ->
                        onFieldTouched("platform")
                        onFormChange { it.copy(platform = p) }
                    },
                )
                if (form.platform == Platform.OTHER) {
                    OrdraField(
                        label = stringResource(R.string.editor_custom_platform),
                        value = form.customPlatformName,
                        onValueChange = { v -> onFormChange { it.copy(customPlatformName = v) } },
                        required = true,
                        placeholder = stringResource(R.string.editor_ph_custom_platform),
                        error = errors["customPlatformName"],
                        onBlur = { onFieldTouched("customPlatformName") },
                    )
                }
                OrdraField(
                    label = stringResource(R.string.editor_seller),
                    value = form.sellerName,
                    onValueChange = { v -> onFormChange { it.copy(sellerName = v) } },
                    placeholder = stringResource(R.string.editor_ph_seller),
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_items)) {
                form.items.forEachIndexed { index, item ->
                    ItemCard(
                        index = index,
                        item = item,
                        errors = errors,
                        onFieldTouched = onFieldTouched,
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
                    if (index < form.items.lastIndex) Spacer(Modifier.height(10.dp))
                }
                OutlinedButton(
                    onClick = {
                        onFormChange { it.copy(items = it.items + OrderItemForm()) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.editor_add_item))
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_pricing)) {
                if (form.itemsSubtotalMinor > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            stringResource(R.string.editor_items_subtotal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            MoneyFormat.formatMinor(form.itemsSubtotalMinor, form.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrdraField(
                        label = stringResource(R.string.editor_shipping),
                        value = form.shipping,
                        onValueChange = { v -> onFormChange { it.copy(shipping = v) } },
                        placeholder = "0.00",
                        error = errors["shipping"],
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onBlur = { onFieldTouched("shipping") },
                        modifier = Modifier.weight(1f),
                    )
                    OrdraField(
                        label = stringResource(R.string.editor_discount),
                        value = form.discount,
                        onValueChange = { v -> onFormChange { it.copy(discount = v) } },
                        placeholder = "0.00",
                        error = errors["discount"],
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onBlur = { onFieldTouched("discount") },
                        modifier = Modifier.weight(1f),
                    )
                }
                OrdraField(
                    label = stringResource(R.string.editor_total),
                    value = form.total,
                    onValueChange = { v -> onFormChange { it.copy(total = v) } },
                    required = true,
                    placeholder = "0.00",
                    error = errors["total"],
                    onBlur = { onFieldTouched("total") },
                    supporting = if (form.total.isBlank() && form.suggestedTotalMinor != 0L) {
                        stringResource(
                            R.string.editor_total_hint,
                            MoneyFormat.formatMinor(form.suggestedTotalMinor, form.currencyCode),
                        )
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_payment)) {
                PaymentMethodCards(
                    selected = form.paymentMethod,
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
                Spacer(Modifier.height(4.dp))
                PaymentStatusSegment(
                    selected = form.paymentStatus,
                    onSelect = { s -> onFormChange { it.copy(paymentStatus = s) } },
                )
                if (form.paymentMethod == PaymentMethod.COD) {
                    MoneyFormat.parseToMinor(form.total)?.let { cod ->
                        OrdraBanner(
                            kind = OrdraBannerKind.WARN,
                            icon = Icons.Filled.Payments,
                            text = stringResource(
                                R.string.editor_cod_note,
                                MoneyFormat.formatMinor(cod, form.currencyCode),
                            ),
                        )
                    }
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_delivery)) {
                OrdraSelect(
                    label = stringResource(R.string.editor_status),
                    options = OrderStatus.entries.filter {
                        it != OrderStatus.DRAFT || form.status == OrderStatus.DRAFT
                    },
                    selected = form.status,
                    optionLabel = { orderStatusLabel(it) },
                    onSelect = { s -> onFormChange { it.copy(status = s) } },
                )
                OrdraSelect(
                    label = stringResource(R.string.editor_courier),
                    options = Courier.entries,
                    selected = form.courier.takeIf { it != Courier.UNKNOWN },
                    optionLabel = { courierLabel(it, null) },
                    onSelect = { c -> onFormChange { it.copy(courier = c) } },
                )
                if (form.courier == Courier.OTHER) {
                    OrdraField(
                        label = stringResource(R.string.editor_custom_courier),
                        value = form.customCourierName,
                        onValueChange = { v -> onFormChange { it.copy(customCourierName = v) } },
                        error = errors["customCourierName"],
                        onBlur = { onFieldTouched("customCourierName") },
                    )
                }
                OrdraField(
                    label = stringResource(R.string.editor_tracking),
                    value = form.trackingNumber,
                    onValueChange = { v -> onFormChange { it.copy(trackingNumber = v) } },
                    placeholder = stringResource(R.string.editor_ph_tracking),
                )
                OrdraDateField(
                    label = stringResource(R.string.editor_ordered_date),
                    value = form.orderedDate,
                    onChange = { d -> onFormChange { it.copy(orderedDate = d) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrdraDateField(
                        label = stringResource(R.string.editor_eta_start),
                        value = form.etaStart,
                        onChange = { d -> onFormChange { it.copy(etaStart = d) } },
                        modifier = Modifier.weight(1f),
                    )
                    OrdraDateField(
                        label = stringResource(R.string.editor_eta_end),
                        value = form.etaEnd,
                        onChange = { d ->
                            onFieldTouched("etaEnd")
                            onFormChange { it.copy(etaEnd = d) }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                FieldError(errors["etaEnd"])
                Column {
                    Row {
                        Text(
                            stringResource(R.string.editor_reminders_label),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    ReminderChips(form.reminderTypes) { t ->
                        onFormChange {
                            it.copy(
                                reminderTypes = if (t in it.reminderTypes) it.reminderTypes - t
                                else it.reminderTypes + t,
                            )
                        }
                    }
                }
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_notes)) {
                OrdraField(
                    label = stringResource(R.string.editor_notes),
                    value = form.notes,
                    onValueChange = { v -> onFormChange { it.copy(notes = v) } },
                    placeholder = stringResource(R.string.editor_ph_notes),
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            FormSection(title = stringResource(R.string.editor_section_categories)) {
                CategoryPicker(
                    available = state.availableCategories,
                    selected = form.selectedCategoryIds,
                    onToggle = { id ->
                        onFormChange {
                            it.copy(
                                selectedCategoryIds = if (id in it.selectedCategoryIds) {
                                    it.selectedCategoryIds - id
                                } else {
                                    it.selectedCategoryIds + id
                                },
                            )
                        }
                    },
                    onCreate = onCreateCategory,
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdraDateField(
    label: String,
    value: LocalDate?,
    onChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    Column(modifier) {
        Row {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        Spacer(Modifier.height(6.dp))
        Surface(
            onClick = { dialogOpen = true },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                Text(
                    value?.format(DATE_FMT) ?: "—",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.editor_pick_date),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
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

/** Bordered "Item N" sub-card — mirrors the mockup's .wiz-item grouping. */
@Composable
private fun ItemCard(
    index: Int,
    item: OrderItemForm,
    errors: Map<String, String>,
    onFieldTouched: (String) -> Unit,
    onChange: (OrderItemForm) -> Unit,
    onRemove: (() -> Unit)?,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.editor_item_header, index + 1),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.editor_remove_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            OrdraField(
                label = stringResource(R.string.editor_item_name),
                value = item.name,
                onValueChange = { onChange(item.copy(name = it)) },
                required = true,
                placeholder = stringResource(R.string.editor_item_name),
                error = errors["item_name_$index"],
                onBlur = { onFieldTouched("item_name_$index") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrdraField(
                    label = stringResource(R.string.editor_item_variant),
                    value = item.variant,
                    onValueChange = { onChange(item.copy(variant = it)) },
                    placeholder = stringResource(R.string.editor_item_variant),
                    modifier = Modifier.weight(1f),
                )
                OrdraField(
                    label = stringResource(R.string.editor_item_qty),
                    value = item.quantity,
                    onValueChange = { onChange(item.copy(quantity = it.filter { c -> c.isDigit() })) },
                    placeholder = stringResource(R.string.editor_item_qty),
                    error = errors["item_qty_$index"],
                    onBlur = { onFieldTouched("item_qty_$index") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OrdraField(
                    label = stringResource(R.string.editor_item_price),
                    value = item.unitPrice,
                    onValueChange = { onChange(item.copy(unitPrice = it)) },
                    placeholder = stringResource(R.string.editor_item_price),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(
    available: List<com.ordra.domain.model.Category>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onCreate: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    if (available.isNotEmpty()) {
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            available.forEach { category ->
                androidx.compose.material3.FilterChip(
                    selected = category.id in selected,
                    onClick = { onToggle(category.id) },
                    label = { Text(category.name) },
                )
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text(stringResource(R.string.editor_new_category)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        Spacer(Modifier.width(8.dp))
        TextButton(
            onClick = {
                onCreate(newName)
                newName = ""
            },
            enabled = newName.isNotBlank(),
        ) { Text(stringResource(R.string.editor_add_category)) }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ReminderChips(selected: Set<ReminderType>, onToggle: (ReminderType) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            ReminderType.DELIVERY_DAY to stringResource(R.string.remind_delivery_day),
            ReminderType.DAY_BEFORE to stringResource(R.string.remind_day_before),
            ReminderType.TWO_DAYS_BEFORE to stringResource(R.string.remind_2_days),
        ).forEach { (type, label) ->
            androidx.compose.material3.FilterChip(
                selected = type in selected,
                onClick = { onToggle(type) },
                label = { Text(label) },
            )
        }
    }
}

/** Single review card + offline banner — mirrors the mockup editor review step. */
@Composable
private fun OrderReviewContent(
    form: OrderFormState,
    availableCategories: List<com.ordra.domain.model.Category>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OrdraBanner(
                kind = OrdraBannerKind.INFO,
                icon = Icons.Filled.Info,
                text = stringResource(R.string.editor_review_offline),
            )
        }
        item {
            FormSection(title = stringResource(R.string.editor_review_title)) {
                ReviewRow(
                    stringResource(R.string.editor_platform),
                    platformLabel(
                        form.platform ?: Platform.OTHER,
                        form.customPlatformName.ifBlank { null },
                    ),
                )
                val displayTitle = form.title.ifBlank {
                    form.items.firstOrNull { it.name.isNotBlank() }?.name ?: "—"
                }
                ReviewRow(stringResource(R.string.editor_order_title), displayTitle)
                ReviewRow(
                    stringResource(R.string.editor_seller),
                    form.sellerName.ifBlank { "—" },
                )
                val filledItems = form.items.filterNot { it.isCompletelyEmpty }
                filledItems.forEachIndexed { i, item ->
                    val qty = item.quantity.toIntOrNull() ?: 1
                    val price = MoneyFormat.parseToMinor(item.unitPrice)
                    ReviewRow(
                        stringResource(R.string.editor_item_header, i + 1),
                        buildString {
                            append(item.name)
                            if (item.variant.isNotBlank()) append(" (${item.variant})")
                            append(" ×$qty")
                            if (price != null) {
                                append(" — ")
                                append(MoneyFormat.formatMinor(price * qty, form.currencyCode))
                            }
                        },
                    )
                }
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
                ReviewRow(
                    stringResource(R.string.editor_review_payment),
                    "${paymentMethodLabel(form.paymentMethod)} · ${paymentStatusLabel(form.paymentStatus)}",
                )
                ReviewRow(stringResource(R.string.editor_status), orderStatusLabel(form.status))
                ReviewRow(
                    stringResource(R.string.editor_courier),
                    if (form.courier != Courier.UNKNOWN) {
                        courierLabel(form.courier, form.customCourierName.ifBlank { null })
                    } else "—",
                )
                ReviewRow(
                    stringResource(R.string.editor_tracking),
                    form.trackingNumber.ifBlank { "—" },
                )
                ReviewRow(
                    stringResource(R.string.editor_ordered_date),
                    form.orderedDate?.format(DATE_FMT) ?: "—",
                )
                ReviewRow(
                    stringResource(R.string.editor_eta_start),
                    if (form.etaStart != null || form.etaEnd != null) {
                        listOfNotNull(form.etaStart, form.etaEnd)
                            .joinToString(" – ") { it.format(DATE_FMT) }
                    } else "—",
                )
                val reminderNames = mutableListOf<String>()
                for (t in form.reminderTypes) reminderNames += reminderTypeLabel(t)
                ReviewRow(
                    stringResource(R.string.editor_review_reminders),
                    if (reminderNames.isEmpty()) "—" else reminderNames.joinToString(", "),
                )
                if (form.notes.isNotBlank()) {
                    ReviewRow(stringResource(R.string.editor_notes), form.notes)
                }
                val chosen = availableCategories.filter { it.id in form.selectedCategoryIds }
                if (chosen.isNotEmpty()) {
                    ReviewRow(
                        stringResource(R.string.editor_section_categories),
                        chosen.joinToString(", ") { it.name },
                    )
                }
            }
        }
    }
}

@Composable
private fun reminderTypeLabel(type: ReminderType): String = when (type) {
    ReminderType.DELIVERY_DAY -> stringResource(R.string.remind_delivery_day)
    ReminderType.DAY_BEFORE -> stringResource(R.string.remind_day_before)
    ReminderType.TWO_DAYS_BEFORE -> stringResource(R.string.remind_2_days)
    ReminderType.CUSTOM -> stringResource(R.string.remind_custom)
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

/** Manual entry / Import picker for new orders — mirrors mockup #entrySeg. */
@Composable
private fun EntrySegment(mode: EntryMode, onSelect: (EntryMode) -> Unit) {
    OrdraSegmented(
        options = listOf(EntryMode.MANUAL, EntryMode.IMPORT),
        selected = mode,
        onSelect = onSelect,
        optionLabel = {
            stringResource(
                if (it == EntryMode.MANUAL) R.string.editor_entry_manual
                else R.string.editor_entry_import,
            )
        },
        optionIcon = {
            if (it == EntryMode.MANUAL) Icons.Filled.EditNote
            else Icons.Filled.DocumentScanner
        },
    )
}

/** Embedded import pane — pick → OCR → extracted values merge into the SAME
 *  manual draft (fill-blanks), then the manual form shows for review. */
@Composable
private fun EditorImportPane(
    state: AddEditUiState,
    onPick: (List<String>) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPick(listOf(uri.toString())) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OrdraBanner(
            kind = OrdraBannerKind.INFO,
            icon = Icons.Filled.DocumentScanner,
            text = stringResource(R.string.editor_import_banner),
        )
        if (state.importBusy) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.editor_import_processing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Surface(
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.editor_import_pick),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.editor_import_pick_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.importOcrFailed) {
                Text(
                    stringResource(R.string.editor_import_ocr_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Post-import notice on the manual form — "review before saving" banner,
 *  per-field confidence chips, dismissible. Same fields, just prefilled. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ImportNoticeCard(
    result: SmartImportResult,
    onDismiss: () -> Unit,
) {
    val draft = result.draft
    FormSection(title = stringResource(R.string.editor_extracted_title)) {
        OrdraBanner(
            kind = OrdraBannerKind.WARN,
            icon = Icons.AutoMirrored.Filled.FactCheck,
            text = stringResource(R.string.editor_import_review_banner),
        )
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ConfChip(stringResource(R.string.editor_field_product), draft.title.confidence)
            ConfChip(stringResource(R.string.editor_platform), draft.platform.confidence)
            ConfChip(stringResource(R.string.editor_seller), draft.sellerName.confidence)
            ConfChip(stringResource(R.string.editor_field_total_php), draft.totalMinor.confidence)
            ConfChip(stringResource(R.string.editor_courier), draft.courier.confidence)
            ConfChip(stringResource(R.string.editor_tracking), draft.trackingNumber.confidence)
        }
        Box(Modifier.fillMaxWidth()) {
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(stringResource(R.string.editor_dismiss))
            }
        }
    }
}

@Composable
private fun ConfChip(field: String, confidence: Float) {
    val (level, container, content) = when {
        confidence >= 0.8f -> Triple(
            stringResource(R.string.editor_import_conf_high),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        confidence >= 0.5f -> Triple(
            stringResource(R.string.editor_import_conf_medium),
            com.ordra.core.ui.theme.OrdraAmber.copy(alpha = 0.18f),
            androidx.compose.ui.graphics.Color(0xFFB45309),
        )
        else -> Triple(
            stringResource(R.string.editor_import_conf_low),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Surface(color = container, shape = CircleShape) {
        Text(
            "$field · $level",
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/** 4-column brand picker — mirrors the mockup .plat-grid of colored platform badges. */
@Composable
private fun PlatformGrid(
    selected: Platform?,
    error: String?,
    onSelect: (Platform) -> Unit,
) {
    Column {
        Row {
            Text(
                stringResource(R.string.editor_platform),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(" *", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(6.dp))
        Platform.entries.chunked(4).forEach { rowPlatforms ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowPlatforms.forEach { platform ->
                    val on = platform == selected
                    val tint = platformColor(platform)
                    Surface(
                        onClick = { onSelect(platform) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (on) 1.5.dp else 1.dp,
                            if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(tint, RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    platformLabel(platform).take(1),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = androidx.compose.ui.graphics.Color.White,
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                platformLabel(platform),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
                repeat(4 - rowPlatforms.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
        FieldError(error)
    }
}

/** Radio-card list — mirrors the mockup .radio-card payment options. */
@Composable
private fun PaymentMethodCards(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethod.entries.forEach { method ->
            val on = method == selected
            Surface(
                onClick = { onSelect(method) },
                shape = RoundedCornerShape(12.dp),
                color = if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (on) 1.5.dp else 1.dp,
                    if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    RadioButton(selected = on, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            paymentMethodLabel(method),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        if (method == PaymentMethod.COD) {
                            Text(
                                stringResource(R.string.editor_payment_cod_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Unpaid / Paid / Partial pill — mirrors the mockup payment-status .segmented. */
@Composable
private fun PaymentStatusSegment(
    selected: PaymentStatus,
    onSelect: (PaymentStatus) -> Unit,
) {
    val main = listOf(PaymentStatus.UNPAID, PaymentStatus.PAID, PaymentStatus.PARTIALLY_PAID)
    val options = if (selected !in main) main + selected else main
    Column {
        Row {
            Text(
                stringResource(R.string.editor_payment_status),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        Spacer(Modifier.height(6.dp))
        OrdraSegmented(
            options = options,
            selected = selected,
            onSelect = onSelect,
            optionLabel = { paymentStatusLabel(it) },
        )
    }
}
