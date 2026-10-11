package com.example.pw2.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pw2.data.ChargingPort
import com.example.pw2.data.ChargingSession
import com.example.pw2.data.ChargingStation
import com.example.pw2.repositories.AdvancedChargingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdvancedChargingViewModel(private val repository: AdvancedChargingRepository) : ViewModel() {
    private val _stations = MutableStateFlow<List<ChargingStation>>(emptyList())
    val stations: StateFlow<List<ChargingStation>> = _stations.asStateFlow()

    private val _selectedStationId = MutableStateFlow<Int?>(null)
    val selectedStationId: StateFlow<Int?> = _selectedStationId.asStateFlow()

    private val _ports = MutableStateFlow<List<ChargingPort>>(emptyList())
    val ports: StateFlow<List<ChargingPort>> = _ports.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChargingSession>>(emptyList())
    val sessions: StateFlow<List<ChargingSession>> = _sessions.asStateFlow()

    fun loadStations() {
        viewModelScope.launch { _stations.value = repository.getAllStations() }
    }

    fun selectStation(id: Int) {
        _selectedStationId.value = id
        viewModelScope.launch {
            _ports.value = repository.getPortsForStation(id)
            _sessions.value = repository.getSessionsForStation(id)
        }
    }

    fun addStationWithData(
        name: String,
        location: String,
        portTypes: List<String>,
        sessionEnergies: List<Double>
    ) {
        viewModelScope.launch {
            val station = ChargingStation(name = name, location = location)
            val ports = portTypes.map { ChargingPort(portType = it, maxPowerKw = 50.0, stationId = 0) }
            val sessions = sessionEnergies.map { ChargingSession(energyKwh = it, costGrn = it * 8.5, stationId = 0) }

            repository.addStationWithData(station, ports, sessions)
            loadStations()
        }
    }
}