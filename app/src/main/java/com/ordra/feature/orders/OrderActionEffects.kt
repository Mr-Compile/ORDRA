package com.ordra.feature.orders

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.ordra.R
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collects [OrderActionEvent]s: shows an undoable snackbar on success and a
 * plain message on failure. [onDelivered] is invoked when an order was just
 * marked delivered so the screen can show the confirmation moment.
 */
@Composable
fun OrderActionEffects(
    events: SharedFlow<OrderActionEvent>,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onDelivered: () -> Unit = {},
) {
    val context = LocalContext.current
    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is OrderActionEvent.Success -> {
                    if (event.delivered) onDelivered()
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(event.messageRes),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) onUndo()
                }
                is OrderActionEvent.Failure ->
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.messageRes),
                        duration = SnackbarDuration.Short,
                    )
            }
        }
    }
}
