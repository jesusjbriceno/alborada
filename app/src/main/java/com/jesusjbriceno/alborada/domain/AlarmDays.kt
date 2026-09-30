package com.jesusjbriceno.alborada.domain

import java.util.Calendar

/**
 * Bitmask helpers for `Alarm.daysBitmask`, aligned with
 * [Calendar.DAY_OF_WEEK]: bit 0 = SUNDAY … bit 6 = SATURDAY.
 */
object AlarmDays {
    const val SUNDAY = Calendar.SUNDAY
    const val MONDAY = Calendar.MONDAY
    const val TUESDAY = Calendar.TUESDAY
    const val WEDNESDAY = Calendar.WEDNESDAY
    const val THURSDAY = Calendar.THURSDAY
    const val FRIDAY = Calendar.FRIDAY
    const val SATURDAY = Calendar.SATURDAY

    /** All days in [Calendar.DAY_OF_WEEK] order, SUNDAY first. */
    val ALL: Set<Int> = setOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)

    fun toBitmask(days: Set<Int>): Int = days.fold(0) { mask, day -> mask or (1 shl (day - SUNDAY)) }

    fun fromBitmask(mask: Int): Set<Int> = ALL.filterTo(mutableSetOf()) { day -> mask and (1 shl (day - SUNDAY)) != 0 }

    fun containsDay(
        mask: Int,
        dayOfWeek: Int,
    ): Boolean = mask and (1 shl (dayOfWeek - SUNDAY)) != 0
}
