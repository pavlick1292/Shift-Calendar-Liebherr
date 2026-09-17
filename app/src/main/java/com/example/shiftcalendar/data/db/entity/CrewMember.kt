package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "crew_members",
    primaryKeys = ["personId", "crewId"],
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Crew::class,
            parentColumns = ["id"],
            childColumns = ["crewId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("crewId"), Index("personId")]
)
data class CrewMember(
    val personId: Long,
    val crewId: Long,
    val roleInCrew: String = "",
    val joinedAt: LocalDate? = null
)
