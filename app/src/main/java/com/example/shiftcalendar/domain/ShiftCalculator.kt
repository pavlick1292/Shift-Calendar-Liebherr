package com.example.shiftcalendar.domain

import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.domain.model.CalendarType
import com.example.shiftcalendar.domain.model.DayStatus
import kotlinx.datetime.LocalDate

class ShiftCalculator(private val calendar: ProductionCalendar) {

    fun dayStatusFor(
        date: LocalDate,
        periods: List<ShiftPeriod>,
        memberships: List<CrewMember> = emptyList()
    ): DayStatus {
        val active = mutableListOf<ShiftPeriod>()

        for (p in periods) {
            if (date >= p.startDate && date <= p.endDate) {
                active += p
            }
        }

        val calendarType = when {
            calendar.isHoliday(date) -> CalendarType.HOLIDAY
            calendar.isWeekend(date) -> CalendarType.WEEKEND
            else -> CalendarType.WORK_DAY
        }

        return DayStatus(
            date = date,
            calendarType = calendarType,
            activePeriods = active,
            roadPeriods = emptyList()
        )
    }
}
