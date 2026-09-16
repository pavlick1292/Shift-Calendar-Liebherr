package com.example.shiftcalendar.data.calendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ProductionCalendarJson(
    val year: Int,
    val holidays: List<String> = emptyList(),
    val workingWeekends: List<String> = emptyList()
)

data class ProductionCalendar(
    val year: Int,
    val holidays: Set<LocalDate>,
    val workingWeekends: Set<LocalDate>
) {
    fun isHoliday(date: LocalDate): Boolean = date in holidays

    fun isWeekend(date: LocalDate): Boolean {
        if (date in workingWeekends) return false
        return date.dayOfWeek == DayOfWeek.SATURDAY ||
               date.dayOfWeek == DayOfWeek.SUNDAY
    }

    fun isWorkingDay(date: LocalDate): Boolean = !isHoliday(date) && !isWeekend(date)
}
