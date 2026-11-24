package com.example.pw3.pz10

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EvApiService {

    @GET("pz10/stations")
    suspend fun getStations(): List<ChargingStationDto>

    @GET("pz10/stations/{id}/sessions")
    suspend fun getStationSessions(@Path("id") stationId: Int): List<ChargingRecordDto>

    @GET("pz10/stations/{id}/stats")
    suspend fun getStationStats(@Path("id") stationId: Int): StationTelemetryStatsDto

    @POST("pz10/stations/{id}/simulate/{count}")
    suspend fun simulateSessions(
        @Path("id") stationId: Int,
        @Path("count") count: Int
    ): List<ChargingRecordDto>
}