package com.timer.reminder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timer.reminder.ui.home.HomeScreen
import com.timer.reminder.ui.reminder.ReminderScreen
import com.timer.reminder.ui.tomato.TomatoScreen
import com.timer.reminder.ui.task.TaskScreen
import com.timer.reminder.ui.about.AboutScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Filled.Home)
    data object Reminder : Screen("reminder", "提醒", Icons.Filled.Notifications)
    data object Tomato : Screen("tomato", "番茄钟", Icons.Filled.Timer)
    data object Task : Screen("task", "任务", Icons.Filled.CheckCircle)
    data object About : Screen("about", "关于", Icons.Filled.Info)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Reminder,
    Screen.Tomato,
    Screen.Task
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Reminder.route) { ReminderScreen() }
            composable(Screen.Tomato.route) { TomatoScreen() }
            composable(Screen.Task.route) { TaskScreen() }
            composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
