package com.ordra.core.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.ui.theme.OrdraMint
import com.ordra.core.ui.theme.OrdraViolet
import java.time.LocalTime

/** What the Home banner is currently surfacing — all kinds map to real data only. */
enum class CompanionBannerKind {
    ATTENTION,
    ARRIVING_TODAY,
    DELIVERED,
    ACTIVE,
    EMPTY,
    /** Reserved for when a remote ORDRA AI service exists and is unreachable. */
    AI_UNAVAILABLE,
}

/** Resolved banner content. Counts are real aggregates — never invented. */
data class CompanionBannerData(
    val kind: CompanionBannerKind,
    val count: Int = 0,
    val secondaryKind: CompanionBannerKind? = null,
    val secondaryCount: Int = 0,
)

/** Priority: problems first, then today's arrivals, recent deliveries, active, empty. */
fun resolveCompanionBanner(
    activeCount: Int,
    arrivingTodayCount: Int,
    attentionCount: Int,
    recentlyDeliveredCount: Int,
): CompanionBannerData = when {
    attentionCount > 0 -> CompanionBannerData(
        kind = CompanionBannerKind.ATTENTION,
        count = attentionCount,
        secondaryKind = if (arrivingTodayCount > 0) CompanionBannerKind.ARRIVING_TODAY else null,
        secondaryCount = arrivingTodayCount,
    )
    arrivingTodayCount > 0 -> CompanionBannerData(CompanionBannerKind.ARRIVING_TODAY, arrivingTodayCount)
    recentlyDeliveredCount > 0 -> CompanionBannerData(CompanionBannerKind.DELIVERED, recentlyDeliveredCount)
    activeCount > 0 -> CompanionBannerData(CompanionBannerKind.ACTIVE, activeCount)
    else -> CompanionBannerData(CompanionBannerKind.EMPTY)
}

private fun CompanionBannerKind.toCompanionState(): CompanionState = when (this) {
    CompanionBannerKind.ATTENTION -> CompanionState.ATTENTION
    CompanionBannerKind.ARRIVING_TODAY -> CompanionState.DELIVERY
    CompanionBannerKind.DELIVERED -> CompanionState.SUCCESS
    CompanionBannerKind.ACTIVE -> CompanionState.IDLE
    CompanionBannerKind.EMPTY -> CompanionState.EMPTY
    CompanionBannerKind.AI_UNAVAILABLE -> CompanionState.OFFLINE
}

@Composable
private fun bannerMessage(kind: CompanionBannerKind, count: Int): String = when (kind) {
    CompanionBannerKind.ATTENTION ->
        pluralStringResource(R.plurals.ai_msg_attention, count, count)
    CompanionBannerKind.ARRIVING_TODAY ->
        pluralStringResource(R.plurals.ai_msg_arriving, count, count)
    CompanionBannerKind.DELIVERED ->
        pluralStringResource(R.plurals.ai_msg_delivered, count, count)
    CompanionBannerKind.ACTIVE ->
        pluralStringResource(R.plurals.ai_msg_active, count, count)
    CompanionBannerKind.EMPTY -> stringResource(R.string.ai_msg_empty)
    CompanionBannerKind.AI_UNAVAILABLE -> stringResource(R.string.ai_msg_unavailable)
}

@Composable
private fun greeting(): String {
    val res = when (LocalTime.now().hour) {
        in 5..11 -> R.string.ai_greeting_morning
        in 12..17 -> R.string.ai_greeting_afternoon
        else -> R.string.ai_greeting_evening
    }
    return stringResource(res)
}

/**
 * Smart ORDRA AI companion banner for Home. Translucent glass surface + restrained
 * brand aurora — the one allowed "AI" surface on the dashboard. All content comes
 * from real order/shipment aggregates via [data].
 *
 * [onAskAi] stays null until an ORDRA AI destination exists; without it the Ask
 * action is simply not rendered instead of pretending a feature exists.
 */
@Composable
fun OrdraAiBanner(
    data: CompanionBannerData,
    onViewParcels: () -> Unit,
    modifier: Modifier = Modifier,
    onAskAi: (() -> Unit)? = null,
    onAddOrder: (() -> Unit)? = null,
) {
    val dark = isSystemInDarkTheme()
    val surfaceAlpha = if (dark) CompanionTokens.SURFACE_ALPHA_DARK else CompanionTokens.SURFACE_ALPHA_LIGHT
    val auroraAlpha = if (dark) CompanionTokens.AURORA_ALPHA_DARK else CompanionTokens.AURORA_ALPHA_LIGHT
    val glow = OrdraViolet.copy(alpha = auroraAlpha)
    val glowMint = OrdraMint.copy(alpha = auroraAlpha * 0.6f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = surfaceAlpha),
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = CompanionTokens.BORDER_ALPHA),
        ),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glow, Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.maxDimension * 0.9f,
                        ),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowMint, Color.Transparent),
                            center = Offset(0f, size.height),
                            radius = size.maxDimension * 0.6f,
                        ),
                    )
                },
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val avatarSize = if (maxWidth < 380.dp) 72.dp else CompanionVariant.BANNER.defaultSize
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(
                                        Brush.linearGradient(listOf(OrdraViolet, OrdraMint)),
                                        CircleShape,
                                    ),
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = stringResource(R.string.ai_brand),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = greeting(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = bannerMessage(data.kind, data.count),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        data.secondaryKind?.let { secondary ->
                            Text(
                                text = bannerMessage(secondary, data.secondaryCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onAskAi != null) {
                                Button(onClick = onAskAi) {
                                    Text(stringResource(R.string.ai_action_ask))
                                }
                            }
                            if (data.kind == CompanionBannerKind.EMPTY && onAddOrder != null) {
                                Button(onClick = onAddOrder) {
                                    Text(stringResource(R.string.ai_action_add_first))
                                }
                            } else {
                                OutlinedButton(onClick = onViewParcels) {
                                    Text(stringResource(R.string.ai_action_view_parcels))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                    CompanionAvatar(
                        state = data.kind.toCompanionState(),
                        variant = CompanionVariant.BANNER,
                        size = avatarSize,
                    )
                }
            }
        }
    }
}
