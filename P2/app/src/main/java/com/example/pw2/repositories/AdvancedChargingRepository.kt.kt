package com.example.pw2.repositories

import com.example.pw2.dao.AdvancedChargingDao
import com.example.pw2.data.ChargingPort
import com.example.pw2.data.ChargingSession
import com.example.pw2.data.ChargingStation

class AdvancedChargingRepository(private val dao: AdvancedChargingDao) {
    suspend fun getAllStations() = dao.getAllStations()
    suspend fun getSessionsForStation(id: Int) = dao.getSessionsForStation(id)
    suspend fun getPortsForStation(id: Int) = dao.getPortsForStation(id)
    suspend fun addStationWithData(
        station: ChargingStation,
        ports: List<ChargingPort>,
        sessions: List<ChargingSession>
    ) {
        dao.insertStationWithPortsAndSessions(station, ports, sessions)
    }
}