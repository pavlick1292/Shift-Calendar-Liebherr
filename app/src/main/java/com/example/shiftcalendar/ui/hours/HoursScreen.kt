package com.example.shiftcalendar.ui.hours

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.export.CsvExporter
import com.example.shiftcalendar.export.PdfExporter
import com.example.shiftcalendar.ui.crews.vmFactory
import com.example.shiftcalendar.ui.theme.ShiftColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursScreen(container: AppContainer, navController: NavController) {
    val vm: HoursViewModel = viewModel(factory = vmFactory { HoursViewModel(container) })
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Мои часы", fontWeight = FontWeight.SemiBold)
                        if (state.hasMe) {
                            Text(state.meName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.setYear(state.year - 1) }) {
                        Icon(Icons.Outlined.ChevronLeft, "Назад")
                    }
                    Text(state.year.toString(),
                        style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { vm.setYear(state.year + 1) }) {
                        Icon(Icons.Outlined.ChevronRight, "Вперёд")
                    }
                }
            )
        }
    ) { padding ->
        if (!state.hasMe) {
            EmptyState(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SummaryCard(state) }
                item {
                    Text("По месяцам",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp))
                }
                items(state.monthly, key = { it.month }) { m ->
                    MonthRow(m)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Не отмечен \"Это я\"",
            style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Открой вкладку «Люди» → отметь себя галочкой «Это я» → тогда здесь появятся твои часы.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryCard(state: HoursUiState) {
    val progress = (state.totalAll / state.norm).toFloat().coerceIn(0f, 1.2f)
    val totalText = String.format(Locale.US, "%.0f", state.totalAll)
    val normText = String.format(Locale.US, "%.0f", state.norm)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(totalText,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text("ч", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.weight(1f))
                Text("Норма: $normText ч",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = ShiftColors.WorkBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCell("Обычные", state.totalRegular, ShiftColors.WorkBlue, Modifier.weight(1f))
                StatCell("Ночные", state.totalNight, ShiftColors.NightViolet, Modifier.weight(1f))
                StatCell("Дорога", state.totalRoad, ShiftColors.RoadAmber, Modifier.weight(1f))
            }
            if (state.totalAll > state.norm) {
                val overText = String.format(Locale.US, "%.0f", state.totalAll - state.norm)
                Spacer(Modifier.height(8.dp))
                Text("Переработка: +$overText ч",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShiftColors.HolidayRed)
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    val valueText = String.format(Locale.US, "%.0f", value)
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(MaterialTheme.shapes.extraSmall).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(2.dp))
        Text("$valueText ч",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MonthRow(m: MonthHours) {
    val monthName = listOf(
        "Январь","Февраль","Март","Апрель","Май","Июнь",
        "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
    )[m.month - 1]

    val totalText = String.format(Locale.US, "%.0f", m.total)
    val regularText = String.format(Locale.US, "%.0f", m.regularHours)
    val nightText = String.format(Locale.US, "%.0f", m.nightHours)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                Text("$totalText ч",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (m.total > 0) ShiftColors.WorkBlue
                            else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (m.total > 0) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallStat("Обычные", regularText, ShiftColors.WorkBlue)
                    if (m.nightHours > 0) {
                        SmallStat("Ночные", nightText, ShiftColors.NightViolet)
                    }
                    if (m.roadHours > 0) {
                        val roadText = String.format(Locale.US, "%.0f", m.roadHours)
                        SmallStat("Дорога", roadText, ShiftColors.RoadAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(MaterialTheme.shapes.extraSmall).background(color))
        Spacer(Modifier.width(4.dp))
        Text("$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
