package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import kotlinx.datetime.LocalDate

@Composable
fun ShiftPeriodDialog(
    period: ShiftPeriod?,
    onDismiss: () -> Unit,
    onSave: (ShiftPeriod) -> Unit
) {
    var start by remember { mutableStateOf(period?.startDate?.toString() ?: "") }
    var end by remember { mutableStateOf(period?.endDate?.toString() ?: "") }
    var roadBefore by remember { mutableStateOf((period?.roadDaysBefore ?: 1).toString()) }
    var roadAfter by remember { mutableStateOf((period?.roadDaysAfter ?: 1).toString()) }
    var night by remember { mutableStateOf(period?.isNightShift ?: false) }
    var label by remember { mutableStateOf(period?.label ?: "") }

    val startDate = runCatching { LocalDate.parse(start) }.getOrNull()
    val endDate = runCatching { LocalDate.parse(end) }.getOrNull()
    val valid = startDate != null && endDate != null && endDate >= startDate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (period == null) "Новая вахта" else "Изменить вахту") },
        text = {
            Column {
                OutlinedTextField(
                    value = start, onValueChange = { start = it },
                    label = { Text("Начало (ГГГГ-ММ-ДД)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = end, onValueChange = { end = it },
                    label = { Text("Конец (ГГГГ-ММ-ДД)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roadBefore, onValueChange = { roadBefore = it.filter { c -> c.isDigit() } },
                        label = { Text("Дорога до") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = roadAfter, onValueChange = { roadAfter = it.filter { c -> c.isDigit() } },
                        label = { Text("Дорога после") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text("Название (необязательно)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = night, onCheckedChange = { night = it })
                    Text("Ночная смена")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        ShiftPeriod(
                            id = period?.id ?: 0,
                            crewId = period?.crewId ?: 0,
                            startDate = startDate!!,
                            endDate = endDate!!,
                            roadDaysBefore = roadBefore.toIntOrNull() ?: 1,
                            roadDaysAfter = roadAfter.toIntOrNull() ?: 1,
                            isNightShift = night,
                            label = label
                        )
                    )
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
