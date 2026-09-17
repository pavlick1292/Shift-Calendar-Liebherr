package com.example.shiftcalendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.calendar.ProductionCalendar
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.domain.model.CalendarType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private fun currentYear(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

data class ActiveCrew(
    val crewId: Long,
    val crewName: String,
    val colorHex: String,
    val iconType: String
)

data class DayStatus(
    val date: LocalDate,
    val calendarType: CalendarType,
    val activeCrews: List<ActiveCrew>,
    val totalActiveCount: Int
)

data class CalendarUiState(
    val year: Int = currentYear(),
    val selectedTab: Int = 0,
    val crews: List<CrewWithPeriods> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(private val container: AppContainer) : ViewModel() {

    private val _year = MutableStateFlow(currentYear())
    private val _selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<CalendarUiState> = combine(
        container.crewRepository.observeCrewsWithPeriods(),
        _year,
        _selectedTab
    ) { crews, year, tab ->
        CalendarUiState(year = year, selectedTab = tab, crews = crews, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun setYear(year: Int) { _year.value = year }
    fun setTab(index: Int) { _selectedTab.value = index }

    fun getHolidayName(date: LocalDate): String? {
        val state = uiState.value
        val calendar = container.calendarRepository.get(state.year)
        return calendar.holidayName(date)
    }

    fun dayStatusForCrew(date: LocalDate, crewId: Long): DayStatus {
        val state = uiState.value
        val calendar = container.calendarRepository.get(state.year)
        val cwp = state.crews.firstOrNull { it.crew.id == crewId }
            ?: return emptyStatus(date, calendar)

        val isActive = cwp.periods.any { date >= it.startDate && date <= it.endDate }
        val activeCrews = if (isActive) {
            listOf(ActiveCrew(cwp.crew.id, cwp.crew.name, cwp.crew.colorHex, cwp.crew.iconType))
        } else emptyList()

        return DayStatus(date, typeFor(date, calendar), activeCrews, activeCrews.size)
    }

    fun dayStatusSummary(date: LocalDate): DayStatus {
        val state = uiState.value
        val calendar = container.calendarRepository.get(state.year)

        val active = mutableListOf<ActiveCrew>()
        for (cwp in state.crews) {
            val periods = cwp.periods.filter { date >= it.startDate && date <= it.endDate }
            if (periods.isNotEmpty()) {
                active += ActiveCrew(cwp.crew.id, cwp.crew.name, cwp.crew.colorHex, cwp.crew.iconType)
            }
        }

        return DayStatus(date, typeFor(date, calendar), active, active.size)
    }

    private fun typeFor(date: LocalDate, calendar: ProductionCalendar): CalendarType = when {
        calendar.isHoliday(date) -> CalendarType.HOLIDAY
        calendar.isWeekend(date) -> CalendarType.WEEKEND
        else -> CalendarType.WORK_DAY
    }

    private fun emptyStatus(date: LocalDate, calendar: ProductionCalendar) = DayStatus(
        date, typeFor(date, calendar), emptyList(), 0
    )
}
