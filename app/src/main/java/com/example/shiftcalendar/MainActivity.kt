package com.example.shiftcalendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shiftcalendar.data.settings.AnimationSettings
import com.example.shiftcalendar.data.settings.ThemeSettings
import com.example.shiftcalendar.ui.animation.LocalAnimationSettings
import com.example.shiftcalendar.ui.navigation.AppRoot
import com.example.shiftcalendar.ui.permissions.RequestNotificationPermission
import com.example.shiftcalendar.ui.theme.ShiftCalendarTheme

class MainActivity : ComponentActivity() {

    private val deepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        deepLink.value = intent?.data?.toString()

        val container = (application as ShiftCalendarApp).container

        setContent {
            val theme by container.settings.themeSettings
                .collectAsStateWithLifecycle(initialValue = ThemeSettings())
            val anims by container.settings.animationSettings
                .collectAsStateWithLifecycle(initialValue = AnimationSettings())

            CompositionLocalProvider(LocalAnimationSettings provides anims) {
                ShiftCalendarTheme(
                    themeMode = theme.themeMode,
                    dynamicColor = theme.dynamicColor
                ) {
                    RequestNotificationPermission(enabled = true)
                    AppRoot(
                        container = container,
                        initialDeepLink = deepLink.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLink.value = intent.data?.toString()
    }
}

