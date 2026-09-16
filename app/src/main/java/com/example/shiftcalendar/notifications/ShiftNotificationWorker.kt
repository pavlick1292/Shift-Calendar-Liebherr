package com.example.shiftcalendar.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.shiftcalendar.ShiftCalendarApp
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ShiftNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_TYPE) ?: return Result.failure()
        val crewId = inputData.getLong(KEY_CREW_ID, -1L)
        val periodId = inputData.getLong(KEY_PERIOD_ID, -1L)

        val container = (applicationContext as ShiftCalendarApp).container
        val crew = container.crewRepository.getCrew(crewId) ?: return Result.success()
        val periods = container.crewRepository.observePeriods(crewId).first()
        val period = periods.firstOrNull { it.id == periodId } ?: return Result.success()

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        when (type) {
            TYPE_START_SOON -> {
                val days = (period.startDate.toEpochDays() - today.toEpochDays()).toLong()
                Notifications.notifyStartSoon(applicationContext, crew, period, days)
            }
            TYPE_START_TODAY -> Notifications.notifyStartToday(applicationContext, crew, period)
            TYPE_END_SOON -> {
                val days = (period.endDate.toEpochDays() - today.toEpochDays()).toLong()
                Notifications.notifyEndSoon(applicationContext, crew, period, days)
            }
            TYPE_END_TODAY -> Notifications.notifyEndToday(applicationContext, crew, period)
            TYPE_ROAD -> Notifications.notifyRoad(applicationContext, crew, period)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TYPE = "type"
        const val KEY_CREW_ID = "crewId"
        const val KEY_PERIOD_ID = "periodId"
        const val TYPE_START_SOON = "start_soon"
        const val TYPE_START_TODAY = "start_today"
        const val TYPE_END_SOON = "end_soon"
        const val TYPE_END_TODAY = "end_today"
        const val TYPE_ROAD = "road"
    }
}
