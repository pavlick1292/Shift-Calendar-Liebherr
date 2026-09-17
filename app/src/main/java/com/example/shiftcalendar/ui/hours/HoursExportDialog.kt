package com.example.shiftcalendar.ui.hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HoursExportDialog(
    onDismiss: () -> Unit,
    onPdf: () -> Unit,
    onPng: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Мой график вахт") },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onPdf)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📄 PDF (для печати)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Список твоих вахт за год, А4.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onPng)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🖼 PNG (для телефона)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Картинка твоего графика — на телефон.",
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
