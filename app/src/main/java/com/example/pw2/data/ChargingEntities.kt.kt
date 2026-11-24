package com.example.pw2.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Таблиця зарядних станцій електромобілів */
@Entity(tableName = "charging_station_table")
data class ChargingStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String
)

/** Таблиця сесій зарядки електромобілів (1 to N зі станцією) */
@Entity(
    tableName = "charging_session_table",
    foreignKeys = [
        ForeignKey(
            entity = ChargingStation::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stationId")]
)
data class ChargingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val energyKwh: Double,
    val costGrn: Double,
    val stationId: Int
)

/** Таблиця портів/роз'ємів (CCS2, Type 2, CHAdeMO) (1 to N зі станцією) */
@Entity(
    tableName = "charging_port_table",
    foreignKeys = [
        ForeignKey(
            entity = ChargingStation::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stationId")]
)
data class ChargingPort(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val portType: String,
    val maxPowerKw: Double,
    val stationId: Int
)