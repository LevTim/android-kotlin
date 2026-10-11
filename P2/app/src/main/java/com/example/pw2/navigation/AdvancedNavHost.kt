package com.example.pw2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pw2.screens.*

@Composable
fun AdvancedNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(navController)
        }
        composable(Screen.UserInfo.route) {
            UserInfoScreen(navController)
        }
        composable(
            route = "${Screen.ChargingDetails.route}/{operatorName}",
            arguments = listOf(navArgument("operatorName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("operatorName") ?: "Оператор"
            ChargingDetailsScreen(navController, name)
        }
        composable(Screen.RegistrationForm.route) {
            RegistrationFormScreen(navController)
        }
        composable(Screen.MvvmForecast.route) {
            MvvmForecastScreen(navController)
        }
        composable(Screen.RoomDatabase.route) {
            RoomDatabaseScreen(navController)
        }
    }
}