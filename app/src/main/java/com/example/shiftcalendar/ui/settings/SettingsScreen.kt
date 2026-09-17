package com.example.shiftcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.shiftcalendar.R
import com.example.shiftcalendar.data.settings.AnimationSettings
import com.example.shiftcalendar.data.settings.AppearanceSettings
import com.example.shiftcalendar.data.settings.CalendarStyle
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.data.settings.NotificationSettings
import com.example.shiftcalendar.data.settings.ThemeMode
import com.example.shiftcalendar.data.settings.ThemeSettings
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.navigation.Routes
import com.example.shiftcalendar.ui.theme.ShiftColors
import com.example.shiftcalendar.widget.ShiftWidget
import com.example.shiftcalendar.widget.WidgetBg
import com.example.shiftcalendar.widget.WidgetSettings
import kotlinx.coroutines.launch

private const val DONATION_URL = "https://www.donationalerts.com/r/pavel1292"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer, navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val anim by container.settings.animationSettings.collectAsStateWithLifecycle(initialValue = AnimationSettings())
    val notif by container.settings.notificationSettings.collectAsStateWithLifecycle(initialValue = NotificationSettings())
    val hours by container.settings.hoursSettings.collectAsStateWithLifecycle(initialValue = HoursSettings())
    val theme by container.settings.themeSettings.collectAsStateWithLifecycle(initialValue = ThemeSettings())
    val appearance by container.settings.appearanceSettings.collectAsStateWithLifecycle(initialValue = AppearanceSettings())

    var widgetBg by remember { mutableStateOf(WidgetSettings.getBackground(context)) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Календарь")
                Text("Стиль праздников и выходных",
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))

                val styles = CalendarStyle.entries
                for (i in styles.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            CalendarStyleOption(
                                style = styles[i],
                                selected = styles[i] == appearance.calendarStyle,
                                onClick = {
                                    scope.launch {
                                        container.settings.setCalendarStyle(styles[i])
                                    }
                                }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            if (i + 1 < styles.size) {
                                CalendarStyleOption(
                                    style = styles[i + 1],
                                    selected = styles[i + 1] == appearance.calendarStyle,
                                    onClick = {
                                        scope.launch {
                                            container.settings.setCalendarStyle(styles[i + 1])
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { navController.navigate(Routes.CALENDAR_STYLE_TEST) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Palette, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Показать все стили")
                }
            }

            item {
                SectionHeader("Оформление")
                ThemeDropdown(current = theme.themeMode) { mode ->
                    scope.launch { container.settings.setThemeMode(mode) }
                }
                SettingSwitch(
                    title = "Динамические цвета",
                    subtitle = "Material You (Android 12+)",
                    checked = theme.dynamicColor,
                    onChange = { scope.launch { container.settings.setDynamicColor(it) } }
                )
            }

            item {
                SectionHeader("Виджет")
                Text("Фон виджета на рабочем столе",
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                WidgetBg.entries.forEach { bg ->
                    WidgetBgOption(
                        bg = bg,
                        selected = bg == widgetBg,
                        onClick = {
                            widgetBg = bg
                            WidgetSettings.setBackground(context, bg)
                            scope.launch {
                                try { ShiftWidget().updateAll(context) } catch (e: Exception) { }
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                SectionHeader("Анимации")
                SettingSwitch(
                    title = "Все анимации",
                    checked = anim.animationsEnabled,
                    onChange = { scope.launch { container.settings.setAnimationsEnabled(it) } }
                )
                SettingSwitch(
                    title = "Переходы между экранами",
                    checked = anim.screenTransitions,
                    enabled = anim.animationsEnabled,
                    onChange = { scope.launch { container.settings.setScreenTransitions(it) } }
                )
                SettingSwitch(
                    title = "Анимации карточек",
                    checked = anim.cardAnimations,
                    enabled = anim.animationsEnabled,
                    onChange = { scope.launch { container.settings.setCardAnimations(it) } }
                )
            }

            item {
                SectionHeader("Уведомления")
                SettingSwitch(
                    title = "Все уведомления",
                    checked = notif.enabled,
                    onChange = { scope.launch { container.settings.setNotificationsEnabled(it) } }
                )
            }

            item {
                SectionHeader("Учёт часов")
                SettingNumber(
                    title = "Часов в вахтовом дне",
                    value = hours.shiftDayHours,
                    onChange = { scope.launch { container.settings.setShiftDayHours(it) } }
                )
                SettingNumber(
                    title = "Обед (вычитается)",
                    value = hours.lunchHours,
                    onChange = { scope.launch { container.settings.setLunchHours(it) } }
                )
                SettingNumber(
                    title = "Норма часов за год",
                    value = hours.yearlyNorm,
                    onChange = { scope.launch { container.settings.setYearlyNorm(it) } }
                )
            }

            item {
                SectionHeader("Подсказки")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Показать приветствие и подсказки заново.",
                            style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { scope.launch { container.settings.resetOnboarding() } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Сбросить подсказки")
                        }
                    }
                }
            }

            item {
                SectionHeader("О приложении")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Моя вахта",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Версия 1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                SectionHeader("Поддержать проект")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Приложение бесплатное. Если оно помогает тебе — поддержи разработку.",
                            style = MaterialTheme.typography.bodyLarge)

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Outlined.FavoriteBorder, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Поддержать через DonationAlerts")
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Text("  или  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }

                        Spacer(Modifier.height(14.dp))

                        Text("📱 Перевод по СБП (без комиссии)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.qr_donate),
                                contentDescription = "QR-код для перевода",
                                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Наведи камеру банковского приложения и переведи любую сумму",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarStyleOption(
    style: CalendarStyle,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            MiniCalendarPreview(style)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(style.titleRu,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f))
                if (selected) {
                    Text("✓", color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarPreview(style: CalendarStyle) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isHoliday = index == 0
                    val isWeekend = index == 1
                    val isRed = isHoliday || isWeekend
                    val hasCrew = index == 2

                    MiniDayCell(
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
private fun MiniDayCell(
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
            listOf(ShiftColors.HolidayRed.copy(alpha = 0.12f), ShiftColors.HolidayRed.copy(alpha = 0.12f))
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
            .clip(RoundedCornerShape(4.dp))
            .then(if (bg != null) Modifier.background(bg) else Modifier)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isRed || hasCrew) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (style == CalendarStyle.UNDERLINE && isRed) {
                Box(
                    Modifier
                        .height(1.5.dp)
                        .width(12.dp)
                        .background(ShiftColors.HolidayRed, RoundedCornerShape(1.dp))
                )
            }
        }
        if (style == CalendarStyle.FRAME_DOT && isRed) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(ShiftColors.HolidayRed)
            )
        }
    }
}

@Composable
private fun WidgetBgOption(
    bg: WidgetBg,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
                      else Color.Transparent

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(
                        listOf(Color(bg.topColor), Color(bg.bottomColor))
                    )),
                contentAlignment = Alignment.Center
            ) {
                Text("6",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(bg.accentColor))
            }

            Spacer(Modifier.width(12.dp))

            Text(bg.titleRu,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)

            Spacer(Modifier.weight(1f))

            if (selected) {
                Text("✓", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onChange)
    }
}

@Composable
private fun ThemeDropdown(
    current: ThemeMode,
    onChange: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Тема", style = MaterialTheme.typography.bodyLarge)
            Text(themeDescription(current),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(current.titleRu)
            }
            DropdownMenu(expanded, { expanded = false }) {
                ThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(mode.titleRu)
                                Text(themeDescription(mode),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            onChange(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun themeDescription(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "Следует за системой Android"
    ThemeMode.LIGHT -> "Белая, светлая"
    ThemeMode.DARK -> "Тёмная, стандартная"
    ThemeMode.HIGH_CONTRAST -> "Чёрно-белая, для слабовидящих"
    ThemeMode.AURORA -> "Фиолетово-оранжевая"
    ThemeMode.INDUSTRIAL -> "Серо-оранжевая, вахтовая"
}

@Composable
private fun SettingNumber(
    title: String,
    value: Double,
    onChange: (Double) -> Unit
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = text,
            onValueChange = { v ->
                text = v
                v.toDoubleOrNull()?.let(onChange)
            },
            singleLine = true,
            modifier = Modifier.width(110.dp)
        )
    }
}
