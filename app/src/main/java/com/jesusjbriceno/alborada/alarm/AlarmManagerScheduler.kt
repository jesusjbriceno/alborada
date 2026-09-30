package com.jesusjbriceno.alborada.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jesusjbriceno.alborada.data.local.Alarm
import com.jesusjbriceno.alborada.domain.AlarmScheduler
import com.jesusjbriceno.alborada.domain.NextAlarmCalculator

/**
 * [AlarmScheduler] backed by [AlarmManager] with exact, while-idle alarms.
 * The event is scheduled at the *sunrise start* (anticipation before the
 * alarm time) and carries the real alarm instant so the activity knows when
 * to switch from the dawn ramp to full ringing.
 */
class AlarmManagerScheduler(
    private val context: Context,
) : AlarmScheduler {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun scheduleNext(
        alarm: Alarm,
        fromMillis: Long,
    ) {
        cancel(alarm)
        if (!alarm.enabled) return
        val next = NextAlarmCalculator.nextOccurrenceMillis(alarm, fromMillis) ?: return
        val sunriseStart = next - alarm.anticipationMinutes * MINUTE_MILLIS
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                sunriseStart,
                pendingIntentFor(alarm, next),
            )
        } else {
            // Fall back to an inexact while-idle alarm: it may ring slightly
            // late, but it must ring rather than stay silent.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                sunriseStart,
                pendingIntentFor(alarm, next),
            )
        }
    }

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(pendingIntentFor(alarm, 0L))
    }

    override fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun pendingIntentFor(
        alarm: Alarm,
        alarmStartEpoch: Long,
    ): PendingIntent {
        val intent =
            Intent(context, AlarmReceiver::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                .putExtra(AlarmReceiver.EXTRA_ALARM_START_EPOCH, alarmStartEpoch)
                .putExtra(AlarmReceiver.EXTRA_ANTICIPATION_MINUTES, alarm.anticipationMinutes)
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
