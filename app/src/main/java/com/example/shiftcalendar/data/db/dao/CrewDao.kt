package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shiftcalendar.data.db.entity.Crew
import kotlinx.coroutines.flow.Flow

@Dao
interface CrewDao {
    @Query("SELECT * FROM crews WHERE isArchived = 0 ORDER BY createdAt")
    fun observeAll(): Flow<List<Crew>>

    @Query("SELECT * FROM crews WHERE isArchived = 0 ORDER BY createdAt")
    suspend fun getAllOnce(): List<Crew>

    @Query("SELECT * FROM crews WHERE id = :id")
    suspend fun getById(id: Long): Crew?

    @Query("SELECT * FROM crews WHERE id = :id")
    fun observeById(id: Long): Flow<Crew?>

    @Insert
    suspend fun insert(crew: Crew): Long

    @Update
    suspend fun update(crew: Crew)

    @Delete
    suspend fun delete(crew: Crew)

    @Query("UPDATE crews SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)
}
