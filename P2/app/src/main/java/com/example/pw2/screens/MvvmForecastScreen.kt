package com.example.pw2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pw2.components.ScreenTitle
import com.example.pw2.viewmodels.AdvancedViewModel
import com.example.pw2.components.*

@Composable
fun MvvmForecastScreen(navController: NavHostController, viewModel: AdvancedViewModel = viewModel()) {
    val chargingPower by viewModel.chargingPower.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val status by viewModel.status.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTitle("Монітор зарядки EV (MVVM)")

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("З'єднання з контролером станції...")
        } else {
            Text("Поточна потужність: $chargingPower кВт", style = MaterialTheme.typography.titleMedium)
            Text("Рівень батареї авто: $batteryLevel%", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Статус: $status", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.startChargingSession() }, enabled = !isLoading) {
            Text("Запустити тестову сесію")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Назад")
        }
    }
}