package com.example.shiftcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
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
import com.example.shiftcalendar.R
import com.example.shiftcalendar.data.settings.AnimationSettings
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.data.settings.NotificationSettings
import com.example.shiftcalendar.data.settings.ThemeMode
import com.example.shiftcalendar.data.settings.ThemeSettings
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.widget.ShiftWidget
import com.example.shiftcalendar.widget.WidgetBg
import com.example.shiftcalendar.widget.WidgetSettings
import kotlinx.coroutines.launch

private const val DONATION_URL = "https://www.donationalerts.com/r/pavel1292"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val anim by container.settings.animationSettings.collectAsStateWithLifecycle(initialValue = AnimationSettings())
    val notif by container.settings.notificationSettings.collectAsStateWithLifecycle(initialValue = NotificationSettings())
    val hours by container.settings.hoursSettings.collectAsStateWithLifecycle(initialValue = HoursSettings())
    val theme by container.settings.themeSettings.collectAsStateWithLifecycle(initialValue = ThemeSettings())

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
                Text(
                    "Фон виджета на рабочем столе",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                WidgetBg.entries.forEach { bg ->
                    WidgetBgOption(
                        bg = bg,
                        selected = bg == widgetBg,
                        onClick = {
                            widgetBg = bg
                            WidgetSettings.setBackground(context, bg)
                            scope.launch {
                                try {
                                    ShiftWidget().updateAll(context)
                                } catch (e: Exception) { }
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
                SettingSwitch(
                    title = "Пульсация и свечение",
                    checked = anim.pulsingEffects,
                    enabled = anim.animationsEnabled,
                    onChange = { scope.launch { container.settings.setPulsingEffects(it) } }
                )
            }

            item {
                SectionHeader("Уведомления")
                SettingSwitch(
                    title = "Все уведомления",
                    checked = notif.enabled,
                    onChange = { scope.launch { container.settings.setNotificationsEnabled(it) } }
                )
                SettingSwitch(
                    title = "Перед началом вахты",
                    subtitle = "за ${notif.shiftStartDaysBefore} дн.",
                    checked = true,
                    enabled = notif.enabled,
                    onChange = { }
                )
                SettingSwitch(
                    title = "Перед концом вахты",
                    subtitle = "за ${notif.shiftEndDaysBefore} дн.",
                    checked = true,
                    enabled = notif.enabled,
                    onChange = { }
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
                            onClick = {
                                scope.launch { container.settings.resetOnboarding() }
                            },
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
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Text(
                                "  или  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "📱 Перевод по СБП (без комиссии)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.qr_donate),
                                contentDescription = "QR-код для перевода",
                                modifier = Modifier.size(180.dp)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Наведи камеру банковского приложения и переведи любую сумму",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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
                    .size(48.dp)
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
                Text("✓", fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary)
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
