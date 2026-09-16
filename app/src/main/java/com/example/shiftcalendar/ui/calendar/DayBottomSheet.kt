package com.example.shiftcalendar.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.domain.model.CalendarType
import com.example.shiftcalendar.domain.model.DayStatus
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayBottomSheet(
    date: LocalDate,
    status: DayStatus,
    crewNameById: Map<Long, String>,
    memberNamesByCrew: Map<Long, List<String>>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp)) {
            Text(formatDateRu(date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(calendarTypeLabel(status.calendarType),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            if (status.activePeriods.isNotEmpty()) {
                Text("Активные вахты",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                status.activePeriods.forEach { p ->
                    PeriodRow(p, crewNameById[p.crewId] ?: "?",
                        memberNamesByCrew[p.crewId].orEmpty(), night = status.isNight)
                }
            }

            if (status.roadPeriods.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("В дороге",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                status.roadPeriods.forEach { p ->
                    PeriodRow(p, crewNameById[p.crewId] ?: "?",
                        memberNamesByCrew[p.crewId].orEmpty(), night = false)
                }
            }

            if (status.isEmpty) {
                Text("Нет событий",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PeriodRow(
    period: ShiftPeriod,
    crewName: String,
    members: List<String>,
    night: Boolean
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (night) {
                    Icon(Icons.Outlined.Nightlight, null,
                        tint = ShiftColors.NightViolet,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(crewName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            if (members.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(members.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun calendarTypeLabel(t: CalendarType): String = when (t) {
    CalendarType.WORK_DAY -> "Рабочий день по календарю РФ"
    CalendarType.WEEKEND -> "Выходной по календарю РФ"
    CalendarType.HOLIDAY -> "Праздничный день"
}

private fun formatDateRu(d: LocalDate): String {
    val months = listOf("января","февраля","марта","апреля","мая","июня",
        "июля","августа","сентября","октября","ноября","декабря")
    return "${d.dayOfMonth} ${months[d.monthNumber - 1]} ${d.year}"
}
