package com.example.shiftcalendar.ui.navigation

object Routes {
    const val CALENDAR = "calendar"
    const val CREWS = "crews"
    const val PEOPLE = "people"
    const val HOURS = "hours"
    const val SETTINGS = "settings"
    const val CREW_DETAIL = "crew/{crewId}"
    const val PERSON_DETAIL = "person/{personId}"

    fun crewDetail(id: Long) = "crew/$id"
    fun personDetail(id: Long) = "person/$id"
}
