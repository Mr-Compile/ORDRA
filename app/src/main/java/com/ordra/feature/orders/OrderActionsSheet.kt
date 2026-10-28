package com.ordra.feature.orders

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.ui.components.OrderSwipeAction
import com.ordra.core.ui.components.OrderSwipeDirection
import com.ordra.core.ui.components.resolveSwipeAction
import com.ordra.domain.model.Order

/**
 * Long-press quick actions for an order card. Actions reuse the same
 * validity rules as the swipe gesture and the same ViewModel entry point.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderActionsSheet(
    order: Order,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onAction: (OrderSwipeAction) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                order.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            SheetActionRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                label = stringResource(R.string.action_open),
            ) {
                onOpen()
                onDismiss()
            }
            val swipeActions = listOfNotNull(
                resolveSwipeAction(
                    order.isArchived,
                    order.status,
                    OrderSwipeDirection.StartToEnd,
                ),
                resolveSwipeAction(
                    order.isArchived,
                    order.status,
                    OrderSwipeDirection.EndToStart,
                ),
            )
            swipeActions.forEach { action ->
                SheetActionRow(
                    icon = action.icon(),
                    label = stringResource(action.labelRes()),
                ) {
                    onAction(action)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun OrderSwipeAction.icon(): ImageVector = when (this) {
    OrderSwipeAction.MarkDelivered -> Icons.Filled.CheckCircle
    OrderSwipeAction.Restore -> Icons.Filled.Refresh
    OrderSwipeAction.Archive -> Icons.AutoMirrored.Filled.ExitToApp
}

@StringRes
private fun OrderSwipeAction.labelRes(): Int = when (this) {
    OrderSwipeAction.MarkDelivered -> R.string.swipe_mark_delivered
    OrderSwipeAction.Restore -> R.string.swipe_restore
    OrderSwipeAction.Archive -> R.string.swipe_archive
}
