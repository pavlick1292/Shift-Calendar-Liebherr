package com.example.shiftcalendar.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.glassSurface(): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            )
        ),
        shape = MaterialTheme.shapes.large
    )
    .border(
        width = 0.5.dp,
        color = Color.White.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.large
    )

fun headerGradient(): Brush = Brush.horizontalGradient(
    colors = listOf(ShiftColors.Aurora, ShiftColors.Sunrise)
)

fun dayGradient(colors: List<Color>): Brush = Brush.linearGradient(colors)
