package com.example.shiftcalendar.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.shiftcalendar.ShiftCalendarApp
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val context: Context,
    private val workManager: WorkManager,
    private val settings: SettingsDataStore
) {
    suspend fun rescheduleAll() {
        workManager.cancelAllWorkByTag(TAG_SHIFT_NOTIF)

        val s = settings.notificationSettings.first()
        if (!s.enabled) return

        val container = (context.applicationContext as ShiftCalendarApp).container
        val crews = container.crewRepository.observeCrewsWithPeriods().first()

        for (cwp in crews) {
            for (period in cwp.periods) {
                enqueueStart(cwp.crew.id, period, s.shiftStartDaysBefore, s.shiftStartDayEnabled)
                enqueueEnd(cwp.crew.id, period, s.shiftEndDaysBefore, s.shiftEndDayEnabled)
                if (s.roadEnabled) enqueueRoad(cwp.crew.id, period, s.roadDaysBefore)
            }
        }
    }

    private fun enqueueStart(crewId: Long, period: ShiftPeriod, daysBefore: Int, dayEnabled: Boolean) {
        scheduleAt(period.startDate.minus(DatePeriod(days = daysBefore)), 9,
            ShiftNotificationWorker.TYPE_START_SOON, crewId, period.id)
        if (dayEnabled) {
            scheduleAt(period.startDate, 8,
                ShiftNotificationWorker.TYPE_START_TODAY, crewId, period.id)
        }
    }

    private fun enqueueEnd(crewId: Long, period: ShiftPeriod, daysBefore: Int, dayEnabled: Boolean) {
        scheduleAt(period.endDate.minus(DatePeriod(days = daysBefore)), 9,
            ShiftNotificationWorker.TYPE_END_SOON, crewId, period.id)
        if (dayEnabled) {
            scheduleAt(period.endDate, 8,
                ShiftNotificationWorker.TYPE_END_TODAY, crewId, period.id)
        }
    }

    private fun enqueueRoad(crewId: Long, period: ShiftPeriod, daysBefore: Int) {
        val roadStart = period.startDate.minus(DatePeriod(days = daysBefore))
        scheduleAt(roadStart, 20, ShiftNotificationWorker.TYPE_ROAD, crewId, period.id)
    }

    private fun scheduleAt(at: LocalDate, hour: Int, type: String, crewId: Long, periodId: Long) {
        val targetDate = java.time.LocalDate.of(at.year, at.monthNumber, at.dayOfMonth)
        val targetDateTime = targetDate.atTime(hour, 0)
        val now = LocalDateTime.now()

        if (targetDateTime.isBefore(now)) return

        val delayMillis = Duration.between(now, targetDateTime).toMillis()

        val request = OneTimeWorkRequestBuilder<ShiftNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                ShiftNotificationWorker.KEY_TYPE to type,
                ShiftNotificationWorker.KEY_CREW_ID to crewId,
                ShiftNotificationWorker.KEY_PERIOD_ID to periodId
            ))
            .addTag(TAG_SHIFT_NOTIF)
            .build()

        workManager.enqueueUniqueWork("shift_${type}_${periodId}", ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        const val TAG_SHIFT_NOTIF = "shift_notif"
    }
}

