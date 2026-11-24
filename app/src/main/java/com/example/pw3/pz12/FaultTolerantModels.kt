package com.example.pw3.pz12

/**
 * Модель телеметрії зарядної станції в реальному часі.
 */
data class ChargerTelemetryDto(
    val stationCode: String,
    val timestamp: String,
    val voltageV: Double,
    val currentA: Double,
    val powerKw: Double,
    val connectorTempC: Double,
    val status: String
)

data class ErrorApiResponse(
    val msg: String
)