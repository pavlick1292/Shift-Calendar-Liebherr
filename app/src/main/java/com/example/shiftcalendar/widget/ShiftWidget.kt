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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.shiftcalendar.ShiftCalendarApp
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ShiftWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = computeState(context)
        val bg = WidgetSettings.getBackground(context)
        provideContent { WidgetContent(state, bg) }
    }

    private suspend fun computeState(context: Context): WidgetState {
        val container = (context.applicationContext as ShiftCalendarApp).container

        val people = container.personRepository.observePeople().first()
        val me = people.firstOrNull { it.isMe } ?: return WidgetState.NoMe

        val memberships = container.personRepository.getMembershipsOfPerson(me.id)
        val myCrewIds = memberships.map { it.crewId }.toSet()
        if (myCrewIds.isEmpty()) return WidgetState.NoCrew

        val allCrews = container.crewRepository.observeCrewsWithPeriods().first()
        val myCrews = allCrews.filter { it.crew.id in myCrewIds }

        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        val active = findActive(myCrews, today)
        if (active != null) {
            val (crew, period) = active
            val totalDays = (period.endDate.toEpochDays() - period.startDate.toEpochDays() + 1).toInt()
            val daysLeft = (period.endDate.toEpochDays() - today.toEpochDays()).toInt()
            val daysPassed = totalDays - daysLeft - 1
            val progress = (daysPassed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
            return WidgetState.OnShift(crew.name, daysLeft, period, progress)
        }

        val upcoming = findUpcoming(myCrews, today)
        if (upcoming != null) {
            val (crew, period) = upcoming
            val daysBefore = (period.startDate.toEpochDays() - today.toEpochDays()).toInt()
            return WidgetState.BeforeShift(crew.name, daysBefore, period)
        }

        val rest = findLast(myCrews, today)
        if (rest != null) {
            val (crew, period) = rest
            val daysRest = (today.toEpochDays() - period.endDate.toEpochDays()).toInt()
            return WidgetState.Rest(crew.name, daysRest, period)
        }

        return WidgetState.NoData
    }

    private fun findActive(crews: List<CrewWithPeriods>, today: LocalDate): Pair<Crew, ShiftPeriod>? {
        for (cwp in crews) {
            for (p in cwp.periods) {
                if (today >= p.startDate && today <= p.endDate) return cwp.crew to p
            }
        }
        return null
    }

    private fun findUpcoming(crews: List<CrewWithPeriods>, today: LocalDate): Pair<Crew, ShiftPeriod>? {
        var best: Pair<Crew, ShiftPeriod>? = null
        for (cwp in crews) {
            for (p in cwp.periods) {
                if (p.startDate > today) {
                    if (best == null || p.startDate < best.second.startDate) {
                        best = cwp.crew to p
                    }
                }
            }
        }
        return best
    }

    private fun findLast(crews: List<CrewWithPeriods>, today: LocalDate): Pair<Crew, ShiftPeriod>? {
        var best: Pair<Crew, ShiftPeriod>? = null
        for (cwp in crews) {
            for (p in cwp.periods) {
                if (p.endDate < today) {
                    if (best == null || p.endDate > best.second.endDate) {
                        best = cwp.crew to p
                    }
                }
            }
        }
        return best
    }
}

sealed interface WidgetState {
    data object NoMe : WidgetState
    data object NoCrew : WidgetState
    data object NoData : WidgetState
    data class OnShift(
        val crewName: String,
        val daysLeft: Int,
        val period: ShiftPeriod,
        val progress: Float
    ) : WidgetState
    data class BeforeShift(
        val crewName: String,
        val daysBefore: Int,
        val period: ShiftPeriod
    ) : WidgetState
    data class Rest(
        val crewName: String,
        val daysRest: Int,
        val period: ShiftPeriod
    ) : WidgetState
}

@Composable
private fun WidgetContent(state: WidgetState, bg: WidgetBg) {
    val textMain = Color(bg.textColor)
    val textDim = Color(bg.textColor).copy(alpha = 0.7f)
    val accent = Color(bg.accentColor)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(bg.topColor))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            when (state) {
                WidgetState.NoMe -> NoMeContent(textMain, textDim)
                WidgetState.NoCrew -> NoCrewContent(textMain, textDim)
                WidgetState.NoData -> NoDataContent(textMain, textDim)
                is WidgetState.OnShift -> OnShiftContent(state, textMain, textDim, accent)
                is WidgetState.BeforeShift -> BeforeShiftContent(state, textMain, textDim, accent)
                is WidgetState.Rest -> RestContent(state, textMain, textDim)
            }
        }
    }
}

@Composable
private fun OnShiftContent(
    s: WidgetState.OnShift,
    textMain: Color,
    textDim: Color,
    accent: Color
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysLeft.toString(),
            style = TextStyle(
                color = ColorProvider(textMain),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysLeft.toLong()),
            style = TextStyle(
                color = ColorProvider(textDim),
                fontSize = 13.sp
            ),
            modifier = GlanceModifier.padding(bottom = 10.dp)
        )
    }
    Text(
        "до конца",
        style = TextStyle(
            color = ColorProvider(textDim),
            fontSize = 11.sp
        )
    )
    Spacer(GlanceModifier.height(4.dp))
    ProgressBar(s.progress, accent)
}

@Composable
private fun BeforeShiftContent(
    s: WidgetState.BeforeShift,
    textMain: Color,
    textDim: Color,
    accent: Color
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysBefore.toString(),
            style = TextStyle(
                color = ColorProvider(accent),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysBefore.toLong()),
            style = TextStyle(
                color = ColorProvider(textDim),
                fontSize = 13.sp
            ),
            modifier = GlanceModifier.padding(bottom = 10.dp)
        )
    }
    Text(
        "до вахты",
        style = TextStyle(
            color = ColorProvider(textDim),
            fontSize = 11.sp
        )
    )
}

@Composable
private fun RestContent(
    s: WidgetState.Rest,
    textMain: Color,
    textDim: Color
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysRest.toString(),
            style = TextStyle(
                color = ColorProvider(textMain),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysRest.toLong()),
            style = TextStyle(
                color = ColorProvider(textDim),
                fontSize = 13.sp
            ),
            modifier = GlanceModifier.padding(bottom = 10.dp)
        )
    }
    Text(
        "отдыха",
        style = TextStyle(
            color = ColorProvider(textDim),
            fontSize = 11.sp
        )
    )
}

@Composable
private fun NoMeContent(textMain: Color, textDim: Color) {
    Text("👤", style = TextStyle(fontSize = 24.sp))
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Отметь себя",
        style = TextStyle(
            color = ColorProvider(textMain),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun NoCrewContent(textMain: Color, textDim: Color) {
    Text("📋", style = TextStyle(fontSize = 24.sp))
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Нет состава",
        style = TextStyle(
            color = ColorProvider(textMain),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun NoDataContent(textMain: Color, textDim: Color) {
    Text("📅", style = TextStyle(fontSize = 24.sp))
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Нет вахт",
        style = TextStyle(
            color = ColorProvider(textMain),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun ProgressBar(progress: Float, accent: Color) {
    val filled = (progress * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(5.dp)
            .background(accent.copy(alpha = 0.2f))
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().height(5.dp)) {
            Box(
                modifier = GlanceModifier
                    .width(filled.dp)
                    .height(5.dp)
                    .background(accent)
            ) {}
            Box(
                modifier = GlanceModifier
                    .width((100 - filled).dp)
                    .height(5.dp)
                    .background(accent.copy(alpha = 0.2f))
            ) {}
        }
    }
}

private fun pluralDays(n: Long): String {
    val m10 = n % 10
    val m100 = n % 100
    return when {
        m10 == 1L && m100 != 11L -> "день"
        m10 in 2..4 && m100 !in 12..14 -> "дня"
        else -> "дней"
    }
}
