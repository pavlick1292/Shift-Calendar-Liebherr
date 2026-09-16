package com.example.shiftcalendar.ui.people

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.Profession
import com.example.shiftcalendar.data.db.entity.Residence
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.crews.vmFactory
import com.example.shiftcalendar.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(container: AppContainer, navController: NavController) {
    val vm: PeopleViewModel = viewModel(factory = vmFactory { PeopleViewModel(container) })
    val people by vm.people.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    var editing by remember { mutableStateOf<Person?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Person?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Люди", fontWeight = FontWeight.SemiBold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                icon = { Icon(Icons.Outlined.PersonAdd, null) },
                text = { Text("Добавить") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Поиск по имени") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp)
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    FilterChip(
                        selected = filter.profession != null,
                        onClick = { expanded = true },
                        label = { Text(filter.profession?.titleRu ?: "Профессия") }
                    )
                    DropdownMenu(expanded, { expanded = false }) {
                        Profession.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.titleRu) },
                                onClick = { vm.setProfession(p); expanded = false }
                            )
                        }
                    }
                }
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    FilterChip(
                        selected = filter.residence != null,
                        onClick = { expanded = true },
                        label = { Text(filter.residence?.titleRu ?: "Проживание") }
                    )
                    DropdownMenu(expanded, { expanded = false }) {
                        Residence.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.titleRu) },
                                onClick = { vm.setResidence(r); expanded = false }
                            )
                        }
                    }
                }
                if (filter.profession != null || filter.residence != null) {
                    TextButton(onClick = {
                        vm.setProfession(null); vm.setResidence(null)
                    }) { Text("Сбросить") }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(people, key = { it.id }) { p ->
                    PersonCard(
                        person = p,
                        onClick = { navController.navigate(Routes.personDetail(p.id)) },
                        onEdit = { editing = p },
                        onDelete = { deleting = p }
                    )
                }
            }
        }
    }

    if (showCreate || editing != null) {
        PersonDialog(
            person = editing,
            onDismiss = { showCreate = false; editing = null },
            onSave = { vm.save(it); showCreate = false; editing = null }
        )
    }

    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить человека?") },
            text = { Text("«${p.fullName}» будет удалён из всех составов.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(p); deleting = null }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun PersonCard(
    person: Person,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(person.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(person.profession.titleRu,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🏠 ${person.residence.titleRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Изменить") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
