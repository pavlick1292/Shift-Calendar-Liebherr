package com.example.shiftcalendar

import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.domain.HoursCalculator
import com.example.shiftcalendar.domain.ShiftCalculator
import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class HoursCalculatorTest {

    private val calendar = ProductionCalendar(2025, emptySet(), emptySet())
    private val settings = HoursSettings()
    private val calc = HoursCalculator(ShiftCalculator(calendar), settings)

    @Test
    fun `one shift day equals 11 hours`() {
        val period = ShiftPeriod(
            id = 1, crewId = 1,
            startDate = LocalDate(2025, 1, 1),
            endDate = LocalDate(2025, 1, 10),
            roadDaysBefore = 0, roadDaysAfter = 0
        )
        val membership = CrewMember(personId = 1, crewId = 1)
        val result = calc.calculateForPerson(
            personId = 1, year = 2025,
            memberships = listOf(membership),
            periodsByCrew = mapOf(1L to listOf(period)),
            hoursOverrides = emptyList()
        )
        assertThat(result.regularHours).isEqualTo(110.0)
    }

    @Test
    fun `night shift counted without coefficient`() {
        val period = ShiftPeriod(
            id = 1, crewId = 1,
            startDate = LocalDate(2025, 1, 1),
            endDate = LocalDate(2025, 1, 5),
            roadDaysBefore = 0, roadDaysAfter = 0,
            isNightShift = true
        )
        val membership = CrewMember(personId = 1, crewId = 1, worksAtNight = true)
        val result = calc.calculateForPerson(
            personId = 1, year = 2025,
            memberships = listOf(membership),
            periodsByCrew = mapOf(1L to listOf(period)),
            hoursOverrides = emptyList()
        )
        assertThat(result.nightHours).isEqualTo(55.0)
    }
}
