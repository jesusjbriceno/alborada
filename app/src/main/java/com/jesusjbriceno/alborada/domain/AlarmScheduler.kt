package com.jesusjbriceno.alborada.domain

import com.jesusjbriceno.alborada.data.local.Alarm

/**
 * Schedules alarms with the OS. The Android implementation talks to
 * [android.app.AlarmManager]; the interface keeps receivers/UI decoupled
 * and testable.
 */
interface AlarmScheduler {
    /** Schedules the next occurrence of [alarm], strictly after [fromMillis]. */
    fun scheduleNext(
        alarm: Alarm,
        fromMillis: Long = System.currentTimeMillis(),
    )

    /** Removes any pending scheduling for [alarm]. */
    fun cancel(alarm: Alarm)

    /** Whether exact alarms may be scheduled (Android 12+ permission). */
    fun canScheduleExact(): Boolean

    companion object {
        /** Optional label of a repeating alarm (days set). */
        const val REPEAT_MINIMUM_INTERVAL_MS = 60_000L
    }
}
