package com.example.tb_33_levun_9.task3.model

data class ChargingPort(
    val id: String,
    val name: String,
    val type: String,        // "AC" або "DC Fast"
    val maxPowerKw: Double,  // Максимальна потужність роз'єму
    val isActive: Boolean    // Активний/зайнятий порт
)