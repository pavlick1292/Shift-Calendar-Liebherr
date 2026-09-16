package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "day_overrides",
    foreignKeys = [
        ForeignKey(
            entity = Crew::class,
            parentColumns = ["id"],
            childColumns = ["crewId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("crewId"), Index("personId")]
)
data class DayOverride(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val crewId: Long?,
    val personId: Long?,
    val date: LocalDate,
    val type: DayType,
    val note: String = ""
)
