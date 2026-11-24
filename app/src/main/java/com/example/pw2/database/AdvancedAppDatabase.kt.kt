package com.example.pw2.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pw2.dao.AdvancedChargingDao
import com.example.pw2.data.ChargingPort
import com.example.pw2.data.ChargingSession
import com.example.pw2.data.ChargingStation

@Database(
    entities = [ChargingStation::class, ChargingSession::class, ChargingPort::class],
    version = 1,
    exportSchema = false
)
abstract class AdvancedAppDatabase : RoomDatabase() {
    abstract fun advancedDao(): AdvancedChargingDao
}