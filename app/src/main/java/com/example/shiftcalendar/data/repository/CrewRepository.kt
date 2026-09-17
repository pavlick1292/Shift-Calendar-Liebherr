package com.example.shiftcalendar.data.repository

import com.example.shiftcalendar.data.db.dao.CrewDao
import com.example.shiftcalendar.data.db.dao.CrewMemberDao
import com.example.shiftcalendar.data.db.dao.ShiftPeriodDao
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class CrewWithPeriods(
    val crew: Crew,
    val periods: List<ShiftPeriod>,
    val memberCount: Int = 0
)

class CrewRepository(
    private val crewDao: CrewDao,
    private val periodDao: ShiftPeriodDao,
    private val memberDao: CrewMemberDao
) {
    fun observeCrews(): Flow<List<Crew>> = crewDao.observeAll()

    fun observeCrewsWithPeriods(): Flow<List<CrewWithPeriods>> =
        combine(
            crewDao.observeAll(),
            periodDao.observeAll(),
            memberDao.observeAll()
        ) { crews, periods, members ->
            val periodsByCrew = periods.groupBy { it.crewId }
            val memberCountByCrew = members.groupBy { it.crewId }.mapValues { it.value.size }
            crews.map { crew ->
                CrewWithPeriods(
                    crew = crew,
                    periods = periodsByCrew[crew.id].orEmpty().sortedBy { it.startDate },
                    memberCount = memberCountByCrew[crew.id] ?: 0
                )
            }
        }

    fun observeCrew(id: Long): Flow<Crew?> = crewDao.observeById(id)

    fun observePeriods(crewId: Long): Flow<List<ShiftPeriod>> = periodDao.observeByCrew(crewId)

    suspend fun getCrew(id: Long): Crew? = crewDao.getById(id)

    suspend fun createCrew(crew: Crew): Long = crewDao.insert(crew)

    suspend fun updateCrew(crew: Crew) = crewDao.update(crew)

    suspend fun deleteCrew(crew: Crew) = crewDao.delete(crew)

    suspend fun createPeriod(period: ShiftPeriod): Long = periodDao.insert(period)

    suspend fun createPeriods(periods: List<ShiftPeriod>) = periodDao.insertAll(periods)

    suspend fun updatePeriod(period: ShiftPeriod) = periodDao.update(period)

    suspend fun deletePeriod(period: ShiftPeriod) = periodDao.delete(period)

    suspend fun deletePeriodsForCrew(crewId: Long) = periodDao.deleteAllForCrew(crewId)

    suspend fun generatePeriodsByTemplate(
        crewId: Long,
        startDate: LocalDate,
        shiftDays: Int,
        restDays: Int,
        count: Int
    ): List<ShiftPeriod> {
        val result = mutableListOf<ShiftPeriod>()
        var cursor = startDate
        repeat(count) { i ->
            val end = cursor.plus(DatePeriod(days = shiftDays - 1))
            result += ShiftPeriod(
                crewId = crewId,
                startDate = cursor,
                endDate = end,
                label = "Вахта N" + (i + 1)
            )
            cursor = end.plus(DatePeriod(days = restDays + 1))
        }
        periodDao.insertAll(result)
        return result
    }
}
