package com.example.pw3.pz12

import retrofit2.http.GET
import retrofit2.http.Path

interface FaultTolerantApiService {

    @GET("pz12/telemetry/today")
    suspend fun getLiveTelemetry(): ChargerTelemetryDto

    // Ендпоінт, який імітує тимчасові збої 503 на перших двох спробах і успіх на третій
    @GET("pz12/telemetry/flaky")
    suspend fun getFlakyTelemetry(): ChargerTelemetryDto

    // Ендпоінт для симуляції довільного коду помилки (400, 404, 500 тощо)
    @GET("pz12/telemetry/error/{code}")
    suspend fun simulateError(@Path("code") code: Int): ErrorApiResponse
}