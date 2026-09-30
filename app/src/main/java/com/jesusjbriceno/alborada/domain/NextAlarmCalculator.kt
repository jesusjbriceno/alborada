package com.jesusjbriceno.alborada.domain

import com.jesusjbriceno.alborada.data.local.Alarm
import java.util.Calendar
import java.util.TimeZone

/**
 * Pure calculation of the next firing time of an [Alarm]. The only "clock" in
 * the project, so it is kept dependency-free and unit-tested against a fixed
 * instant and time zone.
 *
 * Semantics: the next occurrence is always strictly after [fromMillis].
 * An alarm with an empty day set fires once at the next occurrence of its
 * time (today if still ahead, otherwise tomorrow); a repeating alarm fires
 * on the next allowed day.
 */
object NextAlarmCalculator {
    private const val MAX_SCAN_DAYS = 8

    fun nextOccurrenceMillis(
        alarm: Alarm,
        fromMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Long? {
        if (!alarm.enabled) return null

        val calendar = Calendar.getInstance(zone).apply { timeInMillis = fromMillis }
        val alarmMillisOfDay = alarm.hour * MILLIS_PER_HOUR + alarm.minute * MILLIS_PER_MINUTE
        val fromMillisOfDay = millisOfDay(calendar)

        if (alarm.daysBitmask == 0) {
            // One-shot: the next occurrence of the time — today if still
            // strictly ahead, otherwise tomorrow (so an alarm set at night
            // for the morning fires as expected).
            if (alarmMillisOfDay > fromMillisOfDay) return atTime(calendar, alarm)
            calendar.timeInMillis = fromMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return atTime(calendar, alarm)
        }

        // Repeating: today counts only while its time is strictly ahead
        // (an alarm firing at 07:00 must not find 07:00:00 as its next run).
        if (AlarmDays.containsDay(alarm.daysBitmask, calendar.get(Calendar.DAY_OF_WEEK)) &&
            alarmMillisOfDay > fromMillisOfDay
        ) {
            return atTime(calendar, alarm)
        }

        for (offset in 1..MAX_SCAN_DAYS) {
            calendar.timeInMillis = fromMillis
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            if (AlarmDays.containsDay(alarm.daysBitmask, calendar.get(Calendar.DAY_OF_WEEK))) {
                return atTime(calendar, alarm)
            }
        }
        return null
    }

    private fun atTime(calendar: Calendar, alarm: Alarm): Long {
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun millisOfDay(calendar: Calendar): Long =
        calendar.get(Calendar.HOUR_OF_DAY) * MILLIS_PER_HOUR +
            calendar.get(Calendar.MINUTE) * MILLIS_PER_MINUTE +
            calendar.get(Calendar.SECOND) * MILLIS_PER_SECOND +
            calendar.get(Calendar.MILLISECOND)

    private const val MILLIS_PER_SECOND = 1_000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L
}
