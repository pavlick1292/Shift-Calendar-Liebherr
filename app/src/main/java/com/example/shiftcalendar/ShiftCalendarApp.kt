package com.example.shiftcalendar

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.example.shiftcalendar.data.db.AppDatabase
import com.example.shiftcalendar.data.settings.SettingsDataStore
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShiftCalendarApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        NotificationChannels.createAll(this)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.NAME
        ).fallbackToDestructiveMigration().build()

        val settings = SettingsDataStore(applicationContext, appScope)

        container = AppContainer(
            db = db,
            settings = settings,
            workManager = WorkManager.getInstance(applicationContext),
            appContext = applicationContext
        )

        appScope.launch {
            container.notificationScheduler.rescheduleAll()
        }
    }
}

