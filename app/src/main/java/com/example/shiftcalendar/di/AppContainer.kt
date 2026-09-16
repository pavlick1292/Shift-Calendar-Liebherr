package com.example.shiftcalendar.di

import android.content.Context
import androidx.work.WorkManager
import com.example.shiftcalendar.data.calendar.CalendarRepository
import com.example.shiftcalendar.data.db.AppDatabase
import com.example.shiftcalendar.data.repository.CrewRepository
import com.example.shiftcalendar.data.repository.OverrideRepository
import com.example.shiftcalendar.data.repository.PersonRepository
import com.example.shiftcalendar.data.settings.SettingsDataStore
import com.example.shiftcalendar.notifications.NotificationScheduler

class AppContainer(
    val db: AppDatabase,
    val settings: SettingsDataStore,
    val workManager: WorkManager,
    val appContext: Context
) {
    val crewRepository: CrewRepository by lazy { CrewRepository(db.crewDao(), db.shiftPeriodDao()) }
    val personRepository: PersonRepository by lazy { PersonRepository(db.personDao(), db.crewMemberDao()) }
    val overrideRepository: OverrideRepository by lazy {
        OverrideRepository(db.dayOverrideDao(), db.hoursOverrideDao())
    }
    val calendarRepository: CalendarRepository by lazy { CalendarRepository(appContext) }
    val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(appContext, workManager, settings)
    }
}

