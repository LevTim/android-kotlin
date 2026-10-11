package com.example.tb_33_levun_9.task2.model

// Модель результатів розрахунку
data class EvCalculationResult(
    val energyNeededKwh: Double,  // Потрібна енергія (кВт·год)
    val chargingTimeHours: Double, // Час зарядки (в годинах)
    val hourlyProgress: List<Double> // Погодинний прогрес заряду для графіка
)