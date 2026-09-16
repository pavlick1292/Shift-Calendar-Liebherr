package com.example.shiftcalendar.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.shiftcalendar.MainActivity
import com.example.shiftcalendar.R
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.ShiftPeriod

object Notifications {

    fun notifyStartSoon(context: Context, crew: Crew, period: ShiftPeriod, daysLeft: Long) {
        val text = when (daysLeft) {
            0L -> "Вахта начинается сегодня"
            1L -> "Вахта начнётся завтра"
            else -> "До начала вахты $daysLeft ${pluralDays(daysLeft)}"
        }
        show(context, NotificationChannels.SHIFT_START, notifId("start_soon", period.id),
            "🌅 ${crew.name}", "$text · ${period.startDate} – ${period.endDate}", crew.id)
    }

    fun notifyStartToday(context: Context, crew: Crew, period: ShiftPeriod) {
        show(context, NotificationChannels.SHIFT_START, notifId("start_today", period.id),
            "🌅 ${crew.name}", "Вахта начинается сегодня · до ${period.endDate}", crew.id)
    }

    fun notifyEndSoon(context: Context, crew: Crew, period: ShiftPeriod, daysLeft: Long) {
        val text = when (daysLeft) {
            0L -> "Вахта заканчивается сегодня"
            1L -> "Вахта закончится завтра"
            else -> "До конца вахты $daysLeft ${pluralDays(daysLeft)}"
        }
        show(context, NotificationChannels.SHIFT_END, notifId("end_soon", period.id),
            "🌇 ${crew.name}", "$text · ${period.endDate}", crew.id)
    }

    fun notifyEndToday(context: Context, crew: Crew, period: ShiftPeriod) {
        show(context, NotificationChannels.SHIFT_END, notifId("end_today", period.id),
            "🌇 ${crew.name}", "Вахта заканчивается сегодня", crew.id)
    }

    fun notifyRoad(context: Context, crew: Crew, period: ShiftPeriod) {
        show(context, NotificationChannels.ROAD, notifId("road", period.id),
            "🚗 ${crew.name}", "Завтра в дорогу · выезд на вахту ${period.startDate}", crew.id)
    }

    private fun show(context: Context, channel: String, id: Int, title: String, text: String, crewId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = Uri.parse("shiftcalendar://crew/$crewId")
        }
        val pending = PendingIntent.getActivity(context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) { }
    }

    private fun notifId(prefix: String, periodId: Long): Int =
        (prefix.hashCode() * 31 + periodId.toInt()) and 0x7FFFFFFF

    private fun pluralDays(n: Long): String {
        val m10 = n % 10; val m100 = n % 100
        return when {
            m10 == 1L && m100 != 11L -> "день"
            m10 in 2..4 && m100 !in 12..14 -> "дня"
            else -> "дней"
        }
    }
}
