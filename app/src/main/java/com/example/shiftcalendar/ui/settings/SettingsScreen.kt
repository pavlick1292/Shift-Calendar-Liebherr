package com.example.shiftcalendar.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shiftcalendar.data.settings.AnimationSettings
import com.example.shiftcalendar.data.settings.HoursSettings
import com.example.shiftcalendar.data.settings.NotificationSettings
import com.example.shiftcalendar.data.settings.ThemeMode
import com.example.shiftcalendar.data.settings.ThemeSettings
import com.example.shiftcalendar.di.AppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer) {
    val scope = rememberCoroutineScope()
    val anim by container.settings.animationSettings.collectAsStateWithLifecycle(initialValue = AnimationSettings())
    val notif by container.settings.notificationSettings.collectAsStateWithLifecycle(initialValue = NotificationSettings())
    val hours by container.settings.hoursSettings.collectAsStateWithLifecycle(initialValue = HoursSettings())
    val theme by container.settings.themeSettings.collectAsStateWithLifecycle(initialValue = ThemeSettings())

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
                SettingDropdown(
                    title = "Тема",
                    value = when (theme.themeMode) {
                        ThemeMode.SYSTEM -> "Системная"
                        ThemeMode.LIGHT -> "Светлая"
                        ThemeMode.DARK -> "Тёмная"
                    },
                    options = listOf("Системная", "Светлая", "Тёмная")
                ) { idx ->
                    scope.launch {
                        container.settings.setThemeMode(
                            when (idx) {
                                0 -> ThemeMode.SYSTEM
                                1 -> ThemeMode.LIGHT
                                else -> ThemeMode.DARK
                            }
                        )
                    }
                }
                SettingSwitch(
                    title = "Динамические цвета",
                    subtitle = "Material You (Android 12+)",
                    checked = theme.dynamicColor,
                    onChange = { scope.launch { container.settings.setDynamicColor(it) } }
                )
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
                SettingSwitch(
                    title = "День в дороге",
                    subtitle = "за ${notif.roadDaysBefore} д.",
                    checked = notif.roadEnabled,
                    enabled = notif.enabled,
                    onChange = { scope.launch { container.settings.setRoadEnabled(it) } }
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
                    title = "Часов в дороге",
                    value = hours.roadDayHours,
                    onChange = { scope.launch { container.settings.setRoadDayHours(it) } }
                )
                SettingNumber(
                    title = "Норма часов за год",
                    value = hours.yearlyNorm,
                    onChange = { scope.launch { container.settings.setYearlyNorm(it) } }
                )
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
private fun SettingDropdown(
    title: String,
    value: String,
    options: List<String>,
    onChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Box {
            TextButton(onClick = { expanded = true }) { Text(value) }
            DropdownMenu(expanded, { expanded = false }) {
                options.forEachIndexed { i, opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onChange(i); expanded = false }
                    )
                }
            }
        }
    }
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
