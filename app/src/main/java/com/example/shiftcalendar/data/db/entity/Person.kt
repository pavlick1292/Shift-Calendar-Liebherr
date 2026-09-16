package com.example.shiftcalendar.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val profession: Profession,
    val residence: Residence,
    val phone: String = "",
    val note: String = ""
)
