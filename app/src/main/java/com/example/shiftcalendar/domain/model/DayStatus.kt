package com.example.shiftcalendar.domain.model

import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import kotlinx.datetime.LocalDate

enum class CalendarType { WORK_DAY, WEEKEND, HOLIDAY }

data class DayStatus(
    val date: LocalDate,
    val calendarType: CalendarType,
    val activePeriods: List<ShiftPeriod>,
    val roadPeriods: List<ShiftPeriod>,
    val isNight: Boolean
) {
    val isWorking: Boolean get() = activePeriods.isNotEmpty()
    val isOnRoad: Boolean get() = roadPeriods.isNotEmpty()
    val isEmpty: Boolean get() = activePeriods.isEmpty() && roadPeriods.isEmpty()
}
