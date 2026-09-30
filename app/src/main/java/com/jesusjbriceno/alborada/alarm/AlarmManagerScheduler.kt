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
 * [AlarmScheduler] backed by [AlarmManager] with exact, while-idle alarms so
 * the system wakes the device and fires on time.
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
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next,
            pendingIntentFor(alarm.id),
        )
    }

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(pendingIntentFor(alarm.id))
    }

    override fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun pendingIntentFor(alarmId: Long): PendingIntent {
        val intent =
            Intent(context, AlarmReceiver::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
