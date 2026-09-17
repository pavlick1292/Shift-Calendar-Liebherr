package com.example.shiftcalendar.ui.people

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import com.example.shiftcalendar.data.db.entity.Person
import com.example.shiftcalendar.data.db.entity.Profession
import com.example.shiftcalendar.data.db.entity.Residence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDialog(
    person: Person?,
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit
) {
    var name by remember { mutableStateOf(person?.fullName ?: "") }
    var profession by remember { mutableStateOf(person?.profession ?: Profession.SERVICE_ENGINEER) }
    var residence by remember { mutableStateOf(person?.residence ?: Residence.BUM) }
    var phone by remember { mutableStateOf(person?.phone ?: "") }
    var note by remember { mutableStateOf(person?.note ?: "") }
    var isMe by remember { mutableStateOf(person?.isMe ?: false) }

    var profExpanded by remember { mutableStateOf(false) }
    var resExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (person == null) "Новый человек" else "Изменить") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("ФИО") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                ExposedDropdownMenuBox(profExpanded, { profExpanded = it }) {
                    OutlinedTextField(
                        value = profession.titleRu,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Профессия") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(profExpanded) },
                        modifier = Modifier.fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(profExpanded, { profExpanded = false }) {
                        Profession.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.titleRu) },
                                onClick = { profession = p; profExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                ExposedDropdownMenuBox(resExpanded, { resExpanded = it }) {
                    OutlinedTextField(
                        value = residence.titleRu,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Проживание") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(resExpanded) },
                        modifier = Modifier.fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(resExpanded, { resExpanded = false }) {
                        Residence.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.titleRu) },
                                onClick = { residence = r; resExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Телефон") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMe, onCheckedChange = { isMe = it })
                    Text("Это я")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(Person(
                        id = person?.id ?: 0,
                        fullName = name.trim(),
                        profession = profession,
                        residence = residence,
                        phone = phone.trim(),
                        note = note.trim(),
                        isMe = isMe
                    ))
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
