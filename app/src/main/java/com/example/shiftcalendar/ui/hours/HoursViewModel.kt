package com.example.shiftcalendar.ui.hours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.domain.HoursCalculator
import com.example.shiftcalendar.domain.ShiftCalculator
import com.example.shiftcalendar.domain.model.WorkHours
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

data class HoursRow(val person: Person, val hours: WorkHours)

data class HoursUiState(
    val year: Int = LocalDate.now().year,
    val rows: List<HoursRow> = emptyList(),
    val totalRegular: Double = 0.0,
    val totalNight: Double = 0.0,
    val totalRoad: Double = 0.0,
    val totalAll: Double = 0.0,
    val norm: Double = 1972.0,
    val isLoading: Boolean = true
)

class HoursViewModel(private val container: AppContainer) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
    val year: StateFlow<Int> = _year

    val state: StateFlow<HoursUiState> = _year.flatMapLatest { year ->
        flow { emit(compute(year)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HoursUiState())

    fun setYear(y: Int) { _year.value = y }

    private suspend fun compute(year: Int): HoursUiState {
        val people = container.personRepository.observePeople().first()
        val crewsWithPeriods = container.crewRepository.observeCrewsWithPeriods().first()
        val periodsByCrew = crewsWithPeriods.associate { it.crew.id to it.periods }
        val calendar = container.calendarRepository.get(year)
        val shiftCalc = ShiftCalculator(calendar)
        val settings = container.settings.hoursSettings.first()
        val calc = HoursCalculator(shiftCalc, settings)

        val rows = people.map { p ->
            val memberships = container.personRepository.getMembershipsOfPerson(p.id)
            val overrides = container.overrideRepository.getHoursOverridesForYear(p.id, year)
            val wh = calc.calculateForPerson(
                personId = p.id, year = year,
                memberships = memberships,
                periodsByCrew = periodsByCrew,
                hoursOverrides = overrides
            )
            HoursRow(p, wh)
        }

        return HoursUiState(
            year = year,
            rows = rows,
            totalRegular = rows.sumOf { it.hours.regularHours },
            totalNight = rows.sumOf { it.hours.nightHours },
            totalRoad = rows.sumOf { it.hours.roadHours },
            totalAll = rows.sumOf { it.hours.total },
            norm = settings.yearlyNorm,
            isLoading = false
        )
    }
}
