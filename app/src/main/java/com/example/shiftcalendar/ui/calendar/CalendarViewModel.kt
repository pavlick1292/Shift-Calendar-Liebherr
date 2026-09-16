package com.example.shiftcalendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.domain.ShiftCalculator
import com.example.shiftcalendar.domain.model.DayStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val year: Int = LocalDate.now().year,
    val selectedTab: Int = 0,
    val crews: List<CrewWithPeriods> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(private val container: AppContainer) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
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

    fun dayStatus(date: LocalDate, crewId: Long?): DayStatus {
        val state = uiState.value
        val calendar = container.calendarRepository.get(state.year)
        val calc = ShiftCalculator(calendar)

        val periods = if (crewId == null) {
            state.crews.flatMap { it.periods }
        } else {
            state.crews.firstOrNull { it.crew.id == crewId }?.periods.orEmpty()
        }

        return calc.dayStatusFor(date, periods)
    }
}
