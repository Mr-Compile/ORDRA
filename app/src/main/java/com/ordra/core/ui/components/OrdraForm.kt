package com.ordra.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ordra.R
import com.ordra.core.ui.theme.OrdraAmber

private val FieldShape = RoundedCornerShape(10.dp)
private val FieldTextStyle @Composable get() =
    MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
private val FieldLabelStyle @Composable get() =
    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)

/**
 * Compact form field — external label + bordered box + placeholder + inline error.
 * Mirrors the mockup `.field-label` / `.field` anatomy (~42dp tall vs M3's 56dp).
 */
@Composable
fun OrdraField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    placeholder: String? = null,
    error: String? = null,
    supporting: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier) {
        FieldLabel(label, required)
        Spacer(Modifier.height(6.dp))
        FieldBox(error != null) { innerPadding ->
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = FieldTextStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                singleLine = singleLine,
                minLines = minLines,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box(Modifier.padding(innerPadding)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                placeholder,
                                style = FieldTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        FieldFootnote(error, supporting)
    }
}

/** Read-only compact field that opens a dropdown — mirrors mockup `<select class="field">`. */
@Composable
fun <T> OrdraSelect(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    error: String? = null,
    emptyLabel: String = stringResource(R.string.not_set),
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        FieldLabel(label, required)
        Spacer(Modifier.height(6.dp))
        Box {
            FieldBox(error != null) { innerPadding ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FieldShape)
                        .clickable { expanded = true }
                        .padding(innerPadding),
                ) {
                    Text(
                        selected?.let { optionLabel(it) } ?: emptyLabel,
                        style = FieldTextStyle,
                        color = if (selected == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        } else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
        FieldFootnote(error, null)
    }
}

/** Pill segmented control — mirrors mockup `.segmented`/`.seg` (entry mode, payment status). */
@Composable
fun <T> OrdraSegmented(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    optionIcon: @Composable ((T) -> ImageVector?)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val on = option == selected
            Surface(
                onClick = { onSelect(option) },
                shape = CircleShape,
                color = if (on) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (on) 1.dp else 0.dp,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 9.dp),
                ) {
                    optionIcon?.invoke(option)?.let { icon ->
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (on) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        optionLabel(option),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

enum class OrdraBannerKind { INFO, WARN }

/** Inline banner — mirrors mockup `.banner.info` / `.banner.warn` (icon + message). */
@Composable
fun OrdraBanner(
    kind: OrdraBannerKind,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (kind) {
        OrdraBannerKind.INFO ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) to MaterialTheme.colorScheme.primary
        OrdraBannerKind.WARN ->
            OrdraAmber.copy(alpha = 0.16f) to Color(0xFFB45309)
    }
    Surface(
        color = container,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = content)
        }
    }
}

@Composable
private fun FieldLabel(label: String, required: Boolean) {
    Row {
        Text(label, style = FieldLabelStyle, color = MaterialTheme.colorScheme.onSurface)
        if (required) {
            Text(" *", style = FieldLabelStyle, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun FieldBox(isError: Boolean, content: @Composable (innerPadding: androidx.compose.foundation.layout.PaddingValues) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = FieldShape,
        border = BorderStroke(
            1.dp,
            if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        content(androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 11.dp))
    }
}

@Composable
private fun FieldFootnote(error: String?, supporting: String?) {
    val text = error ?: supporting ?: return
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error != null) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )
}
