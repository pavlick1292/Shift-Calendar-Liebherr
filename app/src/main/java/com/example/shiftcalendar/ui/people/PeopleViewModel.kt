package com.example.shiftcalendar.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.Profession
import com.example.shiftcalendar.data.db.entity.Residence
import com.example.shiftcalendar.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PeopleFilter(
    val query: String = "",
    val profession: Profession? = null,
    val residence: Residence? = null
)

class PeopleViewModel(private val container: AppContainer) : ViewModel() {

    private val _filter = MutableStateFlow(PeopleFilter())
    val filter: StateFlow<PeopleFilter> = _filter

    val people: StateFlow<List<Person>> = combine(
        container.personRepository.observePeople(),
        _filter
    ) { list, f ->
        list.filter { p ->
            (f.query.isBlank() || p.fullName.contains(f.query, true)) &&
            (f.profession == null || p.profession == f.profession) &&
            (f.residence == null || p.residence == f.residence)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _filter.value = _filter.value.copy(query = q) }
    fun setProfession(p: Profession?) { _filter.value = _filter.value.copy(profession = p) }
    fun setResidence(r: Residence?) { _filter.value = _filter.value.copy(residence = r) }

    fun save(person: Person) {
        viewModelScope.launch {
            if (person.id == 0L) container.personRepository.createPerson(person)
            else container.personRepository.updatePerson(person)
        }
    }

    fun delete(person: Person) {
        viewModelScope.launch { container.personRepository.deletePerson(person) }
    }
}
