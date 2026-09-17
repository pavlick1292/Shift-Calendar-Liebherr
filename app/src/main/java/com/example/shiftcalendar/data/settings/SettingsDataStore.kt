package com.example.shiftcalendar.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class SettingsDataStore(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private object AnimKeys {
        val ENABLED     = booleanPreferencesKey("anim_enabled")
        val TRANSITIONS = booleanPreferencesKey("anim_transitions")
        val CARDS       = booleanPreferencesKey("anim_cards")
        val PULSE       = booleanPreferencesKey("anim_pulse")
        val STAGGER     = booleanPreferencesKey("anim_stagger")
        val GLASS       = booleanPreferencesKey("anim_glass")
    }

    private object NotifKeys {
        val ENABLED           = booleanPreferencesKey("notif_enabled")
        val START_DAYS_BEFORE = intPreferencesKey("notif_start_days_before")
        val START_DAY_ENABLED = booleanPreferencesKey("notif_start_day_enabled")
        val END_DAYS_BEFORE   = intPreferencesKey("notif_end_days_before")
        val END_DAY_ENABLED   = booleanPreferencesKey("notif_end_day_enabled")
        val ROAD_DAYS_BEFORE  = intPreferencesKey("notif_road_days_before")
        val ROAD_ENABLED      = booleanPreferencesKey("notif_road_enabled")
        val QUIET_ENABLED     = booleanPreferencesKey("notif_quiet_enabled")
        val QUIET_START       = intPreferencesKey("notif_quiet_start")
        val QUIET_END         = intPreferencesKey("notif_quiet_end")
        val ACTIVE_ONLY       = booleanPreferencesKey("notif_active_only")
    }

    private object HoursKeys {
        val SHIFT_HOURS    = doublePreferencesKey("hours_shift")
        val LUNCH_HOURS    = doublePreferencesKey("hours_lunch")
        val NIGHT_HOURS    = doublePreferencesKey("hours_night")
        val ROAD_HOURS     = doublePreferencesKey("hours_road")
        val YEARLY_NORM    = doublePreferencesKey("hours_yearly_norm")
        val TRACK_OVERTIME = booleanPreferencesKey("hours_track_overtime")
    }

    private object ThemeKeys {
        val MODE          = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("theme_dynamic_color")
    }

    private object AppearanceKeys {
        val CALENDAR_STYLE = stringPreferencesKey("calendar_style")
    }

    private object OnboardingKeys {
        val WELCOME_SHOWN = booleanPreferencesKey("onb_welcome_shown")
        val SHOWN_TABS    = stringSetPreferencesKey("onb_shown_tabs")
    }

    val animationSettings: Flow<AnimationSettings> = context.dataStore.data.map { p ->
        AnimationSettings(
            animationsEnabled = p[AnimKeys.ENABLED] ?: true,
            screenTransitions = p[AnimKeys.TRANSITIONS] ?: true,
            cardAnimations    = p[AnimKeys.CARDS] ?: true,
            pulsingEffects    = p[AnimKeys.PULSE] ?: true,
            staggerLists      = p[AnimKeys.STAGGER] ?: true,
            glassBlur         = p[AnimKeys.GLASS] ?: true
        )
    }

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data.map { p ->
        NotificationSettings(
            enabled              = p[NotifKeys.ENABLED] ?: true,
            shiftStartDaysBefore = p[NotifKeys.START_DAYS_BEFORE] ?: 4,
            shiftStartDayEnabled = p[NotifKeys.START_DAY_ENABLED] ?: true,
            shiftEndDaysBefore   = p[NotifKeys.END_DAYS_BEFORE] ?: 7,
            shiftEndDayEnabled   = p[NotifKeys.END_DAY_ENABLED] ?: true,
            roadDaysBefore       = p[NotifKeys.ROAD_DAYS_BEFORE] ?: 1,
            roadEnabled          = p[NotifKeys.ROAD_ENABLED] ?: true,
            quietHoursEnabled    = p[NotifKeys.QUIET_ENABLED] ?: false,
            quietHoursStartHour  = p[NotifKeys.QUIET_START] ?: 23,
            quietHoursEndHour    = p[NotifKeys.QUIET_END] ?: 7,
            activeCrewsOnly      = p[NotifKeys.ACTIVE_ONLY] ?: true
        )
    }

    val hoursSettings: Flow<HoursSettings> = context.dataStore.data.map { p ->
        HoursSettings(
            shiftDayHours   = p[HoursKeys.SHIFT_HOURS] ?: 12.0,
            lunchHours      = p[HoursKeys.LUNCH_HOURS] ?: 1.0,
            nightShiftHours = p[HoursKeys.NIGHT_HOURS] ?: 12.0,
            roadDayHours    = p[HoursKeys.ROAD_HOURS] ?: 8.0,
            yearlyNorm      = p[HoursKeys.YEARLY_NORM] ?: 1972.0,
            trackOvertime   = p[HoursKeys.TRACK_OVERTIME] ?: true
        )
    }

    val themeSettings: Flow<ThemeSettings> = context.dataStore.data.map { p ->
        ThemeSettings(
            themeMode = p[ThemeKeys.MODE]?.let { ThemeMode.fromName(it) } ?: ThemeMode.SYSTEM,
            dynamicColor = p[ThemeKeys.DYNAMIC_COLOR] ?: false
        )
    }

    val appearanceSettings: Flow<AppearanceSettings> = context.dataStore.data.map { p ->
        AppearanceSettings(
            calendarStyle = p[AppearanceKeys.CALENDAR_STYLE]?.let { CalendarStyle.fromName(it) }
                ?: CalendarStyle.FRAME_BOLD
        )
    }

    val onboardingSettings: Flow<OnboardingSettings> = context.dataStore.data.map { p ->
        OnboardingSettings(
            welcomeShown = p[OnboardingKeys.WELCOME_SHOWN] ?: false,
            shownTabs    = p[OnboardingKeys.SHOWN_TABS] ?: emptySet()
        )
    }

    // Setters — animation
    suspend fun setAnimationsEnabled(value: Boolean) = set(AnimKeys.ENABLED, value)
    suspend fun setScreenTransitions(value: Boolean) = set(AnimKeys.TRANSITIONS, value)
    suspend fun setCardAnimations(value: Boolean) = set(AnimKeys.CARDS, value)
    suspend fun setPulsingEffects(value: Boolean) = set(AnimKeys.PULSE, value)
    suspend fun setStaggerLists(value: Boolean) = set(AnimKeys.STAGGER, value)
    suspend fun setGlassBlur(value: Boolean) = set(AnimKeys.GLASS, value)

    // Setters — notifications
    suspend fun setNotificationsEnabled(value: Boolean) = set(NotifKeys.ENABLED, value)
    suspend fun setShiftStartDaysBefore(value: Int) = set(NotifKeys.START_DAYS_BEFORE, value)
    suspend fun setShiftStartDayEnabled(value: Boolean) = set(NotifKeys.START_DAY_ENABLED, value)
    suspend fun setShiftEndDaysBefore(value: Int) = set(NotifKeys.END_DAYS_BEFORE, value)
    suspend fun setShiftEndDayEnabled(value: Boolean) = set(NotifKeys.END_DAY_ENABLED, value)
    suspend fun setRoadDaysBefore(value: Int) = set(NotifKeys.ROAD_DAYS_BEFORE, value)
    suspend fun setRoadEnabled(value: Boolean) = set(NotifKeys.ROAD_ENABLED, value)
    suspend fun setQuietHoursEnabled(value: Boolean) = set(NotifKeys.QUIET_ENABLED, value)
    suspend fun setQuietHoursStart(value: Int) = set(NotifKeys.QUIET_START, value)
    suspend fun setQuietHoursEnd(value: Int) = set(NotifKeys.QUIET_END, value)
    suspend fun setActiveCrewsOnly(value: Boolean) = set(NotifKeys.ACTIVE_ONLY, value)

    // Setters — hours
    suspend fun setShiftDayHours(value: Double) = set(HoursKeys.SHIFT_HOURS, value)
    suspend fun setLunchHours(value: Double) = set(HoursKeys.LUNCH_HOURS, value)
    suspend fun setNightShiftHours(value: Double) = set(HoursKeys.NIGHT_HOURS, value)
    suspend fun setRoadDayHours(value: Double) = set(HoursKeys.ROAD_HOURS, value)
    suspend fun setYearlyNorm(value: Double) = set(HoursKeys.YEARLY_NORM, value)
    suspend fun setTrackOvertime(value: Boolean) = set(HoursKeys.TRACK_OVERTIME, value)

    // Setters — theme
    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[ThemeKeys.MODE] = mode.name }
    suspend fun setDynamicColor(value: Boolean) = set(ThemeKeys.DYNAMIC_COLOR, value)

    // Setters — appearance
    suspend fun setCalendarStyle(style: CalendarStyle) =
        context.dataStore.edit { it[AppearanceKeys.CALENDAR_STYLE] = style.name }

    // Setters — onboarding
    suspend fun setWelcomeShown() {
        context.dataStore.edit { it[OnboardingKeys.WELCOME_SHOWN] = true }
    }

    suspend fun markTabShown(tab: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[OnboardingKeys.SHOWN_TABS] ?: emptySet()
            prefs[OnboardingKeys.SHOWN_TABS] = current + tab
        }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[OnboardingKeys.WELCOME_SHOWN] = false
            prefs[OnboardingKeys.SHOWN_TABS] = emptySet()
        }
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
