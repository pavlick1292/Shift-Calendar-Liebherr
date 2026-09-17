package com.example.shiftcalendar.ui.crews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

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
        startDate: LocalDate, shiftDays: Int, restDays: Int, count: Int
    ) {
        viewModelScope.launch {
            container.crewRepository.generatePeriodsByTemplate(
                crewId = crewId, startDate = startDate,
                shiftDays = shiftDays, restDays = restDays, count = count
            )
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun continueCycle(lastPeriod: ShiftPeriod, count: Int, allPeriods: List<ShiftPeriod>) {
        viewModelScope.launch {
            val shiftDays = (lastPeriod.endDate.toEpochDays() -
                    lastPeriod.startDate.toEpochDays() + 1).toInt()

            val previous = allPeriods
                .filter { it.endDate < lastPeriod.startDate }
                .maxByOrNull { it.endDate }
            val restDays = if (previous != null) {
                (lastPeriod.startDate.toEpochDays() -
                        previous.endDate.toEpochDays() - 1).toInt().coerceAtLeast(0)
            } else 0

            val startDate = lastPeriod.endDate.plus(DatePeriod(days = restDays + 1))

            container.crewRepository.generatePeriodsByTemplate(
                crewId = crewId, startDate = startDate,
                shiftDays = shiftDays, restDays = restDays, count = count
            )
            container.notificationScheduler.rescheduleAll()
        }
    }

    fun removeMember(personId: Long) {
        viewModelScope.launch { container.personRepository.removeFromCrew(personId, crewId) }
    }

    fun addMember(personId: Long) {
        viewModelScope.launch {
            container.personRepository.addToCrew(personId, crewId)
        }
    }
}
