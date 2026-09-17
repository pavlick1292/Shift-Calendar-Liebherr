package com.example.shiftcalendar.data.calendar

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class CalendarRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<Int, ProductionCalendar>()

    fun get(year: Int): ProductionCalendar {
        cache[year]?.let { return it }
        val calendar = loadFromRawOrFile(year)
        cache[year] = calendar
        return calendar
    }

    suspend fun refresh(year: Int): ProductionCalendar = withContext(Dispatchers.IO) {
        val cached = loadFromRawOrFile(year)
        if (cached.holidays.isNotEmpty()) {
            cache[year] = cached
            return@withContext cached
        }

        try {
            val url = URL("https://isdayoff.ru/api/getdata?year=$year&cc=ru")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            val data = conn.inputStream.bufferedReader().use { it.readText().trim() }
            conn.disconnect()

            if (data.length < 300) return@withContext cached

            val calendar = parseIsDayOff(year, data)
            saveToCache(year, data)
            cache[year] = calendar
            calendar
        } catch (e: Exception) {
            val fileCache = loadFromCache(year)
            if (fileCache != null) {
                cache[year] = fileCache
                fileCache
            } else {
                cached
            }
        }
    }

    private fun parseIsDayOff(year: Int, data: String): ProductionCalendar {
        val holidays = mutableSetOf<LocalDate>()
        val workingWeekends = mutableSetOf<LocalDate>()

        var date = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)

        var i = 0
        while (date <= end && i < data.length) {
            when (data[i]) {
                '2' -> holidays += date
                '8' -> workingWeekends += date
            }
            date = date.plusDays(1)
            i++
        }

        return ProductionCalendar(
            year = year,
            holidays = holidays,
            workingWeekends = workingWeekends,
            holidayNames = emptyMap()
        )
    }

    private fun loadFromRawOrFile(year: Int): ProductionCalendar {
        val resId = context.resources.getIdentifier(
            "production_calendar_ru_$year",
            "raw",
            context.packageName
        )
        if (resId != 0) {
            try {
                val text = context.resources.openRawResource(resId)
                    .bufferedReader().use { it.readText() }
                val parsed = json.decodeFromString<ProductionCalendarJson>(text)
                return ProductionCalendar(
                    year = parsed.year,
                    holidays = parsed.holidays.map { LocalDate.parse(it) }.toSet(),
                    workingWeekends = parsed.workingWeekends.map { LocalDate.parse(it) }.toSet(),
                    holidayNames = parsed.holidayNames.mapKeys { LocalDate.parse(it.key) }
                )
            } catch (e: Exception) {
                // fallthrough
            }
        }
        return loadFromCache(year) ?: emptyCalendar(year)
    }

    private fun cacheFile(year: Int): File =
        File(context.filesDir, "calendar_cache_$year.txt")

    private fun saveToCache(year: Int, data: String) {
        try {
            cacheFile(year).writeText(data)
        } catch (e: Exception) { }
    }

    private fun loadFromCache(year: Int): ProductionCalendar? {
        val f = cacheFile(year)
        if (!f.exists()) return null
        return try {
            val data = f.readText().trim()
            if (data.length < 300) return null
            parseIsDayOff(year, data)
        } catch (e: Exception) {
            null
        }
    }

    private fun emptyCalendar(year: Int) = ProductionCalendar(
        year = year,
        holidays = emptySet(),
        workingWeekends = emptySet(),
        holidayNames = emptyMap()
    )
}

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(this.toEpochDays() + days)
