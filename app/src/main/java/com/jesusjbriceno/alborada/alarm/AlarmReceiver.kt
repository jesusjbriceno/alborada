package com.jesusjbriceno.alborada.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jesusjbriceno.alborada.data.AlarmRepository
import com.jesusjbriceno.alborada.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fires when a scheduled alarm comes due. Shows the wake-up notification and,
 * for repeating alarms, re-arms the next occurrence.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, NO_ID)
        if (alarmId == NO_ID) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AlarmRepository(AppDatabase.get(context).alarmDao())
                val alarm = repository.getById(alarmId) ?: return@launch
                AlarmNotifier.show(context, alarm)
                if (alarm.daysBitmask != 0) {
                    AlarmManagerScheduler(context).scheduleNext(alarm)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val NO_ID = -1L
    }
}
