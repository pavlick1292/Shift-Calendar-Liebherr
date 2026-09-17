package com.example.shiftcalendar.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.animation.LocalAnimationSettings
import com.example.shiftcalendar.ui.calendar.CalendarScreen
import com.example.shiftcalendar.ui.crews.CrewDetailScreen
import com.example.shiftcalendar.ui.crews.CrewsScreen
import com.example.shiftcalendar.ui.hours.HoursScreen
import com.example.shiftcalendar.ui.people.PeopleScreen
import com.example.shiftcalendar.ui.people.PersonDetailScreen
import com.example.shiftcalendar.ui.settings.SettingsScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomNavItem(Routes.CALENDAR, "Календарь", Icons.Outlined.CalendarMonth),
    BottomNavItem(Routes.CREWS, "Составы", Icons.Outlined.Groups),
    BottomNavItem(Routes.PEOPLE, "Люди", Icons.Outlined.Person),
    BottomNavItem(Routes.HOURS, "Часы", Icons.Outlined.Schedule),
    BottomNavItem(Routes.SETTINGS, "Настройки", Icons.Outlined.Settings)
)

@Composable
fun AppRoot(
    container: AppContainer,
    initialDeepLink: String? = null
) {
    val navController = rememberNavController()
    val anims = LocalAnimationSettings.current

    LaunchedEffect(initialDeepLink) {
        val link = initialDeepLink ?: return@LaunchedEffect
        val uri = Uri.parse(link)
        if (uri.scheme == "shiftcalendar") {
            val crewId = uri.lastPathSegment?.toLongOrNull()
            if (crewId != null && crewId > 0) {
                navController.navigate(Routes.crewDetail(crewId)) { launchSingleTop = true }
            }
        }
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in bottomItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CALENDAR,
            modifier = Modifier.padding(padding),
            enterTransition = {
                if (anims.screenTransitions)
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) +
                        androidx.compose.animation.slideInHorizontally(initialOffsetX = { it / 10 })
                else androidx.compose.animation.EnterTransition.None
            },
            exitTransition = {
                if (anims.screenTransitions)
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                else androidx.compose.animation.ExitTransition.None
            }
        ) {
            composable(Routes.CALENDAR) { CalendarScreen(container) }
            composable(Routes.CREWS) { CrewsScreen(container, navController) }
            composable(Routes.PEOPLE) { PeopleScreen(container, navController) }
            composable(Routes.HOURS) { HoursScreen(container, navController) }
            composable(Routes.SETTINGS) { SettingsScreen(container) }
            composable(Routes.CREW_DETAIL) { backStackEntry ->
                val crewId = backStackEntry.arguments?.getString("crewId")?.toLongOrNull() ?: 0L
                CrewDetailScreen(container, crewId, navController)
            }
            composable(Routes.PERSON_DETAIL) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull() ?: 0L
                PersonDetailScreen(container, personId, navController)
            }
        }
    }
}
