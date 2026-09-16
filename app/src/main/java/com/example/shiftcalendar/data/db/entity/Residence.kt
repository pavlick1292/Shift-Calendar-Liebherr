package com.example.shiftcalendar.data.db.entity

enum class Residence(val titleRu: String) {
    BUM("БУМ"),
    ZEBRA("Зебра"),
    SHAKHTERSK("Шахтерск"),
    PLOSHCHAD("Площадь");

    companion object {
        fun fromName(name: String): Residence =
            entries.firstOrNull { it.name == name } ?: BUM
    }
}
