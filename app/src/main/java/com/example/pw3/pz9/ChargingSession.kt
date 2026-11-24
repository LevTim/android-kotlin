package com.example.pw3.pz9

/**
 * Модель даних сесії заряджання електромобіля.
 * Використовується як уніфікована сутність для локального кешу, віддаленого API та UI.
 */
data class ChargingSession(
    val id: Int,
    val stationName: String,
    val connectorType: String,      // Наприклад: CCS2, Type 2, CHAdeMO
    val powerKw: Double,            // Потужність заряджання у кВт
    val energyDeliveredKwh: Double, // Передана енергія у кВт·год
    val status: String,             // "Активна" або "Завершено"
    val timestamp: String
)