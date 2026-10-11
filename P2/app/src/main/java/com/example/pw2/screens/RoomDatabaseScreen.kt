package com.example.pw2.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.room.Room
import com.example.pw2.components.ScreenTitle
import com.example.pw2.database.AdvancedAppDatabase
import com.example.pw2.repositories.AdvancedChargingRepository
import com.example.pw2.viewmodels.AdvancedChargingViewModel
import com.example.pw2.components.*

@Composable
fun RoomDatabaseScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(context, AdvancedAppDatabase::class.java, "advanced_ev_charging_db").build()
    }
    val repository = remember { AdvancedChargingRepository(db.advancedDao()) }
    val viewModel: AdvancedChargingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AdvancedChargingViewModel(repository) as T
        }
    })

    var stationName by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var portTypesInput by remember { mutableStateOf("") }
    var sessionEnergiesInput by remember { mutableStateOf("") }

    val stations by viewModel.stations.collectAsState()
    val ports by viewModel.ports.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val selectedStationId by viewModel.selectedStationId.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStations() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTitle("База даних станцій EV (Room)")

        OutlinedTextField(value = stationName, onValueChange = { stationName = it }, label = { Text("Назва станції") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = locationInput, onValueChange = { locationInput = it }, label = { Text("Локація / Адреса") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = portTypesInput, onValueChange = { portTypesInput = it }, label = { Text("Типи портів (CCS2, Type2)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sessionEnergiesInput, onValueChange = { sessionEnergiesInput = it }, label = { Text("Сесії зарядки, кВт·год (через кому)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val portsList = portTypesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val energiesList = sessionEnergiesInput.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                viewModel.addStationWithData(stationName, locationInput, portsList, energiesList)
                stationName = ""; locationInput = ""; portTypesInput = ""; sessionEnergiesInput = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зберегти станцію у БД")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Список заповнених станцій EV:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.height(100.dp)) {
            items(stations) { station ->
                Text(
                    text = "• ${station.name} (${station.location})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectStation(station.id) }
                        .padding(4.dp)
                )
            }
        }

        selectedStationId?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Порти обраної станції:", style = MaterialTheme.typography.titleSmall)
            ports.forEach { Text("  - Тип: ${it.portType} (макс. ${it.maxPowerKw} кВт)") }
            Text("Історія сесій зарядки:", style = MaterialTheme.typography.titleSmall)
            sessions.forEach { Text("  - ${it.energyKwh} кВт·год | Вартість: ${it.costGrn} грн") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Назад")
        }
    }
}