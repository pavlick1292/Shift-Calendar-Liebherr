package com.example.shiftcalendar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shiftcalendar.data.db.dao.CrewDao
import com.example.shiftcalendar.data.db.dao.CrewMemberDao
import com.example.shiftcalendar.data.db.dao.DayOverrideDao
import com.example.shiftcalendar.data.db.dao.HoursOverrideDao
import com.example.shiftcalendar.data.db.dao.PersonDao
import com.example.shiftcalendar.data.db.dao.ShiftPeriodDao
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.DayOverride
import com.example.shiftcalendar.data.db.entity.HoursOverride
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.ShiftPeriod

@Database(
    entities = [
        Crew::class,
        ShiftPeriod::class,
        Person::class,
        CrewMember::class,
        DayOverride::class,
        HoursOverride::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crewDao(): CrewDao
    abstract fun shiftPeriodDao(): ShiftPeriodDao
    abstract fun personDao(): PersonDao
    abstract fun crewMemberDao(): CrewMemberDao
    abstract fun dayOverrideDao(): DayOverrideDao
    abstract fun hoursOverrideDao(): HoursOverrideDao

    companion object {
        const val NAME = "shift_calendar.db"
    }
}
