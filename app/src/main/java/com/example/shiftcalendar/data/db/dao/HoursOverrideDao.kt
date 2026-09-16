package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shiftcalendar.data.db.entity.HoursOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HoursOverrideDao {

    @Query("SELECT * FROM hours_overrides WHERE personId = :personId ORDER BY date")
    fun observeByPerson(personId: Long): Flow<List<HoursOverride>>

    @Query("SELECT * FROM hours_overrides WHERE personId = :personId AND date >= :from AND date <= :to ORDER BY date")
    suspend fun getForPersonBetween(personId: Long, from: LocalDate, to: LocalDate): List<HoursOverride>

    @Query("SELECT * FROM hours_overrides")
    suspend fun getAll(): List<HoursOverride>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: HoursOverride)

    @Delete
    suspend fun delete(override: HoursOverride)

    @Query("DELETE FROM hours_overrides WHERE personId = :personId AND date = :date")
    suspend fun deleteByIds(personId: Long, date: LocalDate)
}
