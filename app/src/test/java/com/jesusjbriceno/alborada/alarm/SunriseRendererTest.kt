package com.jesusjbriceno.alborada.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SunriseRendererTest {
    @Test
    fun `progress is linear and clamps at both ends`() {
        assertEquals(0f, SunriseRenderer.progress(0L, 1_800_000L))
        assertEquals(0.5f, SunriseRenderer.progress(900_000L, 1_800_000L), 1e-6f)
        assertEquals(1f, SunriseRenderer.progress(1_800_000L, 1_800_000L))
        assertEquals(1f, SunriseRenderer.progress(3_600_000L, 1_800_000L))
        assertEquals(0f, SunriseRenderer.progress(-5_000L, 1_800_000L))
        assertEquals(1f, SunriseRenderer.progress(100L, 0L))
    }

    @Test
    fun `brightness stays inside window limits and grows with progress`() {
        assertEquals(0.05f, SunriseRenderer.brightness(0f), 1e-6f)
        assertEquals(1f, SunriseRenderer.brightness(1f), 1e-6f)
        var last = -1f
        for (i in 0..10) {
            val b = SunriseRenderer.brightness(i / 10f)
            assertTrue("monotonic", b >= last)
            assertTrue("within [0.05, 1]", b in 0.05f..1f)
            last = b
        }
    }

    @Test
    fun `tone volume is an S-curve from 0 to 1`() {
        assertEquals(0f, SunriseRenderer.toneVolume(0f), 1e-6f)
        assertEquals(1f, SunriseRenderer.toneVolume(1f), 1e-6f)
        // smoothstep(0.5) == 0.5 and the ramp is symmetric.
        assertEquals(0.5f, SunriseRenderer.toneVolume(0.5f), 1e-6f)
        // Volume stays low during the early dawn (below linear).
        assertTrue(SunriseRenderer.toneVolume(0.25f) < 0.25f)
        assertTrue(SunriseRenderer.toneVolume(0.75f) > 0.75f)
    }

    @Test
    fun `phase color starts at night and ends at bright dawn`() {
        val night = SunriseRenderer.phaseColor(0f)
        val dawn = SunriseRenderer.phaseColor(1f)
        // Night is dark (small luminance), dawn is bright and warm-tinted.
        assertTrue(night.red + night.green + night.blue < 0.4f)
        assertTrue(dawn.red + dawn.green + dawn.blue > 2.0f)
        // Red channel dominates more in the middle of the ramp than at night.
        val mid = SunriseRenderer.phaseColor(0.5f)
        assertTrue(mid.red > night.red)
    }

    @Test
    fun `phase color is monotonic in overall luminance`() {
        var last = -1f
        for (i in 0..20) {
            val c = SunriseRenderer.phaseColor(i / 20f)
            val luminance = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
            assertTrue("step $i luminance $luminance >= $last", luminance >= last - 1e-4f)
            last = luminance
        }
    }
}
