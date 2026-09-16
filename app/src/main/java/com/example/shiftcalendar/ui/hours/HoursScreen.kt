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
                title = { Text("Часы за год", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { vm.setYear(state.year - 1) }) {
                        Icon(Icons.Outlined.ChevronLeft, "Назад")
                    }
                    Text(state.year.toString(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { vm.setYear(state.year + 1) }) {
                        Icon(Icons.Outlined.ChevronRight, "Вперёд")
                    }
                    var menuOpen by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.IosShare, "Экспорт")
                    }
                    DropdownMenu(menuOpen, { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Экспорт в CSV") },
                            onClick = {
                                menuOpen = false
                                val file = CsvExporter.export(
                                    context = container.appContext,
                                    year = state.year,
                                    rows = state.rows.map { it.person to it.hours }
                                )
                                CsvExporter.share(container.appContext, file)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Экспорт в PDF") },
                            onClick = {
                                menuOpen = false
                                val file = PdfExporter.export(
                                    context = container.appContext,
                                    year = state.year,
                                    rows = state.rows.map { it.person to it.hours },
                                    norm = state.norm
                                )
                                PdfExporter.share(container.appContext, file)
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SummaryCard(state) }
            item {
                Text("По сотрудникам",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            items(state.rows, key = { it.person.id }) { row -> HoursRowCard(row) }
        }
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
private fun HoursRowCard(row: HoursRow) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(row.person.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(row.person.profession.titleRu,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row {
                MiniStat("Обычные", row.hours.regularHours, Modifier.weight(1f))
                MiniStat("Ночные", row.hours.nightHours, Modifier.weight(1f))
                MiniStat("Дорога", row.hours.roadHours, Modifier.weight(1f))
                MiniStat("Итого", row.hours.total, Modifier.weight(1f), bold = true)
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    val valueText = String.format(Locale.US, "%.0f", value)
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valueText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

