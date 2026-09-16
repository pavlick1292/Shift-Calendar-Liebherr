package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shiftcalendar.data.db.dao.PersonWithMembership
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.theme.ShiftColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewDetailScreen(
    container: AppContainer,
    crewId: Long,
    navController: NavController
) {
    val vm: CrewDetailViewModel = viewModel(
        key = "crew_$crewId",
        factory = vmFactory { CrewDetailViewModel(container, crewId) }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Вахты", "Люди", "Настройки")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.crew?.name ?: "Состав") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
                }
            }
            when (tab) {
                0 -> ShiftsTab(vm, state.periods)
                1 -> PeopleTab(container, vm, state.members)
                2 -> SettingsTab()
            }
        }
    }
}

@Composable
private fun ShiftsTab(vm: CrewDetailViewModel, periods: List<ShiftPeriod>) {
    var editing by remember { mutableStateOf<ShiftPeriod?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var showGenerate by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (periods.isEmpty()) {
                item {
                    Text(
                        "Вахт пока нет. Добавьте вручную или сгенерируйте по циклу.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(periods, key = { it.id }) { p ->
                ShiftPeriodCard(
                    period = p,
                    onEdit = { editing = p },
                    onDelete = { vm.deletePeriod(p) }
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showAdd = true }) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Вахта")
                }
                Button(onClick = { showGenerate = true }) {
                    Icon(Icons.Outlined.AutoAwesome, null)
                    Spacer(Modifier.width(6.dp))
                    Text("По циклу")
                }
            }
        }
    }

    if (showAdd || editing != null) {
        ShiftPeriodDialog(
            period = editing,
            onDismiss = { showAdd = false; editing = null },
            onSave = { p ->
                if (editing != null) vm.updatePeriod(p) else vm.addPeriod(p)
                showAdd = false; editing = null
            }
        )
    }

    if (showGenerate) {
        GenerateByCycleDialog(
            onDismiss = { showGenerate = false },
            onGenerate = { start, shift, rest, count, roadB, roadA, night ->
                vm.generatePeriods(start, shift, rest, count, roadB, roadA, night)
                showGenerate = false
            }
        )
    }
}

@Composable
private fun ShiftPeriodCard(
    period: ShiftPeriod,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${period.startDate} – ${period.endDate}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (period.isNightShift) {
                    Icon(Icons.Outlined.Nightlight, null,
                        tint = ShiftColors.NightViolet, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Дорога: ${period.roadDaysBefore} до / ${period.roadDaysAfter} после" +
                    (if (period.label.isNotBlank()) " · ${period.label}" else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit) { Text("Изменить") }
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PeopleTab(
    container: AppContainer,
    vm: CrewDetailViewModel,
    members: List<PersonWithMembership>
) {
    var showAdd by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (members.isEmpty()) {
                item {
                    Text("В составе пока нет людей.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(members, key = { it.person.id }) { m ->
                MemberRow(
                    m = m,
                    onToggleNight = { vm.toggleNight(m.person.id, m.worksAtNight) },
                    onRemove = { vm.removeMember(m.person.id) }
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            icon = { Icon(Icons.Outlined.PersonAdd, null) },
            text = { Text("Добавить") }
        )
    }

    if (showAdd) {
        AddMemberDialog(
            container = container,
            existingIds = members.map { it.person.id }.toSet(),
            onDismiss = { showAdd = false },
            onAdd = { personId, night -> vm.addMember(personId, night); showAdd = false }
        )
    }
}

@Composable
private fun MemberRow(
    m: PersonWithMembership,
    onToggleNight: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m.person.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (m.worksAtNight) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.Nightlight, null,
                            tint = ShiftColors.NightViolet, modifier = Modifier.size(16.dp))
                    }
                }
                Text(m.person.profession.titleRu,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🏠 ${m.person.residence.titleRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onToggleNight) {
                Icon(
                    if (m.worksAtNight) Icons.Outlined.Nightlight else Icons.Outlined.NightsStay,
                    "Ночная смена",
                    tint = if (m.worksAtNight) ShiftColors.NightViolet
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Close, "Убрать", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SettingsTab() {
    Column(Modifier.padding(16.dp)) {
        Text("Настройки состава — в разработке", style = MaterialTheme.typography.bodyLarge)
    }
}
