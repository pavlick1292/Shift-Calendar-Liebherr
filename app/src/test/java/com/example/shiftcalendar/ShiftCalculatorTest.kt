package com.example.shiftcalendar

import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.domain.ShiftCalculator
import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class ShiftCalculatorTest {

    private val calendar = ProductionCalendar(
        year = 2025,
        holidays = setOf(LocalDate(2025, 1, 1), LocalDate(2025, 1, 7)),
        workingWeekends = emptySet()
    )
    private val calc = ShiftCalculator(calendar)

    @Test
    fun `active period is detected`() {
        val p = ShiftPeriod(
            id = 1, crewId = 1,
            startDate = LocalDate(2025, 1, 5),
            endDate = LocalDate(2025, 1, 25),
            roadDaysBefore = 1, roadDaysAfter = 1
        )
        val status = calc.dayStatusFor(LocalDate(2025, 1, 10), listOf(p))
        assertThat(status.activePeriods).hasSize(1)
        assertThat(status.isWorking).isTrue()
    }

    @Test
    fun `road before period is detected`() {
        val p = ShiftPeriod(
            id = 1, crewId = 1,
            startDate = LocalDate(2025, 1, 5),
            endDate = LocalDate(2025, 1, 25),
            roadDaysBefore = 2, roadDaysAfter = 1
        )
        val status = calc.dayStatusFor(LocalDate(2025, 1, 3), listOf(p))
        assertThat(status.roadPeriods).hasSize(1)
        assertThat(status.isOnRoad).isTrue()
    }

    @Test
    fun `holiday is recognized`() {
        val status = calc.dayStatusFor(LocalDate(2025, 1, 1), emptyList())
        assertThat(status.calendarType.name).isEqualTo("HOLIDAY")
    }

    @Test
    fun `overlapping periods both active`() {
        val p1 = ShiftPeriod(1, 1, LocalDate(2025, 1, 1), LocalDate(2025, 1, 15))
        val p2 = ShiftPeriod(2, 2, LocalDate(2025, 1, 10), LocalDate(2025, 1, 25))
        val status = calc.dayStatusFor(LocalDate(2025, 1, 12), listOf(p1, p2))
        assertThat(status.activePeriods).hasSize(2)
    }
}

