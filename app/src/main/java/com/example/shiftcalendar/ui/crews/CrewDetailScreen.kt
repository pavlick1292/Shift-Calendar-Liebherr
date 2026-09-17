package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.shiftcalendar.export.ShiftPeriodPdfExporter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    var showExportDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Вахты", "Люди")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.crew?.name ?: "Состав") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (tab == 0 && state.periods.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Outlined.IosShare, "Экспорт")
                        }
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
            }
        }
    }

    if (showExportDialog) {
        CrewExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = {
                showExportDialog = false
                val crewName = state.crew?.name ?: "Состав"
                val crews = listOf(
                    ShiftPeriodPdfExporter.CrewData(
                        name = crewName,
                        periods = state.periods
                    )
                )
                val file = ShiftPeriodPdfExporter.export(container.appContext, crews)
                ShiftPeriodPdfExporter.share(container.appContext, file)
            }
        )
    }
}

@Composable
private fun CrewExportDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Экспорт PDF") },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onExport)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📋 Список вахт",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Таблица с датами и количеством дней.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun ShiftsTab(vm: CrewDetailViewModel, periods: List<ShiftPeriod>) {
    var editing by remember { mutableStateOf<ShiftPeriod?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var showGenerate by remember { mutableStateOf(false) }
    var showContinue by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (periods.isEmpty()) {
                item {
                    Text("Вахт пока нет. Добавьте вручную или сгенерируйте по циклу.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(periods, key = { it.id }) { p ->
                ShiftPeriodCard(
                    period = p,
                    onEdit = { editing = p },
                    onDelete = { vm.deletePeriod(p) }
                )
            }
            if (periods.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showContinue = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Продолжить цикл вахт")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = { showAdd = true }) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Вахта")
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = { showGenerate = true }) {
                Icon(Icons.Outlined.AutoAwesome, null)
                Spacer(Modifier.width(6.dp))
                Text("По циклу")
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
            onGenerate = { start, shift, rest, count ->
                vm.generatePeriods(start, shift, rest, count)
                showGenerate = false
            }
        )
    }

    if (showContinue && periods.isNotEmpty()) {
        val last = periods.maxByOrNull { it.endDate }!!
        ContinueCycleDialog(
            lastPeriod = last,
            existingPeriods = periods,
            onDismiss = { showContinue = false },
            onGenerate = { count ->
                vm.continueCycle(last, count, periods)
                showContinue = false
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
            Text("${period.startDate} – ${period.endDate}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Дней: ${period.endDate.toEpochDays() - period.startDate.toEpochDays() + 1}" +
                    (if (period.label.isNotBlank()) " · ${period.label}" else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinueCycleDialog(
    lastPeriod: ShiftPeriod,
    existingPeriods: List<ShiftPeriod>,
    onDismiss: () -> Unit,
    onGenerate: (count: Int) -> Unit
) {
    val shiftDays = (lastPeriod.endDate.toEpochDays() - lastPeriod.startDate.toEpochDays() + 1).toInt()
    val previous = existingPeriods.filter { it.endDate < lastPeriod.startDate }.maxByOrNull { it.endDate }
    val restDays = if (previous != null) {
        (lastPeriod.startDate.toEpochDays() - previous.endDate.toEpochDays() - 1).toInt()
    } else 0

    var count by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Продолжить цикл") },
        text = {
            Column {
                Text("Последняя вахта: ${lastPeriod.startDate} – ${lastPeriod.endDate}",
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text("Длина вахты: $shiftDays дн., отдыха: $restDays дн.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it.filter(Char::isDigit) },
                    label = { Text("Сколько вахт добавить") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = (count.toIntOrNull() ?: 0) > 0,
                onClick = { onGenerate(count.toInt()) }
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
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
                MemberRow(m = m, onRemove = { vm.removeMember(m.person.id) })
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
            onAdd = { personId -> vm.addMember(personId); showAdd = false }
        )
    }
}

@Composable
private fun MemberRow(m: PersonWithMembership, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (m.person.isMe) {
                        Text("⭐ ", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(m.person.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Text(m.person.profession.titleRu,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🏠 ${m.person.residence.titleRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Close, "Убрать", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
