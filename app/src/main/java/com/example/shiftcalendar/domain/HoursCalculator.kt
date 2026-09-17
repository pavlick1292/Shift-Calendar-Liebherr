package com.example.shiftcalendar.domain

import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.HoursCategory
import com.example.shiftcalendar.data.db.entity.HoursOverride
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.domain.model.WorkHours
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class HoursCalculator(
    private val shiftCalc: ShiftCalculator,
    private val settings: HoursSettings
) {
    fun calculateForPerson(
        personId: Long,
        year: Int,
        memberships: List<CrewMember>,
        periodsByCrew: Map<Long, List<ShiftPeriod>>,
        hoursOverrides: List<HoursOverride>
    ): WorkHours {
        var regular = 0.0

        val overrideByDate = hoursOverrides.associateBy { it.date }

        var date = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)

        while (date <= end) {
            val override = overrideByDate[date]
            if (override != null) {
                regular += override.hours
            } else {
                var dayAdded = false
                for (m in memberships) {
                    val periods = periodsByCrew[m.crewId].orEmpty()
                    val status = shiftCalc.dayStatusFor(date, periods, listOf(m))

                    if (status.isWorking && !dayAdded) {
                        regular += settings.effectiveShiftHours
                        dayAdded = true
                    }
                }
            }
            date = date.plus(DatePeriod(days = 1))
        }

        return WorkHours(
            personId = personId,
            year = year,
            regularHours = regular,
            yearlyNorm = settings.yearlyNorm
        )
    }
}
