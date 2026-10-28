package com.ordra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Static loading placeholders — deliberately no shimmer or animation so
 * they are always reduced-motion safe.
 */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
        ),
    )
}

/** Placeholder that mimics an OrderCard row (title, subtitle, trailing chip). */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                SkeletonBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                Spacer(Modifier.height(6.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.4f).height(12.dp))
            }
            Spacer(Modifier.width(12.dp))
            SkeletonBox(Modifier.width(56.dp).height(20.dp))
        }
    }
}

/** A column of skeleton cards sized like the app's list screens. */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 4,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(count) { SkeletonCard() }
    }
}
