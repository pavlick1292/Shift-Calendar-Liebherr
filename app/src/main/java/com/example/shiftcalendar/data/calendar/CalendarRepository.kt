package com.example.shiftcalendar.data.calendar

import android.content.Context
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class CalendarRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<Int, ProductionCalendar>()

    fun get(year: Int): ProductionCalendar {
        cache[year]?.let { return it }

        val resId = context.resources.getIdentifier(
            "production_calendar_ru_$year",
            "raw",
            context.packageName
        )

        val calendar = if (resId != 0) {
            try {
                val text = context.resources.openRawResource(resId)
                    .bufferedReader().use { it.readText() }
                val parsed = json.decodeFromString<ProductionCalendarJson>(text)
                ProductionCalendar(
                    year = parsed.year,
                    holidays = parsed.holidays.map { LocalDate.parse(it) }.toSet(),
                    workingWeekends = parsed.workingWeekends.map { LocalDate.parse(it) }.toSet()
                )
            } catch (e: Exception) {
                emptyCalendar(year)
            }
        } else {
            emptyCalendar(year)
        }

        cache[year] = calendar
        return calendar
    }

    private fun emptyCalendar(year: Int) = ProductionCalendar(
        year = year,
        holidays = emptySet(),
        workingWeekends = emptySet()
    )
}
