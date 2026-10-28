package com.ordra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.ui.rememberReducedMotion
import com.ordra.core.ui.theme.emphasisTween
import com.ordra.core.ui.theme.microTween
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val HOLD_MS = 400L

/**
 * Transient delivered-confirmation moment: a check scales in under the
 * emphasis budget, holds briefly, then fades out. Fires a Confirm haptic
 * on mount — the haptic is supplementary; the state is communicated by
 * the check itself and the status chip. Under reduced motion the overlay
 * fades in/out statically with no scaling.
 */
@Composable
fun DeliveredCelebrationOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduced = rememberReducedMotion()
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(if (reduced) 1f else 0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        if (reduced) {
            alpha.snapTo(1f)
        } else {
            launch { scale.animateTo(1f, emphasisTween()) }
            alpha.animateTo(1f, emphasisTween())
        }
        delay(HOLD_MS)
        if (reduced) alpha.snapTo(0f) else alpha.animateTo(0f, microTween())
        onFinished()
    }

    val label = stringResource(R.string.swipe_order_delivered)
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 6.dp,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .padding(20.dp)
                    .size(48.dp),
            )
        }
    }
}
