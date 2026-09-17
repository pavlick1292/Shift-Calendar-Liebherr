package com.example.shiftcalendar.ui.calendar

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.export.YearCalendarExporter
import com.example.shiftcalendar.ui.crews.vmFactory
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(container: AppContainer) {
    val vm: CalendarViewModel = viewModel(factory = vmFactory { CalendarViewModel(container) })
    val state by vm.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("График вахт", fontWeight = FontWeight.SemiBold)
                        Text(state.year.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.setYear(state.year - 1) }) {
                        Icon(Icons.Outlined.ChevronLeft, "Предыдущий год")
                    }
                    IconButton(onClick = { vm.setYear(state.year + 1) }) {
                        Icon(Icons.Outlined.ChevronRight, "Следующий год")
                    }
                    if (state.crews.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Outlined.IosShare, "Экспорт")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val pagerState = rememberPagerState(
                initialPage = state.selectedTab,
                pageCount = { 1 + state.crews.size }
            )

            // Tab -> Pager
            LaunchedEffect(state.selectedTab) {
                if (pagerState.currentPage != state.selectedTab) {
                    pagerState.animateScrollToPage(state.selectedTab)
                }
            }

            // Pager -> Tab
            LaunchedEffect(pagerState.currentPage) {
                if (state.selectedTab != pagerState.currentPage) {
                    vm.setTab(pagerState.currentPage)
                }
            }

            if (state.crews.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (state.selectedTab < tabPositions.size) {
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[state.selectedTab])
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(ShiftColors.Aurora, ShiftColors.Sunrise)
                                        ),
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                    }
                ) {
                    Tab(selected = state.selectedTab == 0,
                        onClick = { vm.setTab(0) },
                        text = { Text("Сводный") })
                    state.crews.forEachIndexed { index, cwp ->
                        Tab(selected = state.selectedTab == index + 1,
                            onClick = { vm.setTab(index + 1) },
                            text = { Text(cwp.crew.name) })
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val crewId = if (page == 0) null else state.crews[page - 1].crew.id
                YearView(year = state.year, crewId = crewId, vm = vm, crews = state.crews)
            }
        }
    }

    if (showExportDialog) {
        CalendarExportDialog(
            onDismiss = { showExportDialog = false },
            onYearPdf = {
                showExportDialog = false
                scope.launch {
                    val year = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).year
                    val calendar = container.calendarRepository.get(year)
                    val crewsInfo = buildCrewsInfo(container, state.crews)
                    val file = YearCalendarExporter.exportPdf(
                        container.appContext, year, crewsInfo, calendar
                    )
                    YearCalendarExporter.share(container.appContext, file, "application/pdf")
                }
            },
            onYearPng = {
                showExportDialog = false
                scope.launch {
                    val year = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).year
                    val calendar = container.calendarRepository.get(year)
                    val crewsInfo = buildCrewsInfo(container, state.crews)
                    val file = YearCalendarExporter.exportPng(
                        container.appContext, year, crewsInfo, calendar
                    )
                    YearCalendarExporter.share(container.appContext, file, "image/png")
                }
            }
        )
    }
}

private suspend fun buildCrewsInfo(
    container: AppContainer,
    crews: List<CrewWithPeriods>
): List<YearCalendarExporter.CrewInfo> {
    return crews.map { cwp ->
        val members = container.personRepository
            .observeMembersOfCrew(cwp.crew.id)
            .first()
        YearCalendarExporter.CrewInfo(cwp, members)
    }
}

@Composable
private fun YearView(
    year: Int,
    crewId: Long?,
    vm: CalendarViewModel,
    crews: List<CrewWithPeriods>
) {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonth = today.monthNumber
    val currentYear = today.year

    val initialIndex = if (year == currentYear) (currentMonth - 1) else 0
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed((1..12).toList()) { _, month ->
            MonthGrid(year = year, month = month, crewId = crewId, vm = vm, crews = crews)
        }
    }
}
