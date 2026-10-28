package com.ordra.core.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * Ordra motion tokens — canonical durations from `docs/uiux/DESIGN_SYSTEM.md`.
 * Screens never hardcode millisecond values; every animation routes through
 * these specs so the whole app shares one motion language.
 */
object OrdraMotion {
    /** Press feedback, chip state — midpoint of the 100–150ms micro band. */
    const val MICRO_MS = 120

    /** Transitions, sheet open, status change — midpoint of the 200–300ms band. */
    const val STANDARD_MS = 250

    /** Delivered confirmation — the one allowed emphasis animation, <=400ms. */
    const val EMPHASIS_MS = 350

    /** Standard decelerate for entering elements (M3 emphasized decelerate). */
    val decelerate: Easing = LinearOutSlowInEasing

    /** Standard accelerate for exiting elements. */
    val accelerate: Easing = FastOutLinearInEasing
}

fun <T> microTween(easing: Easing = OrdraMotion.decelerate): TweenSpec<T> =
    tween(OrdraMotion.MICRO_MS, easing = easing)

fun <T> standardTween(easing: Easing = OrdraMotion.decelerate): TweenSpec<T> =
    tween(OrdraMotion.STANDARD_MS, easing = easing)

fun <T> emphasisTween(easing: Easing = OrdraMotion.decelerate): TweenSpec<T> =
    tween(OrdraMotion.EMPHASIS_MS, easing = easing)
