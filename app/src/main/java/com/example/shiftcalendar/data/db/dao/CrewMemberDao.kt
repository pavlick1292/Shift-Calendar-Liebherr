package com.example.shiftcalendar.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.Person
import kotlinx.coroutines.flow.Flow

data class PersonWithMembership(
    @Embedded val person: Person,
    val roleInCrew: String
)

@Dao
interface CrewMemberDao {

    @Query("SELECT * FROM crew_members")
    fun observeAll(): Flow<List<CrewMember>>

    @Query("SELECT * FROM crew_members WHERE crewId = :crewId")
    fun observeByCrew(crewId: Long): Flow<List<CrewMember>>

    @Query("SELECT * FROM crew_members WHERE crewId = :crewId")
    suspend fun getByCrewOnce(crewId: Long): List<CrewMember>

    @Query("SELECT * FROM crew_members WHERE personId = :personId")
    fun observeByPerson(personId: Long): Flow<List<CrewMember>>

    @Query("SELECT * FROM crew_members WHERE personId = :personId")
    suspend fun getByPerson(personId: Long): List<CrewMember>

    @Query("SELECT p.*, cm.roleInCrew AS roleInCrew FROM persons p INNER JOIN crew_members cm ON cm.personId = p.id WHERE cm.crewId = :crewId ORDER BY p.fullName")
    fun observeMembersOfCrew(crewId: Long): Flow<List<PersonWithMembership>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: CrewMember)

    @Delete
    suspend fun delete(member: CrewMember)

    @Query("DELETE FROM crew_members WHERE personId = :personId AND crewId = :crewId")
    suspend fun deleteByIds(personId: Long, crewId: Long)
}
