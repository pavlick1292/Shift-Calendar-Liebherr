package com.example.shiftcalendar.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌅", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Добро пожаловать!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column {
                Text(
                    "Это приложение для отслеживания графика вахт.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Text("Что можно делать:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                BulletText("📅 Вести график вахт на год")
                BulletText("👥 Создавать несколько составов")
                BulletText("🧑 Добавлять людей с профессиями")
                BulletText("⏱ Считать свои отработанные часы")
                BulletText("📤 Экспортировать календарь и часы")
                BulletText("🖼 Видеть отсчёт на виджете")
                Spacer(Modifier.height(12.dp))
                Text(
                    "При первом заходе на каждую вкладку появится краткая подсказка.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Начать")
            }
        }
    )
}

@Composable
fun TabHintDialog(
    hint: TabHint,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(hint.emoji, fontSize = 42.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    hint.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                hint.text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Понятно")
            }
        }
    )
}

@Composable
private fun BulletText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
