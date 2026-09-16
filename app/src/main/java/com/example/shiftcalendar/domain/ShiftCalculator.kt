package com.example.shiftcalendar.domain

import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.domain.model.CalendarType
import com.example.shiftcalendar.domain.model.DayStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class ShiftCalculator(private val calendar: ProductionCalendar) {

    fun dayStatusFor(
        date: LocalDate,
        periods: List<ShiftPeriod>,
        memberships: List<CrewMember> = emptyList()
    ): DayStatus {
        val active = mutableListOf<ShiftPeriod>()
        val road = mutableListOf<ShiftPeriod>()
        var night = false

        for (p in periods) {
            val roadStart = p.startDate.minus(DatePeriod(days = p.roadDaysBefore))
            val roadEnd = p.endDate.plus(DatePeriod(days = p.roadDaysAfter))

            when {
                date >= roadStart && date < p.startDate -> road += p
                date >= p.startDate && date <= p.endDate -> {
                    active += p
                    val memberNight = memberships
                        .firstOrNull { it.crewId == p.crewId }
                        ?.worksAtNight == true
                    if (p.isNightShift || memberNight) night = true
                }
                date > p.endDate && date <= roadEnd -> road += p
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
            roadPeriods = road,
            isNight = night
        )
    }
}
