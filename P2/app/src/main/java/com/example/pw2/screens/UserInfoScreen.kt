package com.example.pw2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pw2.components.InfoRow
import com.example.pw2.components.ScreenTitle

@Composable
fun UserInfoScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenTitle("Заняття 5: Профіль оператора")
        InfoRow("Оператор локації:", "Левун Т.О.")
        InfoRow("Хаб зарядки:", "EV Fast Charger #9")
        InfoRow("Тип підключення:", "3-Phase 400V")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Назад")
        }
    }
}