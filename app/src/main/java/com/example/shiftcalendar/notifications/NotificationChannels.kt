package com.example.shiftcalendar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val SHIFT_START = "shift_start"
    const val SHIFT_END   = "shift_end"
    const val ROAD        = "road"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java)

        mgr.createNotificationChannel(
            NotificationChannel(SHIFT_START, "Начало вахты", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Напоминания о приближении и начале вахты"
                enableVibration(true)
            }
        )
        mgr.createNotificationChannel(
            NotificationChannel(SHIFT_END, "Конец вахты", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Напоминания о приближении и окончании вахты"
                enableVibration(true)
            }
        )
        mgr.createNotificationChannel(
            NotificationChannel(ROAD, "Дорога", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Напоминания о днях в дороге"
            }
        )
    }
}
