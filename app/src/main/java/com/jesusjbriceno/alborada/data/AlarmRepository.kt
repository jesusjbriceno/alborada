package com.jesusjbriceno.alborada.data

import com.jesusjbriceno.alborada.data.local.Alarm
import com.jesusjbriceno.alborada.data.local.AlarmDao
import kotlinx.coroutines.flow.Flow

/** Single source of truth for alarm persistence. */
class AlarmRepository(
    private val dao: AlarmDao,
) {
    fun observeAll(): Flow<List<Alarm>> = dao.observeAll()

    suspend fun getById(id: Long): Alarm? = dao.getById(id)

    suspend fun getEnabled(): List<Alarm> = dao.getEnabled()

    /** Inserts a new alarm and returns its id, or updates an existing one. */
    suspend fun upsert(alarm: Alarm): Long =
        if (alarm.id == 0L) {
            dao.insert(alarm)
        } else {
            dao.update(alarm)
            alarm.id
        }

    suspend fun setEnabled(
        alarm: Alarm,
        enabled: Boolean,
    ) {
        dao.update(alarm.copy(enabled = enabled))
    }

    suspend fun delete(alarm: Alarm) {
        dao.delete(alarm)
    }
}
