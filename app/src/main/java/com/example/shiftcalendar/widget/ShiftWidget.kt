package com.example.shiftcalendar.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.shiftcalendar.ShiftCalendarApp
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ShiftWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = computeState(context)
        provideContent { WidgetContent(state) }
    }

    private suspend fun computeState(context: Context): WidgetState {
        val container = (context.applicationContext as ShiftCalendarApp).container
        val crews = container.crewRepository.observeCrewsWithPeriods().first()
        if (crews.isEmpty()) return WidgetState.Empty

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        for (cwp in crews) {
            for (p in cwp.periods) {
                if (today >= p.startDate && today <= p.endDate) {
                    val daysLeft = (p.endDate.toEpochDays() - today.toEpochDays()).toLong()
                    return WidgetState.OnShift(cwp.crew.name, daysLeft, "${p.startDate} – ${p.endDate}")
                }
            }
        }

        val upcoming = crews
            .flatMap { cwp -> cwp.periods.map { cwp.crew to it } }
            .filter { (_, p) -> p.startDate > today }
            .minByOrNull { (_, p) -> p.startDate }

        return if (upcoming != null) {
            val (crew, p) = upcoming
            val daysBefore = (p.startDate.toEpochDays() - today.toEpochDays()).toLong()
            WidgetState.BeforeShift(crew.name, daysBefore, "${p.startDate} – ${p.endDate}")
        } else WidgetState.Empty
    }
}

sealed interface WidgetState {
    data object Empty : WidgetState
    data class OnShift(val crewName: String, val days: Long, val range: String) : WidgetState
    data class BeforeShift(val crewName: String, val days: Long, val range: String) : WidgetState
}

@Composable
private fun WidgetContent(state: WidgetState) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(Color(0xFF1A1F36)).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                WidgetState.Empty -> Text(
                    text = "Нет активных вахт",
                    style = TextStyle(color = ColorProvider(Color(0xFFE6E8F0)), fontSize = 13.sp)
                )
                is WidgetState.OnShift -> {
                    Text("🌇 ${state.crewName}",
                        style = TextStyle(color = ColorProvider(Color(0xFFF59E0B)), fontSize = 12.sp, fontWeight = FontWeight.Medium))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(state.days.toString(),
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 36.sp, fontWeight = FontWeight.Bold))
                    Text(pluralDays(state.days) + " до конца",
                        style = TextStyle(color = ColorProvider(Color(0xFFE6E8F0)), fontSize = 11.sp))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(state.range,
                        style = TextStyle(color = ColorProvider(Color(0xFF9CA3AF)), fontSize = 10.sp))
                }
                is WidgetState.BeforeShift -> {
                    Text("🌅 ${state.crewName}",
                        style = TextStyle(color = ColorProvider(Color(0xFF6C5CE7)), fontSize = 12.sp, fontWeight = FontWeight.Medium))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(state.days.toString(),
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 36.sp, fontWeight = FontWeight.Bold))
                    Text(pluralDays(state.days) + " до вахты",
                        style = TextStyle(color = ColorProvider(Color(0xFFE6E8F0)), fontSize = 11.sp))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(state.range,
                        style = TextStyle(color = ColorProvider(Color(0xFF9CA3AF)), fontSize = 10.sp))
                }
            }
        }
    }
}

private fun pluralDays(n: Long): String {
    val m10 = n % 10; val m100 = n % 100
    return when {
        m10 == 1L && m100 != 11L -> "день"
        m10 in 2..4 && m100 !in 12..14 -> "дня"
        else -> "дней"
    }
}
