package com.example.tb_33_levun_9.task3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tb_33_levun_9.task3.logic.EvDashboardLogic
import com.example.tb_33_levun_9.task3.model.ChargingPort

@Composable
fun EvDashboardScreen() {
    var ports by remember {
        mutableStateOf(
            listOf(
                ChargingPort("1", "Порт 1 (Type 2)", "AC", 22.0, true),
                ChargingPort("2", "Порт 2 (CCS2)", "DC Fast", 150.0, false),
                ChargingPort("3", "Порт 3 (CHAdeMO)", "DC Fast", 50.0, true),
                ChargingPort("4", "Порт 4 (Type 2)", "AC", 22.0, false)
            )
        )
    }

    var powerLimitInput by remember { mutableStateOf("200") }
    val totalPower = EvDashboardLogic.calculateTotalActivePower(ports)
    val powerLimit = powerLimitInput.toDoubleOrNull() ?: 0.0
    val isOverLimit = totalPower > powerLimit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Дашборд портів зарядної станції",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Поточне навантаження станції: $totalPower кВт", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = powerLimitInput,
            onValueChange = { powerLimitInput = it },
            label = { Text("Ліміт потужності підстанції (кВт)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isOverLimit) {
            Text(
                text = "УВАГА: Перевищено ліміт потужності підстанції!",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Адаптивна сітка з картками портів
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ports, key = { it.id }) { port ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (port.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = port.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Тип: ${port.type}", fontSize = 12.sp)
                        Text(text = "${port.maxPowerKw} кВт", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { ports = EvDashboardLogic.togglePort(ports, port.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (port.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (port.isActive) "Вимкнути" else "Увімкнути", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}