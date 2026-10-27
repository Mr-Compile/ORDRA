package com.ordra.feature.smartimport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ordra.R
import com.ordra.core.ui.ai.CompanionAvatar
import com.ordra.core.ui.ai.CompanionState
import com.ordra.core.ui.ai.CompanionVariant
import com.ordra.core.ui.courierLabel
import com.ordra.core.ui.paymentMethodLabel
import com.ordra.core.ui.platformLabel
import com.ordra.domain.model.Courier
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.feature.orderedit.OrderFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartImportScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SmartImportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.onImagePicked(it.toString()) } }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { if (it is SmartImportEffect.Saved) onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (state.step) {
            ImportStep.PICK -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CompanionAvatar(
                    state = CompanionState.WORKING,
                    variant = CompanionVariant.ONBOARDING,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.import_intro_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.import_intro_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.import_pick)) }
            }
            ImportStep.PROCESSING -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.import_processing),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            ImportStep.REVIEW -> ReviewStep(
                state = state,
                onFormChange = viewModel::updateForm,
                onAnalyze = viewModel::analyzeWithAi,
                onSave = viewModel::confirm,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ReviewStep(
    state: SmartImportUiState,
    onFormChange: ((OrderFormState) -> OrderFormState) -> Unit,
    onAnalyze: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.ocrFailed) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                ),
            ) {
                Text(
                    stringResource(
                        if (state.ocrFailed) R.string.import_ocr_failed
                        else R.string.import_review_hint,
                    ),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item {
            OutlinedTextField(
                value = form.title,
                onValueChange = { v -> onFormChange { it.copy(title = v) } },
                label = { Text(stringResource(R.string.field_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            ImportEnumChips(
                label = stringResource(R.string.field_platform),
                options = Platform.entries,
                selected = form.platform,
                labelOf = { platformLabel(it) },
                onSelect = { p -> onFormChange { it.copy(platform = p) } },
            )
        }
        item {
            OutlinedTextField(
                value = form.sellerName,
                onValueChange = { v -> onFormChange { it.copy(sellerName = v) } },
                label = { Text(stringResource(R.string.editor_seller)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = form.total,
                onValueChange = { v -> onFormChange { it.copy(total = v) } },
                label = { Text(stringResource(R.string.field_total)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            ImportEnumChips(
                label = stringResource(R.string.field_payment),
                options = PaymentMethod.entries,
                selected = form.paymentMethod,
                labelOf = { paymentMethodLabel(it) },
                onSelect = { m -> onFormChange { it.copy(paymentMethod = m) } },
            )
        }
        item {
            ImportEnumChips(
                label = stringResource(R.string.field_courier),
                options = Courier.entries,
                selected = form.courier,
                labelOf = { courierLabel(it) },
                onSelect = { c -> onFormChange { it.copy(courier = c) } },
            )
        }
        item {
            OutlinedTextField(
                value = form.trackingNumber,
                onValueChange = { v -> onFormChange { it.copy(trackingNumber = v) } },
                label = { Text(stringResource(R.string.field_tracking)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        if (state.onlineAnalysisEnabled && state.aiConfigured) {
            item {
                OutlinedButton(
                    onClick = onAnalyze,
                    enabled = !state.aiAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.aiAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(stringResource(R.string.import_ai_analyze))
                }
            }
        }
        if (state.aiNotice != null) {
            item {
                Text(
                    stringResource(
                        when (state.aiNotice) {
                            AiNotice.UNAVAILABLE -> R.string.import_ai_unavailable
                            AiNotice.NO_TEXT -> R.string.import_ai_no_text
                            AiNotice.DONE -> R.string.import_ai_done
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Button(
                onClick = onSave,
                enabled = form.title.isNotBlank() && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.import_save)) }
        }
    }
}

@Composable
private fun <T> ImportEnumChips(
    label: String,
    options: List<T>,
    selected: T?,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(labelOf(option)) },
                )
            }
        }
    }
}
