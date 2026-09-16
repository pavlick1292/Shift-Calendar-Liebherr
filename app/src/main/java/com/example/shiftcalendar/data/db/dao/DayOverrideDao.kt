package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shiftcalendar.data.db.entity.DayOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface DayOverrideDao {

    @Query("SELECT * FROM day_overrides")
    fun observeAll(): Flow<List<DayOverride>>

    @Query("SELECT * FROM day_overrides WHERE crewId = :crewId OR personId = :personId")
    suspend fun getForCrewOrPerson(crewId: Long, personId: Long): List<DayOverride>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: DayOverride)

    @Delete
    suspend fun delete(override: DayOverride)
}
