package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ShiftPeriodDao {

    @Query("SELECT * FROM shift_periods WHERE crewId = :crewId ORDER BY startDate")
    fun observeByCrew(crewId: Long): Flow<List<ShiftPeriod>>

    @Query("SELECT * FROM shift_periods ORDER BY startDate")
    fun observeAll(): Flow<List<ShiftPeriod>>

    @Query("SELECT * FROM shift_periods ORDER BY startDate")
    suspend fun getAllOnce(): List<ShiftPeriod>

    @Insert
    suspend fun insert(period: ShiftPeriod): Long

    @Insert
    suspend fun insertAll(periods: List<ShiftPeriod>)

    @Update
    suspend fun update(period: ShiftPeriod)

    @Delete
    suspend fun delete(period: ShiftPeriod)

    @Query("DELETE FROM shift_periods WHERE crewId = :crewId")
    suspend fun deleteAllForCrew(crewId: Long)
}
