package com.example.pw3.pz9

/**
 * Модель агрегованих бізнес-показників зарядної мережі,
 * які обчислюються безпосередньо в шарі Repository.
 */
data class ChargingStatsSummary(
    val totalSessions: Int = 0,
    val activeSessionsCount: Int = 0,
    val totalEnergyKwh: Double = 0.0,
    val averagePowerKw: Double = 0.0,
    val estimatedRevenueUah: Double = 0.0
)