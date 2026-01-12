package org.khlug.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.khlug.data.preferences.SettingsPreferences
import org.khlug.data.repository.BatteryRepository
import org.khlug.ui.home.HomeScreen
import org.khlug.ui.home.HomeViewModel
import org.khlug.ui.settings.SettingsScreen
import org.khlug.ui.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val settingsPreferences = remember { SettingsPreferences(context) }
    val repository = remember { BatteryRepository(context, settingsPreferences) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel = remember {
                HomeViewModel(repository, settingsPreferences)
            }
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel = remember {
                SettingsViewModel(settingsPreferences)
            }
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
