package com.example.shiftcalendar.data.repository

import com.example.shiftcalendar.data.db.dao.CrewMemberDao
import com.example.shiftcalendar.data.db.dao.PersonDao
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.Person
import kotlinx.coroutines.flow.Flow

class PersonRepository(
    private val personDao: PersonDao,
    private val memberDao: CrewMemberDao
) {
    fun observePeople(): Flow<List<Person>> = personDao.observeAll()

    fun observeMembersOfCrew(crewId: Long): Flow<List<PersonWithMembership>> =
        memberDao.observeMembersOfCrew(crewId)

    fun observeMembershipsOfPerson(personId: Long): Flow<List<CrewMember>> =
        memberDao.observeByPerson(personId)

    suspend fun getPerson(id: Long): Person? = personDao.getById(id)

    suspend fun getPeople(ids: List<Long>): List<Person> = personDao.getByIds(ids)

    suspend fun createPerson(person: Person): Long = personDao.insert(person)

    suspend fun updatePerson(person: Person) = personDao.update(person)

    suspend fun deletePerson(person: Person) = personDao.delete(person)

    suspend fun addToCrew(personId: Long, crewId: Long) {
        memberDao.upsert(CrewMember(personId = personId, crewId = crewId))
    }

    suspend fun updateMembership(member: CrewMember) = memberDao.upsert(member)

    suspend fun removeFromCrew(personId: Long, crewId: Long) =
        memberDao.deleteByIds(personId, crewId)

    suspend fun getMembershipsOfPerson(personId: Long): List<CrewMember> =
        memberDao.getByPerson(personId)
}
