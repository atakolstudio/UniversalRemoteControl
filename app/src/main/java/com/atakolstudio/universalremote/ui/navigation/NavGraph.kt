package com.atakolstudio.universalremote.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import com.atakolstudio.universalremote.ui.screens.adddevice.AddDeviceScreen
import com.atakolstudio.universalremote.ui.screens.dashboard.DashboardScreen
import com.atakolstudio.universalremote.ui.screens.macro.MacroScreen
import com.atakolstudio.universalremote.ui.screens.remote.RemoteScreen
import com.atakolstudio.universalremote.ui.screens.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_DEVICE = "add_device"
    const val REMOTE = "remote/{deviceId}"
    const val MACROS = "macros"
    const val SETTINGS = "settings"

    fun remote(deviceId: Long) = "remote/$deviceId"
}

@Composable
fun RemoteAppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenDevice = { id -> navController.navigate(Routes.remote(id)) },
                onAddDevice = { navController.navigate(Routes.ADD_DEVICE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenMacros = { navController.navigate(Routes.MACROS) }
            )
        }
        composable(Routes.ADD_DEVICE) {
            AddDeviceScreen(
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.popBackStack()
                    navController.navigate(Routes.remote(id))
                }
            )
        }
        composable(
            route = Routes.REMOTE,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType })
        ) {
            RemoteScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MACROS) {
            MacroScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
