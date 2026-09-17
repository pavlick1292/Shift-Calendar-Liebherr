package com.example.shiftcalendar.data.db.entity

enum class Profession(val titleRu: String) {
    // Отсортировано по алфавиту (русский)
    LEAD_SERVICE_ENGINEER("Ведущий сервисный инженер"),
    DRIVER_MECHANIC("Механик-водитель"),
    MECHANIC("Механик"),
    PARMER("Пармист"),
    PROJECT_MANAGER("Руководитель проекта"),
    WELDER("Сварщик"),
    SERVICE_ENGINEER("Сервисный инженер");

    companion object {
        fun fromName(name: String): Profession =
            entries.firstOrNull { it.name == name } ?: SERVICE_ENGINEER

        fun sortedByTitle(): List<Profession> =
            entries.sortedBy { it.titleRu.lowercase() }
    }
}
