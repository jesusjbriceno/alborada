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
 * Re-arms every enabled alarm after the system events that can wipe or
 * invalidate pending alarms: boot, package upgrade, and clock/timezone change.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action ?: return
        if (action !in HANDLED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AlarmRepository(AppDatabase.get(context).alarmDao())
                val scheduler = AlarmManagerScheduler(context)
                repository.getEnabled().forEach { scheduler.scheduleNext(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val HANDLED_ACTIONS =
            setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
            )
    }
}
