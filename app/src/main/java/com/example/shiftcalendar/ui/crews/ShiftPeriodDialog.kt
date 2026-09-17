package com.example.shiftcalendar.ui.crews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shiftcalendar.data.db.entity.ShiftPeriod
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftPeriodDialog(
    period: ShiftPeriod?,
    onDismiss: () -> Unit,
    onSave: (ShiftPeriod) -> Unit
) {
    var startDate by remember { mutableStateOf(period?.startDate ?: LocalDate(2025, 1, 1)) }
    var endDate by remember { mutableStateOf(period?.endDate ?: LocalDate(2025, 1, 30)) }
    var label by remember { mutableStateOf(period?.label ?: "") }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val valid = endDate >= startDate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (period == null) "Новая вахта" else "Изменить вахту") },
        text = {
            Column {
                DateFieldRu(
                    label = "Начало вахты",
                    date = startDate,
                    onPick = { showStartPicker = true }
                )
                Spacer(Modifier.height(8.dp))
                DateFieldRu(
                    label = "Конец вахты",
                    date = endDate,
                    onPick = { showEndPicker = true }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Название (необязательно)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(ShiftPeriod(
                        id = period?.id ?: 0,
                        crewId = period?.crewId ?: 0,
                        startDate = startDate,
                        endDate = endDate,
                        label = label
                    ))
                }
            ) { Text("Сохранить") }
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
    if (showEndPicker) {
        SimpleDatePickerDialog(
            initial = endDate,
            onDismiss = { showEndPicker = false },
            onPicked = { endDate = it; showEndPicker = false }
        )
    }
}

@Composable
internal fun DateFieldRu(label: String, date: LocalDate, onPick: () -> Unit) {
    OutlinedTextField(
        value = date.formatRu(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onPick) {
                Icon(Icons.Outlined.CalendarMonth, "Выбрать дату")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SimpleDatePickerDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPicked: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = localDateToMillis(initial)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPicked(millisToLocalDate(it)) } ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    ) {
        DatePicker(state = state)
    }
}

internal fun LocalDate.formatRu(): String {
    val d = dayOfMonth.toString().padStart(2, '0')
    val m = monthNumber.toString().padStart(2, '0')
    return "$d.$m.$year"
}

internal fun localDateToMillis(date: LocalDate): Long =
    date.toEpochDays().toLong() * 86_400_000L

internal fun millisToLocalDate(millis: Long): LocalDate =
    LocalDate.fromEpochDays((millis / 86_400_000L).toInt())
