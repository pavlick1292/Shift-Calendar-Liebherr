package com.example.shiftcalendar.ui.calendar

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.crews.vmFactory
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(container: AppContainer) {
    val vm: CalendarViewModel = viewModel(factory = vmFactory { CalendarViewModel(container) })
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("График вахт", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${state.year}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.setYear(state.year - 1) }) {
                        Icon(Icons.Outlined.ChevronLeft, "Предыдущий год")
                    }
                    IconButton(onClick = { vm.setYear(state.year + 1) }) {
                        Icon(Icons.Outlined.ChevronRight, "Следующий год")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (state.crews.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { vm.setTab(0) },
                        text = { Text("Сводный") }
                    )
                    state.crews.forEachIndexed { index, cwp ->
                        Tab(
                            selected = state.selectedTab == index + 1,
                            onClick = { vm.setTab(index + 1) },
                            text = { Text(cwp.crew.name) }
                        )
                    }
                }
            }

            val pagerState = rememberPagerState(
                initialPage = state.selectedTab,
                pageCount = { 1 + state.crews.size }
            )

            LaunchedEffect(state.selectedTab) {
                pagerState.animateScrollToPage(state.selectedTab)
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val crewId = if (page == 0) null else state.crews[page - 1].crew.id
                YearView(year = state.year, crewId = crewId, vm = vm)
            }
        }
    }
}

@Composable
private fun YearView(year: Int, crewId: Long?, vm: CalendarViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items((1..12).toList()) { month ->
            MonthGrid(year = year, month = month, crewId = crewId, vm = vm)
        }
        item { LegendCard() }
    }
}

@Composable
private fun LegendCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Легенда", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LegendRow(ShiftColors.WorkBlue, "Работа")
            LegendRow(ShiftColors.NightViolet, "Ночь 🌙")
            LegendRow(ShiftColors.RoadAmber, "Дорога 🚗")
            LegendRow(ShiftColors.OffGreen, "Отдых")
            LegendRow(ShiftColors.HolidayRed, "Праздник")
            LegendRow(ShiftColors.WeekendGray, "Выходной")
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(Modifier.size(12.dp).clip(MaterialTheme.shapes.extraSmall).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
