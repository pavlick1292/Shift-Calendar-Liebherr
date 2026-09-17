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
        provideContent { WidgetContent(state) }
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

    private fun findActive(
        crews: List<CrewWithPeriods>,
        today: LocalDate
    ): Pair<Crew, ShiftPeriod>? {
        for (cwp in crews) {
            for (p in cwp.periods) {
                if (today >= p.startDate && today <= p.endDate) {
                    return cwp.crew to p
                }
            }
        }
        return null
    }

    private fun findUpcoming(
        crews: List<CrewWithPeriods>,
        today: LocalDate
    ): Pair<Crew, ShiftPeriod>? {
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

    private fun findLast(
        crews: List<CrewWithPeriods>,
        today: LocalDate
    ): Pair<Crew, ShiftPeriod>? {
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

private val BgTop = Color(0xFF1A1F36)
private val Accent = Color(0xFF8B5CF6)
private val AccentWarm = Color(0xFFFF9F43)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB8BCC8)
private val TextDim = Color(0xFF7A7F94)
private val ProgressBg = Color(0xFF2A3050)

@Composable
private fun WidgetContent(state: WidgetState) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgTop)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            when (state) {
                WidgetState.NoMe -> NoMeContent()
                WidgetState.NoCrew -> NoCrewContent()
                WidgetState.NoData -> NoDataContent()
                is WidgetState.OnShift -> OnShiftContent(state)
                is WidgetState.BeforeShift -> BeforeShiftContent(state)
                is WidgetState.Rest -> RestContent(state)
            }
        }
    }
}

@Composable
private fun OnShiftContent(s: WidgetState.OnShift) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysLeft.toString(),
            style = TextStyle(
                color = ColorProvider(TextPrimary),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysLeft.toLong()),
            style = TextStyle(
                color = ColorProvider(TextSecondary),
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.padding(bottom = 14.dp)
        )
    }
    Text(
        "до конца",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 12.sp
        )
    )
    Spacer(GlanceModifier.height(6.dp))
    ProgressBar(s.progress)
}

@Composable
private fun BeforeShiftContent(s: WidgetState.BeforeShift) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysBefore.toString(),
            style = TextStyle(
                color = ColorProvider(AccentWarm),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysBefore.toLong()),
            style = TextStyle(
                color = ColorProvider(TextSecondary),
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.padding(bottom = 14.dp)
        )
    }
    Text(
        "до вахты",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 12.sp
        )
    )
}

@Composable
private fun RestContent(s: WidgetState.Rest) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            s.daysRest.toString(),
            style = TextStyle(
                color = ColorProvider(TextPrimary),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            pluralDays(s.daysRest.toLong()),
            style = TextStyle(
                color = ColorProvider(TextSecondary),
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.padding(bottom = 14.dp)
        )
    }
    Text(
        "отдыха",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 12.sp
        )
    )
}

@Composable
private fun NoMeContent() {
    Text("👤", style = TextStyle(fontSize = 32.sp))
    Spacer(GlanceModifier.height(6.dp))
    Text(
        "Отметь себя",
        style = TextStyle(
            color = ColorProvider(TextPrimary),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    )
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Люди → «Это я»",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 10.sp
        )
    )
}

@Composable
private fun NoCrewContent() {
    Text("📋", style = TextStyle(fontSize = 32.sp))
    Spacer(GlanceModifier.height(6.dp))
    Text(
        "Нет состава",
        style = TextStyle(
            color = ColorProvider(TextPrimary),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    )
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Добавь себя в состав",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 10.sp
        )
    )
}

@Composable
private fun NoDataContent() {
    Text("📅", style = TextStyle(fontSize = 32.sp))
    Spacer(GlanceModifier.height(6.dp))
    Text(
        "Нет вахт",
        style = TextStyle(
            color = ColorProvider(TextPrimary),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    )
    Spacer(GlanceModifier.height(4.dp))
    Text(
        "Создай вахту",
        style = TextStyle(
            color = ColorProvider(TextSecondary),
            fontSize = 10.sp
        )
    )
}

@Composable
private fun ProgressBar(progress: Float) {
    val filled = (progress * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(6.dp)
            .background(ProgressBg)
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().height(6.dp)) {
            Box(
                modifier = GlanceModifier
                    .width(filled.dp)
                    .height(6.dp)
                    .background(Accent)
            ) {}
            Box(
                modifier = GlanceModifier
                    .width((100 - filled).dp)
                    .height(6.dp)
                    .background(ProgressBg)
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
