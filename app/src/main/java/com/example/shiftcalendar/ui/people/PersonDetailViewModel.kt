package com.example.shiftcalendar.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.domain.HoursCalculator
import com.example.shiftcalendar.domain.ShiftCalculator
import com.example.shiftcalendar.domain.model.WorkHours
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class PersonDetailState(
    val person: Person? = null,
    val crewNames: List<Pair<Long, String>> = emptyList(),
    val nightInCrew: Map<Long, Boolean> = emptyMap(),
    val hours: WorkHours? = null,
    val isLoading: Boolean = true
)

class PersonDetailViewModel(
    private val container: AppContainer,
    private val personId: Long
) : ViewModel() {

    val state: StateFlow<PersonDetailState> = flow {
        val person = container.personRepository.getPerson(personId)
        val memberships = container.personRepository.getMembershipsOfPerson(personId)
        val crews = container.crewRepository.observeCrews().first()
        val crewNames = memberships.mapNotNull { m ->
            crews.firstOrNull { it.id == m.crewId }?.let { it.id to it.name }
        }
        val nightMap = memberships.associate { it.crewId to it.worksAtNight }

        val year = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year

        val cwp = container.crewRepository.observeCrewsWithPeriods().first()
        val periodsByCrew = cwp.associate { it.crew.id to it.periods }
        val calendar = container.calendarRepository.get(year)
        val settings = container.settings.hoursSettings.first()
        val shiftCalc = ShiftCalculator(calendar)
        val hoursCalc = HoursCalculator(shiftCalc, settings)
        val overrides = container.overrideRepository.getHoursOverridesForYear(personId, year)

        val hours = hoursCalc.calculateForPerson(
            personId = personId, year = year,
            memberships = memberships,
            periodsByCrew = periodsByCrew,
            hoursOverrides = overrides
        )

        emit(PersonDetailState(
            person = person,
            crewNames = crewNames,
            nightInCrew = nightMap,
            hours = hours,
            isLoading = false
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PersonDetailState())
}

