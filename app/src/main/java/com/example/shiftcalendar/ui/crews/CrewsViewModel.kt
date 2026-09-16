package com.example.shiftcalendar.ui.crews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CrewsViewModel(private val container: AppContainer) : ViewModel() {

    val crews: StateFlow<List<CrewWithPeriods>> =
        container.crewRepository.observeCrewsWithPeriods()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCrew(name: String, colorHex: String) {
        viewModelScope.launch {
            container.crewRepository.createCrew(Crew(name = name, colorHex = colorHex))
        }
    }

    fun updateCrew(crew: Crew) {
        viewModelScope.launch { container.crewRepository.updateCrew(crew) }
    }

    fun deleteCrew(crew: Crew) {
        viewModelScope.launch {
            container.crewRepository.deleteCrew(crew)
            container.notificationScheduler.rescheduleAll()
        }
    }
}
