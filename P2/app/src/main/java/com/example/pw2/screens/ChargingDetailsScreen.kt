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
fun ChargingDetailsScreen(navController: NavHostController, operatorName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenTitle("Заняття 5: Деталі з аргументом")
        InfoRow("Оператор:", operatorName)
        InfoRow("Тип порту:", "CCS2 Combo (DC)")
        InfoRow("Тариф:", "12.50 грн/кВт·год")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Повернутися")
        }
    }
}