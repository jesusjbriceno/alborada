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

                // Full-screen notification is the only exempt path to open the
                // ringing activity from a background receiver on modern Android.
                RingingNotifier.showRing(context, alarm, alarmStartEpoch)

                if (alarm.daysBitmask != 0) {
                    AlarmManagerScheduler(context).scheduleNext(alarm)
                } else {
                    // One-shot alarms are spent after firing.
                    repository.setEnabled(alarm, false)
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
        const val EXTRA_SOUND_URI = "sound_uri"
        const val NO_ID = -1L
    }
}
