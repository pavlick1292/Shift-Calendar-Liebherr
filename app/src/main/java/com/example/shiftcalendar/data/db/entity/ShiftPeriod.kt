package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "shift_periods",
    foreignKeys = [
        ForeignKey(
            entity = Crew::class,
            parentColumns = ["id"],
            childColumns = ["crewId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("crewId"), Index(value = ["crewId", "startDate"])]
)
data class ShiftPeriod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val crewId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val roadDaysBefore: Int = 1,
    val roadDaysAfter: Int = 1,
    val isNightShift: Boolean = false,
    val label: String = ""
)
