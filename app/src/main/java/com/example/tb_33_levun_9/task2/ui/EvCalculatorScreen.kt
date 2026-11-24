package com.example.tb_33_levun_9.task2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tb_33_levun_9.task2.logic.EvCalculator
import com.example.tb_33_levun_9.task2.model.EvCalculationResult
import com.example.tb_33_levun_9.task2.model.EvInputParams

@Composable
fun EvCalculatorScreen() {
    var capacityInput by remember { mutableStateOf("60") }
    var currentInput by remember { mutableStateOf("20") }
    var targetInput by remember { mutableStateOf("80") }
    var powerInput by remember { mutableStateOf("50") }

    var result by remember { mutableStateOf<EvCalculationResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Розрахунок сесії зарядки EV",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = capacityInput,
            onValueChange = { capacityInput = it },
            label = { Text("Ємність батареї (кВт·год)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentInput,
            onValueChange = { currentInput = it },
            label = { Text("Поточний заряд (%)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Бажаний заряд (%)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = powerInput,
            onValueChange = { powerInput = it },
            label = { Text("Потужність станції (кВт)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cap = capacityInput.toDoubleOrNull() ?: 0.0
                val curr = currentInput.toDoubleOrNull() ?: 0.0
                val targ = targetInput.toDoubleOrNull() ?: 0.0
                val pow = powerInput.toDoubleOrNull() ?: 0.0

                val params = EvInputParams(cap, curr, targ, pow)
                result = EvCalculator.calculateCharging(params)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Розрахувати час та енергію")
        }

        result?.let { res ->
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Потрібно енергії: %.2f кВт·год".format(res.energyNeededKwh), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Очікуваний час: %.2f год (%.0f хв)".format(res.chargingTimeHours, res.chargingTimeHours * 60), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Графік поповнення енергії:", fontSize = 14.sp)

                    // Візуалізація графіка за допомогою Jetpack Compose
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxVal = res.hourlyProgress.maxOrNull() ?: 1.0
                        res.hourlyProgress.forEach { valProgress ->
                            val heightRatio = if (maxVal > 0) (valProgress / maxVal).toFloat() else 0f
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight(heightRatio.coerceAtLeast(0.05f))
                                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}