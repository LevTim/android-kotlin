package com.example.tb_33_levun_9.task2.model

// Модель вхідних параметрів для розрахунку зарядки EV
data class EvInputParams(
    val batteryCapacityKwh: Double, // Ємність акумулятора (кВт·год)
    val currentPercentage: Double,  // Поточний рівень заряду (%)
    val targetPercentage: Double,   // Бажаний рівень заряду (%)
    val chargerPowerKw: Double      // Потужність зарядної станції (кВт)
)