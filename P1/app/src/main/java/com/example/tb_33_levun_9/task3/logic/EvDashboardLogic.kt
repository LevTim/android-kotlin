package com.example.tb_33_levun_9.task3.logic

import com.example.tb_33_levun_9.task3.model.ChargingPort

object EvDashboardLogic {

    fun togglePort(ports: List<ChargingPort>, portId: String): List<ChargingPort> {
        return ports.map { port ->
            if (port.id == portId) port.copy(isActive = !port.isActive) else port
        }
    }

    fun calculateTotalActivePower(ports: List<ChargingPort>): Double {
        return ports.filter { it.isActive }.sumOf { it.maxPowerKw }
    }
}