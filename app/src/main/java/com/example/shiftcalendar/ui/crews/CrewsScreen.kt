package com.example.shiftcalendar.ui.crews

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shiftcalendar.data.db.entity.Crew
import com.example.shiftcalendar.data.repository.CrewWithPeriods
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.navigation.Routes
import com.example.shiftcalendar.ui.theme.ShiftColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewsScreen(container: AppContainer, navController: NavController) {
    val vm: CrewsViewModel = viewModel(factory = vmFactory { CrewsViewModel(container) })
    val crews by vm.crews.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCrew by remember { mutableStateOf<Crew?>(null) }
    var deletingCrew by remember { mutableStateOf<Crew?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Составы вахт", fontWeight = FontWeight.SemiBold) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Новый состав") }
            )
        }
    ) { padding ->
        if (crews.isEmpty()) {
            EmptyCrewsState(
                modifier = Modifier.padding(padding).fillMaxSize(),
                onCreate = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(crews, key = { it.crew.id }) { cwp ->
                    CrewCard(
                        cwp = cwp,
                        onClick = { navController.navigate(Routes.crewDetail(cwp.crew.id)) },
                        onEdit = { editingCrew = cwp.crew },
                        onDelete = { deletingCrew = cwp.crew }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CrewEditDialog(
            crew = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, color -> vm.createCrew(name, color); showCreateDialog = false }
        )
    }

    editingCrew?.let { crew ->
        CrewEditDialog(
            crew = crew,
            onDismiss = { editingCrew = null },
            onSave = { name, color ->
                vm.updateCrew(crew.copy(name = name, colorHex = color))
                editingCrew = null
            }
        )
    }

    deletingCrew?.let { crew ->
        AlertDialog(
            onDismissRequest = { deletingCrew = null },
            title = { Text("Удалить состав?") },
            text = { Text("«${crew.name}» и все его вахты будут удалены.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteCrew(crew); deletingCrew = null }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCrew = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun CrewCard(
    cwp: CrewWithPeriods,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "crewCard"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(parseColor(cwp.crew.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Groups, null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cwp.crew.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append("${cwp.periods.size} ${pluralVakhta(cwp.periods.size)}")
                        if (cwp.periods.isNotEmpty()) {
                            append(" · ${cwp.periods.first().startDate} – ${cwp.periods.last().endDate}")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Изменить") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyCrewsState(modifier: Modifier, onCreate: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Groups, null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text("Пока нет составов", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Создайте первый состав и добавьте в него вахты",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onCreate) {
            Icon(Icons.Outlined.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Создать состав")
        }
    }
}

@Composable
private fun CrewEditDialog(
    crew: Crew?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(crew?.name ?: "") }
    var colorHex by remember { mutableStateOf(crew?.colorHex ?: "#3B82F6") }

    val palette = listOf("#3B82F6", "#8B5CF6", "#F59E0B", "#10B981", "#EF4444", "#EC4899", "#14B8A6", "#6366F1")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (crew == null) "Новый состав" else "Изменить состав") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Цвет", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { hex ->
                        val selected = hex == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(parseColor(hex))
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) Text("✓", color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), colorHex) },
                enabled = name.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

internal fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    ShiftColors.WorkBlue
}

internal fun pluralVakhta(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "вахта"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "вахты"
    else -> "вахт"
}
