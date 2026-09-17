package com.example.shiftcalendar.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.example.shiftcalendar.ui.theme.ShiftCalendarTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ShiftWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val initial = WidgetSettings.getBackground(this)

        setContent {
            ShiftCalendarTheme {
                var selected by remember { mutableStateOf(initial) }

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Настройка виджета") })
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Выбери фон виджета:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(16.dp))

                        WidgetBg.entries.forEach { bg ->
                            BgOption(
                                bg = bg,
                                selected = bg == selected,
                                onClick = { selected = bg }
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                WidgetSettings.setBackground(
                                    this@ShiftWidgetConfigActivity,
                                    selected
                                )

                                lifecycleScope.launch {
                                    try {
                                        // Обновляем через конкретный GlanceId
                                        val manager = GlanceAppWidgetManager(
                                            this@ShiftWidgetConfigActivity
                                        )
                                        val glanceIds = manager.getGlanceIds(
                                            ShiftWidget::class.java
                                        )
                                        glanceIds.forEach { glanceId ->
                                            ShiftWidget().update(
                                                this@ShiftWidgetConfigActivity,
                                                glanceId
                                            )
                                        }
                                    } catch (e: Exception) {
                                        // Не критично — попробуем updateAll как fallback
                                    }

                                    try {
                                        ShiftWidget().updateAll(
                                            this@ShiftWidgetConfigActivity
                                        )
                                    } catch (e: Exception) {
                                        // ignore
                                    }

                                    val resultValue = Intent().apply {
                                        putExtra(
                                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                                            appWidgetId
                                        )
                                    }
                                    setResult(Activity.RESULT_OK, resultValue)
                                    finish()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Готово")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BgOption(
    bg: WidgetBg,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
                      else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(bg.topColor), Color(bg.bottomColor))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "6",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(bg.accentColor)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                bg.titleRu,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(Modifier.weight(1f))

            if (selected) {
                Text(
                    "✓",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
