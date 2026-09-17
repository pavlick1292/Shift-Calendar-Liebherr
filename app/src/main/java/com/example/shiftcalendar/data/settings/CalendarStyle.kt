package com.example.shiftcalendar.data.settings

enum class CalendarStyle(val titleRu: String) {
    FRAME_BOLD("Жирная рамка"),
    FRAME_DOT("Рамка + точка"),
    GRADIENT("Градиент"),
    UNDERLINE("Подчёркивание");

    companion object {
        fun fromName(name: String): CalendarStyle =
            entries.firstOrNull { it.name == name } ?: FRAME_BOLD
    }
}
