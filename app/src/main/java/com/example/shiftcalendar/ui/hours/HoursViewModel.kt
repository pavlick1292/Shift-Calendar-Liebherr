package com.example.shiftcalendar.ui.hours

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.HoursCategory
import com.example.shiftcalendar.data.db.entity.HoursOverride
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.domain.HoursCalculator
import com.example.shiftcalendar.domain.ShiftCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private fun currentYear(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

data class MonthHours(
    val month: Int,
    val regularHours: Double,
    val nightHours: Double
) {
    val total: Double get() = regularHours + nightHours
}

data class HoursUiState(
    val year: Int = currentYear(),
    val hasMe: Boolean = false,
    val meName: String = "",
    val totalRegular: Double = 0.0,
    val totalNight: Double = 0.0,
    val totalAll: Double = 0.0,
    val norm: Double = 1972.0,
    val monthly: List<MonthHours> = emptyList(),
    val isLoading: Boolean = true
)

class HoursViewModel(private val container: AppContainer) : ViewModel() {

    private val _year = MutableStateFlow(currentYear())
    val year: StateFlow<Int> = _year

    val state: StateFlow<HoursUiState> = _year.flatMapLatest { year ->
        flow { emit(compute(year)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HoursUiState())

    fun setYear(y: Int) { _year.value = y }

    private suspend fun compute(year: Int): HoursUiState {
        val allPeople = container.personRepository.observePeople().first()
        val me: Person? = allPeople.firstOrNull { it.isMe }

        if (me == null) {
            return HoursUiState(year = year, hasMe = false, isLoading = false)
        }

        val crewsWithPeriods = container.crewRepository.observeCrewsWithPeriods().first()
        val periodsByCrew = crewsWithPeriods.associate { it.crew.id to it.periods }
        val calendar = container.calendarRepository.get(year)
        val shiftCalc = ShiftCalculator(calendar)
        val settings = container.settings.hoursSettings.first()
        val calc = HoursCalculator(shiftCalc, settings)

        val memberships = container.personRepository.getMembershipsOfPerson(me.id)
        val overrides = container.overrideRepository.getHoursOverridesForYear(me.id, year)

        val wh = calc.calculateForPerson(
            personId = me.id,
            year = year,
            memberships = memberships,
            periodsByCrew = periodsByCrew,
            hoursOverrides = overrides
        )

        val monthly = computeMonthlyHours(
            year = year,
            memberships = memberships,
            periodsByCrew = periodsByCrew,
            overrides = overrides,
            settings = settings
        )

        return HoursUiState(
            year = year,
            hasMe = true,
            meName = me.fullName,
            totalRegular = wh.regularHours,
            totalNight = wh.nightHours,
            totalAll = wh.total,
            norm = settings.yearlyNorm,
            monthly = monthly,
            isLoading = false
        )
    }

    private fun computeMonthlyHours(
        year: Int,
        memberships: List<CrewMember>,
        periodsByCrew: Map<Long, List<ShiftPeriod>>,
        overrides: List<HoursOverride>,
        settings: HoursSettings
    ): List<MonthHours> {
        val result = MutableList(12) { MonthHours(it + 1, 0.0, 0.0) }
        val overrideByDate = overrides.associateBy { it.date }

        val allMyPeriods = memberships
            .flatMap { m -> periodsByCrew[m.crewId].orEmpty().map { m to it } }

        var date = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)

        while (date <= end) {
            val monthIdx = date.monthNumber - 1
            val override = overrideByDate[date]

            if (override != null) {
                when (override.category) {
                    HoursCategory.NIGHT ->
                        result[monthIdx] = result[monthIdx].copy(
                            nightHours = result[monthIdx].nightHours + override.hours
                        )
                    else -> result[monthIdx] = result[monthIdx].copy(
                        regularHours = result[monthIdx].regularHours + override.hours
                    )
                }
            } else {
                var done = false
                for ((member, period) in allMyPeriods) {
                    if (done) break
                    if (date >= period.startDate && date <= period.endDate) {
                        val isNight = period.isNightShift || member.worksAtNight
                        val h = if (isNight) settings.effectiveNightHours
                                else settings.effectiveShiftHours
                        result[monthIdx] = if (isNight) {
                            result[monthIdx].copy(nightHours = result[monthIdx].nightHours + h)
                        } else {
                            result[monthIdx].copy(regularHours = result[monthIdx].regularHours + h)
                        }
                        done = true
                    }
                }
            }
            date = date.plus(DatePeriod(days = 1))
        }
        return result
    }
}
