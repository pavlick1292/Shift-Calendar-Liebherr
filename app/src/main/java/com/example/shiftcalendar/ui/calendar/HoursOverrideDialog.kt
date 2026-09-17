package com.example.shiftcalendar.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shiftcalendar.data.db.entity.HoursCategory
import com.example.shiftcalendar.data.db.entity.HoursOverride
import com.example.shiftcalendar.data.db.entity.Person
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursOverrideDialog(
    person: Person,
    date: LocalDate,
    existing: HoursOverride?,
    defaultHours: Double,
    onDismiss: () -> Unit,
    onSave: (HoursOverride) -> Unit,
    onDelete: () -> Unit
) {
    var hours by remember { mutableStateOf(existing?.hours?.toString() ?: defaultHours.toString()) }
    var category by remember { mutableStateOf(existing?.category ?: HoursCategory.REGULAR) }
    var reason by remember { mutableStateOf(existing?.reason ?: "") }
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${date}: ${person.fullName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Фактически отработано, ч") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoryLabel(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(catExpanded, { catExpanded = false }) {
                        HoursCategory.entries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(categoryLabel(c)) },
                                onClick = { category = c; catExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Причина (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = hours.replace(',', '.').toDoubleOrNull() != null,
                onClick = {
                    val h = hours.replace(',', '.').toDoubleOrNull() ?: return@TextButton
                    onSave(HoursOverride(
                        personId = person.id,
                        date = date,
                        hours = h,
                        category = category,
                        reason = reason.trim()
                    ))
                }
            ) { Text("Сохранить") }
        },
        dismissButton = {
            Row {
                if (existing != null) {
                    TextButton(onClick = onDelete) {
                        Text("Сбросить", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}

private fun categoryLabel(c: HoursCategory): String = when (c) {
    HoursCategory.REGULAR -> "Обычные"
    HoursCategory.OVERTIME -> "Переработка"
    HoursCategory.SICK -> "Больничный"
    HoursCategory.VACATION -> "Отгул / отпуск"
}
