package com.example.shiftcalendar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.shiftcalendar.ShiftCalendarApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as ShiftCalendarApp
        CoroutineScope(Dispatchers.IO).launch {
            app.container.notificationScheduler.rescheduleAll()
        }
    }
}
