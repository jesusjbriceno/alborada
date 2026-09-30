package com.jesusjbriceno.alborada.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jesusjbriceno.alborada.data.AlarmRepository
import com.jesusjbriceno.alborada.data.local.Alarm
import com.jesusjbriceno.alborada.data.local.AppDatabase
import com.jesusjbriceno.alborada.domain.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmListViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val app: Application = application
    private val repository =
        AlarmRepository(AppDatabase.get(app).alarmDao())
    private val scheduler: AlarmScheduler = AlarmManagerScheduler(app)

    val alarms: StateFlow<List<Alarm>> =
        repository
            .observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _canScheduleExact = MutableStateFlow(scheduler.canScheduleExact())
    val canScheduleExact: StateFlow<Boolean> = _canScheduleExact

    private val _canUseFullScreenIntent =
        MutableStateFlow(RingingNotifier.canUseFullScreenIntent(app))
    val canUseFullScreenIntent: StateFlow<Boolean> = _canUseFullScreenIntent

    /** Refresh after returning from the permission settings screens. */
    fun refreshPermissions() {
        _canScheduleExact.value = scheduler.canScheduleExact()
        _canUseFullScreenIntent.value = RingingNotifier.canUseFullScreenIntent(app)
    }

    fun upsert(
        id: Long,
        hour: Int,
        minute: Int,
        days: Set<Int>,
        label: String,
        anticipationMinutes: Int,
        soundUri: String,
    ) {
        viewModelScope.launch {
            when (id) {
                0L -> {
                    val alarm =
                        Alarm(
                            hour = hour,
                            minute = minute,
                            daysBitmask =
                                com.jesusjbriceno.alborada.domain.AlarmDays
                                    .toBitmask(days),
                            label = label,
                            anticipationMinutes = anticipationMinutes,
                            soundUri = soundUri,
                        )
                    val savedId = repository.upsert(alarm)
                    reschedule(alarm.copy(id = savedId))
                }

                else -> {
                    val current = repository.getById(id) ?: return@launch
                    val updated =
                        current.copy(
                            hour = hour,
                            minute = minute,
                            daysBitmask =
                                com.jesusjbriceno.alborada.domain.AlarmDays
                                    .toBitmask(days),
                            label = label,
                            anticipationMinutes = anticipationMinutes,
                            soundUri = soundUri,
                        )
                    repository.upsert(updated)
                    reschedule(updated)
                }
            }
        }
    }

    fun toggle(alarm: Alarm) {
        viewModelScope.launch {
            val enabled = !alarm.enabled
            repository.setEnabled(alarm, enabled)
            if (enabled) reschedule(alarm.copy(enabled = true)) else scheduler.cancel(alarm)
        }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch {
            repository.delete(alarm)
            scheduler.cancel(alarm)
        }
    }

    private fun reschedule(alarm: Alarm) {
        _canScheduleExact.value = scheduler.canScheduleExact()
        if (!_canScheduleExact.value) return
        scheduler.scheduleNext(alarm)
    }
}
