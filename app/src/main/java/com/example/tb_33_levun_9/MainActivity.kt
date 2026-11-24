package com.example.tb_33_levun_9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tb_33_levun_9.composables.EvStationLevel3
import com.example.tb_33_levun_9.task2.ui.EvCalculatorScreen
import com.example.tb_33_levun_9.task3.ui.EvDashboardScreen
import com.example.tb_33_levun_9.task4.ui.AuthAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var selectedTask by remember { mutableStateOf(1) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        // Панель перемикання між 4 завданнями для перевірки
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { selectedTask = 1 }, enabled = selectedTask != 1) { Text("Завд. 1") }
                            Button(onClick = { selectedTask = 2 }, enabled = selectedTask != 2) { Text("Завд. 2") }
                            Button(onClick = { selectedTask = 3 }, enabled = selectedTask != 3) { Text("Завд. 3") }
                            Button(onClick = { selectedTask = 4 }, enabled = selectedTask != 4) { Text("Завд. 4") }
                        }

                        Divider()

                        // Відображення вибраного завдання
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (selectedTask) {
                                1 -> EvStationLevel3()
                                2 -> EvCalculatorScreen()
                                3 -> EvDashboardScreen()
                                4 -> AuthAppScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}