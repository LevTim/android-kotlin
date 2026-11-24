package com.example.pw2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.pw2.data.ChargingPort
import com.example.pw2.data.ChargingSession
import com.example.pw2.data.ChargingStation

@Dao
interface AdvancedChargingDao {
    @Insert
    suspend fun insertStation(station: ChargingStation): Long

    @Query("SELECT * FROM charging_station_table ORDER BY name ASC")
    suspend fun getAllStations(): List<ChargingStation>

    @Insert
    suspend fun insertSession(session: ChargingSession)

    @Query("SELECT * FROM charging_session_table WHERE stationId = :stationId")
    suspend fun getSessionsForStation(stationId: Int): List<ChargingSession>

    @Insert
    suspend fun insertPort(port: ChargingPort)

    @Query("SELECT * FROM charging_port_table WHERE stationId = :stationId")
    suspend fun getPortsForStation(stationId: Int): List<ChargingPort>

    /** Атомарна транзакція для додавання зарядної станції разом із портами та сесіями */
    @Transaction
    suspend fun insertStationWithPortsAndSessions(
        station: ChargingStation,
        ports: List<ChargingPort>,
        sessions: List<ChargingSession>
    ) {
        val stationId = insertStation(station).toInt()
        ports.forEach { insertPort(it.copy(stationId = stationId)) }
        sessions.forEach { insertSession(it.copy(stationId = stationId)) }
    }
}