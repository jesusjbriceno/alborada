package com.jesusjbriceno.alborada.alarm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Pure mapping from sunrise progress (0 = night start, 1 = alarm time) to the
 * screen color, window brightness and tone volume. No Android state here so
 * the ramp is fully unit-testable.
 */
object SunriseRenderer {
    /** (progress, color) keyframes of the simulated dawn. */
    private val PHASES =
        listOf(
            Phase(0.00f, 0xFF0E1A33),
            Phase(0.18f, 0xFF2E2A66),
            Phase(0.38f, 0xFF6B3A7D),
            Phase(0.58f, 0xFFC2452F),
            Phase(0.78f, 0xFFF5A93C),
            Phase(0.92f, 0xFFFFE9C4),
            Phase(1.00f, 0xFFFFF7E8),
        )

    private const val MIN_BRIGHTNESS = 0.05f
    private const val MAX_BRIGHTNESS = 1.0f

    data class Phase(
        val at: Float,
        val argb: Long,
    )

    /** Progress in 0..1 clamped; elapsed beyond the total pins to 1. */
    fun progress(
        elapsedMillis: Long,
        totalMillis: Long,
    ): Float {
        if (totalMillis <= 0L) return 1f
        return (elapsedMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
    }

    /** Screen color for [progress], lerped across the dawn keyframes. */
    fun phaseColor(progress: Float): Color {
        val p = progress.coerceIn(0f, 1f)
        var next = PHASES.first()
        for (phase in PHASES.drop(1)) {
            if (p <= phase.at) {
                val from = Color(PHASES[PHASES.indexOf(next)].argb)
                val to = Color(phase.argb)
                val fraction = if (phase.at == next.at) 0f else (p - next.at) / (phase.at - next.at)
                return lerp(from, to, fraction.coerceIn(0f, 1f))
            }
            next = phase
        }
        return Color(PHASES.last().argb)
    }

    /** Window brightness (screenBrightness attribute) at [progress]. */
    fun brightness(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return MIN_BRIGHTNESS + (MAX_BRIGHTNESS - MIN_BRIGHTNESS) * p
    }

    /**
     * Tone volume in 0..1: an S-curve (smoothstep) so the sound creeps in
     * during the dawn and only reaches full volume at the alarm time.
     */
    fun toneVolume(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return p * p * (3f - 2f * p)
    }
}
