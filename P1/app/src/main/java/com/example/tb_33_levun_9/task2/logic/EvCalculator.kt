package com.example.tb_33_levun_9.task2.logic

import com.example.tb_33_levun_9.task2.model.EvCalculationResult
import com.example.tb_33_levun_9.task2.model.EvInputParams

// Окремий клас бізнес-логіки (відокремлений від UI)
object EvCalculator {

    fun calculateCharging(params: EvInputParams): EvCalculationResult {
        if (params.batteryCapacityKwh <= 0 || params.chargerPowerKw <= 0) {
            return EvCalculationResult(0.0, 0.0, emptyList())
        }

        val percentageNeeded = (params.targetPercentage - params.currentPercentage).coerceAtLeast(0.0)
        val energyNeeded = params.batteryCapacityKwh * (percentageNeeded / 100.0)
        val timeHours = energyNeeded / params.chargerPowerKw

        // Симуляція динаміки накопичення енергії по годинах для графіка
        val steps = 5
        val hourly = List(steps + 1) { step ->
            (energyNeeded * (step.toDouble() / steps))
        }

        return EvCalculationResult(
            energyNeededKwh = energyNeeded,
            chargingTimeHours = timeHours,
            hourlyProgress = hourly
        )
    }
}