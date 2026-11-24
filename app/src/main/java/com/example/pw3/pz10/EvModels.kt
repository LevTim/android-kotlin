package com.example.pw3.pz10

import com.google.gson.annotations.SerializedName

data class ChargingStationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("operator") val operator: String,
    @SerializedName("maxPowerKw") val maxPowerKw: Double,
    @SerializedName("location") val location: LocationInfo,
    @SerializedName("ports") val ports: List<ConnectorPortDto>
)

data class LocationInfo(
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class ConnectorPortDto(
    @SerializedName("portCode") val portCode: String,
    @SerializedName("connectorType") val connectorType: String,
    @SerializedName("powerKw") val powerKw: Int,
    @SerializedName("state") val state: String
)

data class ChargingRecordDto(
    @SerializedName("id") val id: Int,
    @SerializedName("stationId") val stationId: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("vehicleModel") val vehicleModel: String,
    @SerializedName("energyKwh") val energyKwh: Double,
    @SerializedName("avgPowerKw") val avgPowerKw: Double,
    @SerializedName("connectorTempC") val connectorTempC: Double
)

data class StationTelemetryStatsDto(
    @SerializedName("stationId") val stationId: Int,
    @SerializedName("averagePowerKw") val averagePowerKw: Double,
    @SerializedName("totalEnergyDeliveredKwh") val totalEnergyDeliveredKwh: Double,
    @SerializedName("averageConnectorTempC") val averageConnectorTempC: Double,
    @SerializedName("utilizationPercent") val utilizationPercent: Double
)