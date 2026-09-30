package com.jesusjbriceno.alborada.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single alarm. Days are stored as a bitmask of [java.util.Calendar.DAY_OF_WEEK]
 * values (bit 0 = SUNDAY … bit 6 = SATURDAY) so the entity stays plain.
 * Use [com.jesusjbriceno.alborada.domain.AlarmDays] to convert to/from a [Set].
 */
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val daysBitmask: Int = 0,
    val enabled: Boolean = true,
    val label: String = "",
    /** Minutes before the alarm time the simulated sunrise (and tone) start. */
    val anticipationMinutes: Int = DEFAULT_ANTICIPATION_MINUTES,
    /** Sound media uri; empty = the default bundled dawn tone. */
    val soundUri: String = "",
) {
    companion object {
        const val DEFAULT_ANTICIPATION_MINUTES = 15
        const val MIN_ANTICIPATION_MINUTES = 0
        const val MAX_ANTICIPATION_MINUTES = 60

        /** A one-shot alarm that never repeats. */
        fun once(
            hour: Int,
            minute: Int,
            label: String = "",
        ) = Alarm(hour = hour, minute = minute, daysBitmask = 0, label = label)
    }
}
