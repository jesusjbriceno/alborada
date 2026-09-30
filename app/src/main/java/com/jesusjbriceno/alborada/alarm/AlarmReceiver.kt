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
 * Fires at the sunrise start (or at the alarm time when anticipation is 0).
 * Opens the full-screen [AlarmActivity] and, for repeating alarms, re-arms
 * the next occurrence.
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
                val alarmStartEpoch =
                    intent.getLongExtra(EXTRA_ALARM_START_EPOCH, System.currentTimeMillis())

                context.startActivity(
                    Intent(context, AlarmActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP,
                        )
                        putExtra(EXTRA_ALARM_ID, alarm.id)
                        putExtra(EXTRA_ALARM_START_EPOCH, alarmStartEpoch)
                        putExtra(EXTRA_ANTICIPATION_MINUTES, alarm.anticipationMinutes)
                        putExtra(EXTRA_ALARM_LABEL, alarm.label)
                    },
                )

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
        const val EXTRA_ALARM_START_EPOCH = "alarm_start_epoch"
        const val EXTRA_ANTICIPATION_MINUTES = "anticipation_minutes"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val NO_ID = -1L
    }
}
