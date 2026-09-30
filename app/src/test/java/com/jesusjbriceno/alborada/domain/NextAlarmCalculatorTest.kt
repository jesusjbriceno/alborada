package com.jesusjbriceno.alborada.domain

import com.jesusjbriceno.alborada.data.local.Alarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NextAlarmCalculatorTest {
    private val utc = TimeZone.getTimeZone("UTC")

    /** 2026-10-01 (Thursday) 22:00:00 UTC. */
    private val fromThursday22h: Long =
        run {
            val c =
                Calendar.getInstance(utc).apply {
                    clear()
                    set(2026, Calendar.OCTOBER, 1, 22, 0, 0)
                }
            c.timeInMillis
        }

    private fun alarmAt(
        hour: Int,
        minute: Int,
        days: Set<Int> = emptySet(),
        enabled: Boolean = true,
    ) = Alarm(hour = hour, minute = minute, daysBitmask = AlarmDays.toBitmask(days), enabled = enabled)

    /** Epoch millis of a given day (UTC) at 00:00. */
    private fun dayMillis(
        year: Int,
        month: Int,
        day: Int,
    ): Long {
        val c =
            Calendar.getInstance(utc).apply {
                clear()
                set(year, month, day, 0, 0, 0)
            }
        return c.timeInMillis
    }

    private fun assertOccurrence(
        expected: Long?,
        hour: Int,
        minute: Int,
        days: Set<Int>,
        enabled: Boolean = true,
    ) {
        val alarm = alarmAt(hour, minute, days, enabled)
        assertEquals(expected, NextAlarmCalculator.nextOccurrenceMillis(alarm, fromThursday22h, utc))
    }

    @Test
    fun `disabled alarm never fires`() {
        assertOccurrence(null, 7, 0, setOf(AlarmDays.MONDAY), enabled = false)
    }

    @Test
    fun `repeating alarm fires same day when time is still ahead`() {
        // Thursday 23:30 (Thursday is in the set) -> today.
        assertOccurrence(fromThursday22h + 90 * 60_000L, 23, 30, setOf(AlarmDays.THURSDAY))
    }

    @Test
    fun `repeating alarm skips to next allowed day when today passed`() {
        // Saturday 08:00, from Thursday 22:00 -> 2026-10-03 08:00 UTC.
        assertOccurrence(dayMillis(2026, Calendar.OCTOBER, 3) + 8 * 3_600_000L, 8, 0, setOf(AlarmDays.SATURDAY))
    }

    @Test
    fun `repeating alarm fires tomorrow when tomorrow is in the set`() {
        // Friday 06:30, from Thursday 22:00 -> 2026-10-02 06:30 UTC.
        assertOccurrence(dayMillis(2026, Calendar.OCTOBER, 2) + 6 * 3_600_000L + 30 * 60_000L, 6, 30, setOf(AlarmDays.FRIDAY))
    }

    @Test
    fun `repeating alarm with a single day far ahead waits for that day`() {
        // Only Monday, from Thursday 22:00 -> 2026-10-05 07:00 UTC.
        assertOccurrence(dayMillis(2026, Calendar.OCTOBER, 5) + 7 * 3_600_000L, 7, 0, setOf(AlarmDays.MONDAY))
    }

    @Test
    fun `weekend boundary crosses midnight correctly`() {
        // Saturday 07:00, from Friday 23:59 -> 2026-10-03 07:00 UTC (Friday is 10-02).
        val fromFridayNight = dayMillis(2026, Calendar.OCTOBER, 2) + 23 * 3_600_000L + 59 * 60_000L
        val expected = dayMillis(2026, Calendar.OCTOBER, 3) + 7 * 3_600_000L
        val alarm = alarmAt(7, 0, setOf(AlarmDays.SATURDAY))
        assertEquals(expected, NextAlarmCalculator.nextOccurrenceMillis(alarm, fromFridayNight, utc))
    }

    @Test
    fun `exact firing time is not considered an occurrence`() {
        // Alarm at 07:00, asked at exactly 07:00:00 on Thursday -> next Thursday (2026-10-08).
        val atSeven = dayMillis(2026, Calendar.OCTOBER, 1) + 7 * 3_600_000L
        val expected = dayMillis(2026, Calendar.OCTOBER, 8) + 7 * 3_600_000L
        val alarm = alarmAt(7, 0, setOf(AlarmDays.THURSDAY))
        assertEquals(expected, NextAlarmCalculator.nextOccurrenceMillis(alarm, atSeven, utc))
    }

    @Test
    fun `one shot alarm fires today when time is ahead`() {
        assertOccurrence(fromThursday22h + 60 * 60_000L, 23, 0, emptySet())
    }

    @Test
    fun `one shot alarm has no occurrence once time passed`() {
        assertOccurrence(null, 21, 0, emptySet())
    }

    @Test
    fun `empty day set with a past time returns null`() {
        assertOccurrence(null, 22, 0, emptySet())
    }
}
