package com.example.shiftcalendar.data.repository

import com.example.shiftcalendar.data.db.dao.DayOverrideDao
import com.example.shiftcalendar.data.db.dao.HoursOverrideDao
import com.example.shiftcalendar.data.db.entity.DayOverride
import com.example.shiftcalendar.data.db.entity.HoursOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class OverrideRepository(
    private val dayDao: DayOverrideDao,
    private val hoursDao: HoursOverrideDao
) {
    fun observeDayOverrides(): Flow<List<DayOverride>> = dayDao.observeAll()

    fun observeHoursOverrides(personId: Long): Flow<List<HoursOverride>> =
        hoursDao.observeByPerson(personId)

    suspend fun setDayOverride(override: DayOverride) = dayDao.upsert(override)

    suspend fun deleteDayOverride(override: DayOverride) = dayDao.delete(override)

    suspend fun setHoursOverride(override: HoursOverride) = hoursDao.upsert(override)

    suspend fun deleteHoursOverride(personId: Long, date: LocalDate) =
        hoursDao.deleteByIds(personId, date)

    suspend fun getHoursOverridesForYear(personId: Long, year: Int): List<HoursOverride> {
        val from = LocalDate(year, 1, 1)
        val to = LocalDate(year, 12, 31)
        return hoursDao.getForPersonBetween(personId, from, to)
    }

    suspend fun getAllHoursOverrides(): List<HoursOverride> = hoursDao.getAll()

    suspend fun getDayOverridesFor(crewId: Long, personId: Long): List<DayOverride> =
        dayDao.getForCrewOrPerson(crewId, personId)
}
