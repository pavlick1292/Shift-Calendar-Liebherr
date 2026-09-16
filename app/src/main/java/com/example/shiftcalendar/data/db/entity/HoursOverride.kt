package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "hours_overrides",
    primaryKeys = ["personId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId"), Index("date")]
)
data class HoursOverride(
    val personId: Long,
    val date: LocalDate,
    val hours: Double,
    val category: HoursCategory = HoursCategory.REGULAR,
    val reason: String = ""
)

enum class HoursCategory {
    REGULAR,
    NIGHT,
    ROAD,
    OVERTIME,
    SICK,
    VACATION
}
