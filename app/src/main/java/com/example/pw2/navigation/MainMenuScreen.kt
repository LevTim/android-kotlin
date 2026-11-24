package com.example.pw2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pw2.components.MenuButton
import com.example.pw2.components.ScreenTitle
import com.example.pw2.navigation.Screen

@Composable
fun MainMenuScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("Система керування EV Hub")

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton("Профіль оператора") {
            navController.navigate(Screen.UserInfo.route)
        }

        MenuButton("Параметри підключення") {
            navController.navigate("${Screen.ChargingDetails.route}/Левун Т.О.")
        }

        MenuButton("Реєстрація станції") {
            navController.navigate(Screen.RegistrationForm.route)
        }

        MenuButton("Моніторинг процесів") {
            navController.navigate(Screen.MvvmForecast.route)
        }

        MenuButton("База даних локацій") {
            navController.navigate(Screen.RoomDatabase.route)
        }
    }
}