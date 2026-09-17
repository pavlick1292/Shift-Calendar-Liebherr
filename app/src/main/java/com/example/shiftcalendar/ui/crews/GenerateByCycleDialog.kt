package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateByCycleDialog(
    onDismiss: () -> Unit,
    onGenerate: (start: LocalDate, shiftDays: Int, restDays: Int, count: Int) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate(2025, 1, 1)) }
    var shift by remember { mutableStateOf("30") }
    var rest by remember { mutableStateOf("30") }
    var count by remember { mutableStateOf("6") }
    var showStartPicker by remember { mutableStateOf(false) }

    val valid = (shift.toIntOrNull() ?: 0) > 0 && (count.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Генерация вахт по циклу") },
        text = {
            Column {
                DateFieldRu(
                    label = "Начало первой вахты",
                    date = startDate,
                    onPick = { showStartPicker = true }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shift,
                        onValueChange = { shift = it.filter(Char::isDigit) },
                        label = { Text("Вахта, дн.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { rest = it.filter(Char::isDigit) },
                        label = { Text("Отдых, дн.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it.filter(Char::isDigit) },
                    label = { Text("Количество вахт") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onGenerate(
                        startDate,
                        shift.toInt(),
                        rest.toIntOrNull() ?: 30,
                        count.toInt()
                    )
                }
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (showStartPicker) {
        SimpleDatePickerDialog(
            initial = startDate,
            onDismiss = { showStartPicker = false },
            onPicked = { startDate = it; showStartPicker = false }
        )
    }
}
