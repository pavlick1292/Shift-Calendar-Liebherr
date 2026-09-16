package com.example.shiftcalendar.ui.crews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.CrewMember
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class CrewDetailState(
    val crew: Crew? = null,
    val periods: List<ShiftPeriod> = emptyList(),
    val members: List<PersonWithMembership> = emptyList(),
    val isLoading: Boolean = true
)

class CrewDetailViewModel(
    private val container: AppContainer,
    private val crewId: Long
) : ViewModel() {

    val state: StateFlow<CrewDetailState> = combine(
        container.crewRepository.observeCrew(crewId),
        container.crewRepository.observePeriods(crewId),
        container.personRepository.observeMembersOfCrew(crewId)
    ) { crew, periods, members ->
        CrewDetailState(crew, periods, members, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CrewDetailState())

    fun addPeriod(period: ShiftPeriod) {
        viewModelScope.launch {
            container.crewRepository.createPeriod(period.copy(crewId = crewId))
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun updatePeriod(period: ShiftPeriod) {
        viewModelScope.launch {
            container.crewRepository.updatePeriod(period)
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun deletePeriod(period: ShiftPeriod) {
        viewModelScope.launch {
            container.crewRepository.deletePeriod(period)
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun generatePeriods(
        startDate: LocalDate, shiftDays: Int, restDays: Int, count: Int,
        roadBefore: Int, roadAfter: Int, isNight: Boolean
    ) {
        viewModelScope.launch {
            container.crewRepository.generatePeriodsByTemplate(
                crewId = crewId, startDate = startDate, shiftDays = shiftDays,
                restDays = restDays, count = count, roadBefore = roadBefore,
                roadAfter = roadAfter, isNight = isNight
            )
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun removeMember(personId: Long) {
        viewModelScope.launch { container.personRepository.removeFromCrew(personId, crewId) }
    }

    fun toggleNight(personId: Long, currentValue: Boolean) {
        viewModelScope.launch {
            container.personRepository.updateMembership(
                CrewMember(personId = personId, crewId = crewId, worksAtNight = !currentValue)
            )
        }
    }

    fun addMember(personId: Long, worksAtNight: Boolean) {
        viewModelScope.launch {
            container.personRepository.addToCrew(personId, crewId, worksAtNight)
        }
    }
}
