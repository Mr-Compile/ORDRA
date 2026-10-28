package com.ordra.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.domain.model.Order

private data class SwipeStyle(
    val container: Color,
    val content: Color,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
)

/**
 * OrderCard wrapped in a [SwipeToDismissBox]. The dismiss itself is always
 * vetoed: the confirmed swipe fires [onAction], the card settles back, and
 * the Room flow plus LazyColumn item animation handles removal. If the action
 * fails the row simply stays in place instead of being silently hidden.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableOrderCard(
    order: Order,
    onClick: () -> Unit,
    onAction: (OrderSwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    etaText: String? = null,
) {
    val startAction =
        resolveSwipeAction(order.isArchived, order.status, OrderSwipeDirection.StartToEnd)
    val endAction =
        resolveSwipeAction(order.isArchived, order.status, OrderSwipeDirection.EndToStart)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> startAction?.let(onAction)
                SwipeToDismissBoxValue.EndToStart -> endAction?.let(onAction)
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = startAction != null,
        enableDismissFromEndToStart = endAction != null,
        backgroundContent = {
            val action = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> startAction
                SwipeToDismissBoxValue.EndToStart -> endAction
                SwipeToDismissBoxValue.Settled -> null
            }
            if (action != null) {
                SwipeActionBackground(
                    action = action,
                    fromStart = dismissState.dismissDirection ==
                        SwipeToDismissBoxValue.StartToEnd,
                )
            }
        },
    ) {
        OrderCard(order = order, onClick = onClick, etaText = etaText)
    }
}

@Composable
private fun SwipeActionBackground(
    action: OrderSwipeAction,
    fromStart: Boolean,
) {
    val style = when (action) {
        OrderSwipeAction.MarkDelivered -> SwipeStyle(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Filled.CheckCircle,
            labelRes = R.string.swipe_mark_delivered,
        )
        OrderSwipeAction.Restore -> SwipeStyle(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Filled.Refresh,
            labelRes = R.string.swipe_restore,
        )
        OrderSwipeAction.Archive -> SwipeStyle(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            labelRes = R.string.swipe_archive,
        )
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(color = style.container, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.align(
                if (fromStart) Alignment.CenterStart else Alignment.CenterEnd,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (fromStart) {
                Icon(style.icon, contentDescription = null, tint = style.content)
                Text(
                    stringResource(style.labelRes),
                    color = style.content,
                    style = MaterialTheme.typography.labelLarge,
                )
            } else {
                Text(
                    stringResource(style.labelRes),
                    color = style.content,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(style.icon, contentDescription = null, tint = style.content)
            }
        }
    }
}
