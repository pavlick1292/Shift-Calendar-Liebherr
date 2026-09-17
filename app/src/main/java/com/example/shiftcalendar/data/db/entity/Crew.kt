package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crews")
data class Crew(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#3B82F6",
    val iconType: String = "CIRCLE",   // CIRCLE / SQUARE / STAR / TRIANGLE / HEART / DIAMOND
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

enum class CrewIcon(val symbol: String) {
    CIRCLE("●"),
    SQUARE("■"),
    STAR("★"),
    TRIANGLE("▲"),
    HEART("♥"),
    DIAMOND("◆");

    companion object {
        fun fromName(name: String): CrewIcon =
            entries.firstOrNull { it.name == name } ?: CIRCLE
    }
}
