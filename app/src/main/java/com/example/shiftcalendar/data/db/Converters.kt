package com.example.shiftcalendar.data.db

import androidx.room.TypeConverter
import com.example.shiftcalendar.data.db.entity.DayType
import com.example.shiftcalendar.data.db.entity.HoursCategory
import com.example.shiftcalendar.data.db.entity.Profession
import com.example.shiftcalendar.data.db.entity.Residence
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromProfession(p: Profession): String = p.name

    @TypeConverter
    fun toProfession(s: String): Profession = Profession.fromName(s)

    @TypeConverter
    fun fromResidence(r: Residence): String = r.name

    @TypeConverter
    fun toResidence(s: String): Residence = Residence.fromName(s)

    @TypeConverter
    fun fromDayType(t: DayType): String = t.name

    @TypeConverter
    fun toDayType(s: String): DayType = DayType.valueOf(s)

    @TypeConverter
    fun fromHoursCategory(c: HoursCategory): String = c.name

    @TypeConverter
    fun toHoursCategory(s: String): HoursCategory = HoursCategory.valueOf(s)
}
