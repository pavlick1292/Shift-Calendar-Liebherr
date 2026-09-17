package com.example.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shiftcalendar.data.db.entity.CrewIcon
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.domain.model.CalendarType
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthGrid(
    year: Int,
    month: Int,
    crewId: Long?,
    vm: CalendarViewModel,
    crews: List<CrewWithPeriods>
) {
    val monthName = java.time.Month.of(month)
        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.uppercase() }

    Column {
        Text("$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth()) {
            listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEachIndexed { i, d ->
                val isWeekendColumn = i >= 5
                Text(d,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = if (isWeekendColumn) ShiftColors.HolidayRed
                            else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        val days = buildMonthDays(year, month)
        val weeks = days.chunked(7)

        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f)) {
                        if (date == null) {
                            Box(Modifier.aspectRatio(1f))
                        } else {
                            val status = remember(date, crewId, crews) {
                                if (crewId == null) vm.dayStatusSummary(date)
                                else vm.dayStatusForCrew(date, crewId)
                            }
                            DayCell(date = date, status = status, vm = vm)
                        }
                    }
                }
                repeat(7 - week.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, status: DayStatus, vm: CalendarViewModel) {
    val isHoliday = status.calendarType == CalendarType.HOLIDAY
    val isWeekend = status.calendarType == CalendarType.WEEKEND
    val isRed = isHoliday || isWeekend
    val hasCrews = status.totalActiveCount > 0

    var showHolidaySheet by remember { mutableStateOf(false) }
    val holidayName = remember(date) {
        if (isHoliday) vm.getHolidayName(date) else null
    }

    val bg = if (isRed) ShiftColors.HolidayRed.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bg)
            .clickable(
                enabled = isHoliday,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showHolidaySheet = true },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (hasCrews) FontWeight.Bold else FontWeight.Normal,
                color = if (isRed) ShiftColors.HolidayRed
                        else MaterialTheme.colorScheme.onSurface
            )

            if (hasCrews) {
                Spacer(Modifier.height(2.dp))
                CrewStripes(status.activeCrews, status.totalActiveCount)
            }
        }
    }

    if (showHolidaySheet) {
        AlertDialog(
            onDismissRequest = { showHolidaySheet = false },
            title = { Text(date.formatRu()) },
            text = { Text(holidayName ?: "Праздничный день") },
            confirmButton = {
                TextButton(onClick = { showHolidaySheet = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun CrewStripes(crews: List<ActiveCrew>, total: Int) {
    val maxVisible = 2
    val visible = crews.take(maxVisible)
    val extra = total - visible.size

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEach { crew ->
            val color = parseCrewColor(crew.colorHex)
            val icon = CrewIcon.fromName(crew.iconType)
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon.symbol,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (extra > 0) {
            Text(
                "+$extra",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

internal fun parseCrewColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    ShiftColors.WorkBlue
}

private fun LocalDate.formatRu(): String {
    val months = listOf(
        "января","февраля","марта","апреля","мая","июня",
        "июля","августа","сентября","октября","ноября","декабря"
    )
    return "$dayOfMonth ${months[monthNumber - 1]} $year"
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
