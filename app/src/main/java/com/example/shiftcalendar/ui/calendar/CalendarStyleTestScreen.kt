package com.example.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.shiftcalendar.data.settings.AppearanceSettings
import com.example.shiftcalendar.data.settings.CalendarStyle
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.theme.ShiftColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarStyleTestScreen(
    container: AppContainer,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val appearance by container.settings.appearanceSettings
        .collectAsStateWithLifecycle(initialValue = AppearanceSettings())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стиль календаря") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Выбери стиль — применяется сразу",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            items(CalendarStyle.entries, key = { it.name }) { style ->
                val selected = appearance.calendarStyle == style

                Card(
                    onClick = {
                        scope.launch {
                            container.settings.setCalendarStyle(style)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (selected) 2.dp else 0.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                style.titleRu,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Text("✓",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Мини-календарь 3×3
                        MiniCalendarFull(style)
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Готово")
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarFull(style: CalendarStyle) {
    // Мини-календарь 7×5 (полная неделя × 5 недель)
    // Показывает: 1 — праздник, 2-3 — выходные, 4-8 — рабочие с вахтой
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (row in 0 until 5) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val isHoliday = index == 0
                    val isWeekend = col >= 5
                    val isRed = isHoliday || isWeekend
                    val hasCrew = !isRed && (index % 3 == 0)

                    MiniDayCellFull(
                        day = index + 1,
                        isRed = isRed,
                        hasCrew = hasCrew,
                        style = style,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniDayCellFull(
    day: Int,
    isRed: Boolean,
    hasCrew: Boolean,
    style: CalendarStyle,
    modifier: Modifier = Modifier
) {
    val bg: Brush? = when {
        !isRed -> null
        style == CalendarStyle.GRADIENT -> Brush.verticalGradient(
            listOf(ShiftColors.HolidayRed, Color(0xFFF87171))
        )
        style == CalendarStyle.FRAME_BOLD -> Brush.verticalGradient(
            listOf(
                ShiftColors.HolidayRed.copy(alpha = 0.12f),
                ShiftColors.HolidayRed.copy(alpha = 0.12f)
            )
        )
        else -> null
    }

    val borderColor = when {
        !isRed -> Color.Transparent
        style == CalendarStyle.FRAME_BOLD -> ShiftColors.HolidayRed
        style == CalendarStyle.FRAME_DOT -> ShiftColors.HolidayRed
        else -> Color.Transparent
    }

    val borderWidth = when {
        !isRed -> 0.dp
        style == CalendarStyle.FRAME_BOLD -> 2.dp
        style == CalendarStyle.FRAME_DOT -> 1.dp
        else -> 0.dp
    }

    val textColor = when {
        style == CalendarStyle.GRADIENT && isRed -> Color.White
        isRed -> ShiftColors.HolidayRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .then(if (bg != null) Modifier.background(bg) else Modifier)
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isRed || hasCrew) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (style == CalendarStyle.UNDERLINE && isRed) {
                Box(
                    Modifier
                        .height(2.dp)
                        .width(14.dp)
                        .background(ShiftColors.HolidayRed, RoundedCornerShape(1.dp))
                )
            }
            if (hasCrew) {
                Text("★", style = MaterialTheme.typography.labelSmall,
                    color = ShiftColors.WorkBlue)
            }
        }
        if (style == CalendarStyle.FRAME_DOT && isRed) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(ShiftColors.HolidayRed)
            )
        }
    }
}
