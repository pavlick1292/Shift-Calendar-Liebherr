package com.example.shiftcalendar.data.db.entity

enum class Profession(val titleRu: String) {
    SERVICE_ENGINEER("Сервисный инженер"),
    LEAD_SERVICE_ENGINEER("Ведущий сервисный инженер"),
    MECHANIC("Механик"),
    DRIVER_MECHANIC("Механик-водитель"),
    PARMER("Пармист");

    companion object {
        fun fromName(name: String): Profession =
            entries.firstOrNull { it.name == name } ?: SERVICE_ENGINEER
    }
}
