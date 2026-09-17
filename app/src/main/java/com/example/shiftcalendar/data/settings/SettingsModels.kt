package com.example.shiftcalendar.data.settings

data class AnimationSettings(
    val animationsEnabled: Boolean = true,
    val screenTransitions: Boolean = true,
    val cardAnimations: Boolean = true,
    val pulsingEffects: Boolean = true,
    val staggerLists: Boolean = true,
    val glassBlur: Boolean = true
)

data class NotificationSettings(
    val enabled: Boolean = true,
    val shiftStartDaysBefore: Int = 4,
    val shiftStartDayEnabled: Boolean = true,
    val shiftEndDaysBefore: Int = 7,
    val shiftEndDayEnabled: Boolean = true,
    val roadDaysBefore: Int = 1,
    val roadEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 23,
    val quietHoursEndHour: Int = 7,
    val activeCrewsOnly: Boolean = true
)

data class HoursSettings(
    val shiftDayHours: Double = 12.0,
    val lunchHours: Double = 1.0,
    val nightShiftHours: Double = 12.0,
    val roadDayHours: Double = 8.0,
    val yearlyNorm: Double = 1972.0,
    val trackOvertime: Boolean = true
) {
    val effectiveShiftHours: Double get() = shiftDayHours - lunchHours
    val effectiveNightHours: Double get() = nightShiftHours - lunchHours
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false
)

data class OnboardingSettings(
    val welcomeShown: Boolean = false,
    val shownTabs: Set<String> = emptySet()
)

enum class ThemeMode(val titleRu: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    HIGH_CONTRAST("Высокий контраст"),
    AURORA("Северное сияние"),
    INDUSTRIAL("Индустриальная");

    companion object {
        fun fromName(name: String): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
