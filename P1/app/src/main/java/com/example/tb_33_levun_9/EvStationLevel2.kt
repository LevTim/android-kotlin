package com.example.tb_33_levun_9.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun EvStationLevel2() {
    var chargingPower by remember { mutableStateOf(50) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDE7))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Зарядна станція EV",
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Потужність зарядки: $chargingPower кВт",
            fontSize = 20.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { chargingPower = Random.nextInt(10, 180) }) {
            Text("Оновити потужність")
        }
    }
}