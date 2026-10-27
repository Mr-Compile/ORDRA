package com.ordra.core.ui.ai

import android.provider.Settings
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.ui.theme.OrdraIndigo
import com.ordra.core.ui.theme.OrdraMint
import com.ordra.core.ui.theme.OrdraAmber
import com.ordra.core.ui.theme.OrdraViolet
import kotlin.math.min

/**
 * Ordi — the visual companion of ORDRA AI.
 *
 * States are driven by real application state only (see the Home banner resolver and
 * future AI surfaces). Never fabricate a state that does not reflect real data.
 */
enum class CompanionState {
    IDLE,
    GREETING,
    HAPPY,
    THINKING,
    WORKING,
    SUCCESS,
    DELIVERY,
    ATTENTION,
    WARNING,
    EMPTY,
    ERROR,
    OFFLINE,
}

/** Visual presets — artwork logic stays identical, only size context differs. */
enum class CompanionVariant(val defaultSize: Dp) {
    AVATAR(40.dp),
    COMPACT(56.dp),
    EMPTY_STATE(88.dp),
    BANNER(104.dp),
    ONBOARDING(120.dp),
}

/** Shared tokens for the AI/companion layer — keep values here, not in screens. */
object CompanionTokens {
    const val SURFACE_ALPHA_LIGHT = 0.80f
    const val SURFACE_ALPHA_DARK = 0.58f
    const val BORDER_ALPHA = 0.24f
    const val AURORA_ALPHA_LIGHT = 0.14f
    const val AURORA_ALPHA_DARK = 0.20f
    const val BREATHE_MS = 2600
    const val DOT_PULSE_MS = 1500
    const val SWEEP_MS = 1800
}

/** True when the user disabled animations at system level — motion must degrade to static. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) <= 0f
    }
}

/**
 * Renders Ordi at [size]. When [painter] is provided (future final artwork — SVG/PNG/
 * Lottie imported assets), it replaces the built-in procedural drawing, so real assets
 * can drop in later without touching call sites.
 */
@Composable
fun CompanionAvatar(
    state: CompanionState,
    modifier: Modifier = Modifier,
    variant: CompanionVariant = CompanionVariant.COMPACT,
    size: Dp = variant.defaultSize,
    painter: Painter? = null,
    animated: Boolean = true,
) {
    val label = stringResource(R.string.cd_ordi)
    val sized = modifier
        .size(size)
        .semantics { contentDescription = label }
    if (painter != null) {
        Image(painter = painter, contentDescription = null, modifier = sized)
        return
    }
    OrdiArt(state = state, animated = animated, modifier = sized)
}

// ---------------------------------------------------------------------------
// Procedural artwork
//
// Design space is 100x100 units, scaled to the requested size. Ordi is a soft
// rounded "parcel" body with a brand gradient, a small antenna (AI signal cue),
// expressive eyes, a mouth, and one state accessory at most.
// ---------------------------------------------------------------------------

private val BodyGradientStart = OrdraIndigo
private val BodyGradientEnd = OrdraViolet
private val FaceWhite = Color(0xFFF5F5FA)
private val FaceDark = Color(0xFF14164D)
private val SeamColor = Color(0x40FFFFFF)

private enum class EyeStyle { OPEN, HAPPY, SLEEP, HALF, DOT, THINK }
private enum class MouthStyle { SMILE, BIG_SMILE, FLAT, WORRIED, NONE }

private data class Face(val eyes: EyeStyle, val mouth: MouthStyle)

private fun faceFor(state: CompanionState) = when (state) {
    CompanionState.IDLE, CompanionState.DELIVERY -> Face(EyeStyle.OPEN, MouthStyle.SMILE)
    CompanionState.GREETING, CompanionState.HAPPY -> Face(EyeStyle.HAPPY, MouthStyle.BIG_SMILE)
    CompanionState.SUCCESS -> Face(EyeStyle.HAPPY, MouthStyle.BIG_SMILE)
    CompanionState.THINKING -> Face(EyeStyle.THINK, MouthStyle.FLAT)
    CompanionState.WORKING -> Face(EyeStyle.OPEN, MouthStyle.FLAT)
    CompanionState.ATTENTION -> Face(EyeStyle.DOT, MouthStyle.FLAT)
    CompanionState.WARNING -> Face(EyeStyle.DOT, MouthStyle.WORRIED)
    CompanionState.EMPTY -> Face(EyeStyle.HALF, MouthStyle.SMILE)
    CompanionState.ERROR -> Face(EyeStyle.DOT, MouthStyle.WORRIED)
    CompanionState.OFFLINE -> Face(EyeStyle.SLEEP, MouthStyle.NONE)
}

@Composable
private fun OrdiArt(state: CompanionState, animated: Boolean, modifier: Modifier) {
    val reducedMotion = rememberReducedMotion()
    val animate = animated && !reducedMotion

    val transition = rememberInfiniteTransition(label = "ordi")
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(CompanionTokens.BREATHE_MS, easing = EaseInOut),
            RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(CompanionTokens.DOT_PULSE_MS, easing = EaseInOut),
            RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(CompanionTokens.SWEEP_MS, easing = LinearEasing),
        ),
        label = "sweep",
    )

    Canvas(modifier) {
        val s = min(size.width, size.height) / 100f
        fun u(v: Float) = v * s
        fun at(x: Float, y: Float) = Offset(u(x), u(y))

        val bob = if (animate && state != CompanionState.OFFLINE) breathe * u(2.2f) else 0f

        withTransform({ translate(0f, -bob) }) {
            // Body: soft parcel silhouette.
            val body = Rect(u(12f), u(22f), u(88f), u(94f))
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(BodyGradientStart, BodyGradientEnd),
                    start = at(12f, 22f),
                    end = at(88f, 94f),
                ),
                topLeft = body.topLeft,
                size = body.size,
                cornerRadius = CornerRadius(u(26f)),
            )
            // Parcel seam + tape hint — subtle, not a literal box.
            drawLine(
                color = SeamColor,
                start = at(16f, 40f),
                end = at(84f, 40f),
                strokeWidth = u(1.8f),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = SeamColor,
                start = at(50f, 22f),
                end = at(50f, 40f),
                strokeWidth = u(3.2f),
                cap = StrokeCap.Round,
            )
            // Antenna: small stem + signal dot (hollow when offline).
            drawLine(
                color = BodyGradientEnd,
                start = at(70f, 22f),
                end = at(74f, 12f),
                strokeWidth = u(2.4f),
                cap = StrokeCap.Round,
            )
            if (state == CompanionState.OFFLINE) {
                drawCircle(
                    color = FaceDark,
                    radius = u(3.4f),
                    center = at(75f, 9f),
                    style = Stroke(width = u(1.6f)),
                )
            } else {
                drawCircle(color = OrdraMint, radius = u(3.4f), center = at(75f, 9f))
            }

            drawFace(faceFor(state), ::u, ::at)
            drawAccessory(state, pulse, sweep, animate, ::u, ::at)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFace(
    face: Face,
    u: (Float) -> Float,
    at: (Float, Float) -> Offset,
) {
    val leftEye = at(38f, 56f)
    val rightEye = at(62f, 56f)

    fun openEye(center: Offset, pupilOffset: Offset = Offset.Zero) {
        drawCircle(color = FaceWhite, radius = u(6.4f), center = center)
        drawCircle(color = FaceDark, radius = u(3f), center = center + pupilOffset)
    }

    fun arcEye(center: Offset, top: Boolean) {
        drawArc(
            color = FaceWhite,
            startAngle = if (top) 180f else 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = center - Offset(u(5.4f), u(4.6f)),
            size = Size(u(10.8f), u(9.2f)),
            style = Stroke(width = u(2.2f), cap = StrokeCap.Round),
        )
    }

    when (face.eyes) {
        EyeStyle.OPEN -> {
            openEye(leftEye)
            openEye(rightEye)
        }
        EyeStyle.THINK -> {
            val look = Offset(u(1.6f), u(-1.8f))
            openEye(leftEye, look)
            openEye(rightEye, look)
        }
        EyeStyle.HAPPY -> {
            arcEye(leftEye, top = true)
            arcEye(rightEye, top = true)
        }
        EyeStyle.SLEEP -> {
            arcEye(leftEye, top = false)
            arcEye(rightEye, top = false)
        }
        EyeStyle.HALF -> {
            drawLine(
                color = FaceWhite,
                start = leftEye - Offset(u(4.6f), u(1.6f)),
                end = leftEye + Offset(u(4.6f), u(-1.6f)),
                strokeWidth = u(2f),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = FaceWhite,
                start = rightEye - Offset(u(4.6f), u(1.6f)),
                end = rightEye + Offset(u(4.6f), u(-1.6f)),
                strokeWidth = u(2f),
                cap = StrokeCap.Round,
            )
        }
        EyeStyle.DOT -> {
            drawCircle(color = FaceWhite, radius = u(3.6f), center = leftEye)
            drawCircle(color = FaceWhite, radius = u(3.6f), center = rightEye)
        }
    }

    val mouth = at(50f, 74f)
    when (face.mouth) {
        MouthStyle.SMILE -> drawArc(
            color = FaceWhite,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = mouth - Offset(u(5f), u(2.6f)),
            size = Size(u(10f), u(7f)),
            style = Stroke(width = u(2f), cap = StrokeCap.Round),
        )
        MouthStyle.BIG_SMILE -> drawArc(
            color = FaceWhite,
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = mouth - Offset(u(7f), u(3.4f)),
            size = Size(u(14f), u(10f)),
            style = Stroke(width = u(2.2f), cap = StrokeCap.Round),
        )
        MouthStyle.FLAT -> drawLine(
            color = FaceWhite,
            start = mouth - Offset(u(4f), 0f),
            end = mouth + Offset(u(4f), 0f),
            strokeWidth = u(2f),
            cap = StrokeCap.Round,
        )
        MouthStyle.WORRIED -> drawArc(
            color = FaceWhite,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = mouth - Offset(u(4.6f), u(-0.6f)),
            size = Size(u(9.2f), u(7f)),
            style = Stroke(width = u(2f), cap = StrokeCap.Round),
        )
        MouthStyle.NONE -> Unit
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAccessory(
    state: CompanionState,
    pulse: Float,
    sweep: Float,
    animate: Boolean,
    u: (Float) -> Float,
    at: (Float, Float) -> Offset,
) {
    when (state) {
        CompanionState.GREETING -> {
            // Raised wave: small arm nub on the right side.
            drawLine(
                color = BodyGradientEnd,
                start = at(86f, 62f),
                end = at(95f, 50f),
                strokeWidth = u(5f),
                cap = StrokeCap.Round,
            )
            drawCircle(color = OrdraMint, radius = u(3.4f), center = at(96f, 48f))
        }
        CompanionState.THINKING -> {
            // Staggered thinking dots above the head.
            val positions = listOf(at(36f, 12f), at(48f, 8f), at(60f, 12f))
            positions.forEachIndexed { i, center ->
                val phase = if (animate) ((pulse + i / 3f) % 1f) else 0.8f
                drawCircle(
                    color = FaceDark.copy(alpha = 0.35f + 0.55f * phase),
                    radius = u(2.2f),
                    center = center,
                )
            }
        }
        CompanionState.WORKING -> {
            // Orbiting cue around the antenna tip.
            drawArc(
                color = OrdraMint,
                startAngle = if (animate) sweep else 0f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = at(70f, 4f) - Offset(u(7f), u(7f)),
                size = Size(u(14f), u(14f)),
                style = Stroke(width = u(2f), cap = StrokeCap.Round),
            )
        }
        CompanionState.DELIVERY -> {
            // Small held parcel in front of the body.
            val box = Rect(u(38f), u(80f), u(62f), u(96f))
            drawRoundRect(
                color = OrdraMint,
                topLeft = box.topLeft,
                size = box.size,
                cornerRadius = CornerRadius(u(4f)),
            )
            drawLine(
                color = Color.White,
                start = at(50f, 80f),
                end = at(50f, 96f),
                strokeWidth = u(2f),
            )
        }
        CompanionState.ATTENTION, CompanionState.WARNING -> {
            // Amber "!" badge.
            drawCircle(color = OrdraAmber, radius = u(8f), center = at(24f, 18f))
            drawLine(
                color = Color.White,
                start = at(24f, 14f),
                end = at(24f, 19.6f),
                strokeWidth = u(2.4f),
                cap = StrokeCap.Round,
            )
            drawCircle(color = Color.White, radius = u(1.4f), center = at(24f, 22.6f))
        }
        CompanionState.SUCCESS -> {
            // Mint check badge.
            drawCircle(color = OrdraMint, radius = u(8f), center = at(78f, 18f))
            val check = Path().apply {
                moveTo(u(74f), u(18f))
                lineTo(u(77f), u(21.4f))
                lineTo(u(82.6f), u(14.6f))
            }
            drawPath(
                path = check,
                color = Color.White,
                style = Stroke(width = u(2.2f), cap = StrokeCap.Round),
            )
        }
        CompanionState.ERROR -> {
            // Small "?" cue above the head.
            drawArc(
                color = FaceDark,
                startAngle = 200f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = at(70f, 4f) - Offset(u(4f), u(4f)),
                size = Size(u(8f), u(8f)),
                style = Stroke(width = u(2f), cap = StrokeCap.Round),
            )
            drawCircle(color = FaceDark, radius = u(1.4f), center = at(70f, 12f))
        }
        CompanionState.HAPPY -> {
            // Soft cheeks.
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = u(3f),
                center = at(28f, 66f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = u(3f),
                center = at(72f, 66f),
            )
        }
        else -> Unit
    }
}
