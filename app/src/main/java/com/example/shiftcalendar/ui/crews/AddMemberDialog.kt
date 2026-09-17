package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shiftcalendar.di.AppContainer

@Composable
fun AddMemberDialog(
    container: AppContainer,
    existingIds: Set<Long>,
    onDismiss: () -> Unit,
    onAdd: (personId: Long) -> Unit
) {
    val all by container.personRepository.observePeople()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val available = all.filter { it.id !in existingIds }
    var selected by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить человека") },
        text = {
            Column {
                if (available.isEmpty()) {
                    Text("Все люди уже в составе. Добавьте новых в разделе «Люди».",
                        style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(available, key = { it.id }) { p ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = p.id }
                                    .padding(vertical = 6.dp)
                            ) {
                                RadioButton(selected = selected == p.id, onClick = { selected = p.id })
                                Column(Modifier.weight(1f)) {
                                    Text(p.fullName)
                                    Text(
                                        "${p.profession.titleRu} · ${p.residence.titleRu}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = { selected?.let { onAdd(it) } }
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
