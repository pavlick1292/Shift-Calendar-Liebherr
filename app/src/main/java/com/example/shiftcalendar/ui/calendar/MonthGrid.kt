package com.example.shiftcalendar.ui.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shiftcalendar.domain.model.CalendarType
import com.example.shiftcalendar.domain.model.DayStatus
import com.example.shiftcalendar.ui.animation.animSpec
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthGrid(year: Int, month: Int, crewId: Long?, vm: CalendarViewModel) {
    val monthName = java.time.Month.of(month)
        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.uppercase() }

    Column {
        Text("$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))

        // Дни недели
        Row(Modifier.fillMaxWidth()) {
            listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEach { d ->
                Text(d,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        // Разбиваем дни на недели (по 7 ячеек)
        val days = buildMonthDays(year, month)
        val weeks = days.chunked(7)

        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f)) {
                        if (date == null) {
                            Box(Modifier.aspectRatio(1f))
                        } else {
                            val status = remember(date, crewId) { vm.dayStatus(date, crewId) }
                            DayCell(date = date, status = status)
                        }
                    }
                }
                // Дополняем до 7 ячеек, если неделя неполная
                repeat(7 - week.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, status: DayStatus) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = animSpec(150),
        label = "dayScale"
    )

    val bg = when {
        status.isWorking && status.isNight -> ShiftColors.NightViolet.copy(alpha = 0.2f)
        status.isWorking -> ShiftColors.WorkBlue.copy(alpha = 0.2f)
        status.isOnRoad -> ShiftColors.RoadAmber.copy(alpha = 0.2f)
        status.calendarType == CalendarType.HOLIDAY -> ShiftColors.HolidayRed.copy(alpha = 0.15f)
        status.calendarType == CalendarType.WEEKEND -> ShiftColors.WeekendGray.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { pressed = true; pressed = false },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (status.isWorking) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    status.calendarType == CalendarType.HOLIDAY -> ShiftColors.HolidayRed
                    status.calendarType == CalendarType.WEEKEND -> ShiftColors.WeekendGray
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (status.isWorking && status.isNight) {
                Icon(Icons.Outlined.Nightlight, "Ночная",
                    tint = ShiftColors.NightViolet,
                    modifier = Modifier.size(12.dp))
            } else if (status.isOnRoad) {
                Icon(Icons.Outlined.DirectionsCar, "Дорога",
                    tint = ShiftColors.RoadAmber,
                    modifier = Modifier.size(12.dp))
            }
        }
    }
}

private fun buildMonthDays(year: Int, month: Int): List<LocalDate?> {
    val first = java.time.LocalDate.of(year, month, 1)
    val daysInMonth = first.lengthOfMonth()
    val firstDayOfWeek = first.dayOfWeek.value
    val leading = firstDayOfWeek - 1
    val list = mutableListOf<LocalDate?>()
    repeat(leading) { list += null }
    for (d in 1..daysInMonth) list += LocalDate(year, month, d)
    while (list.size % 7 != 0) list += null
    return list
}
