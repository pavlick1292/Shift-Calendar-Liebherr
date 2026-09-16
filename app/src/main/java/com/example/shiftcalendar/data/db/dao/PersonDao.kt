package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shiftcalendar.data.db.entity.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY fullName")
    fun observeAll(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getById(id: Long): Person?

    @Query("SELECT * FROM persons WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Person>

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)
}
