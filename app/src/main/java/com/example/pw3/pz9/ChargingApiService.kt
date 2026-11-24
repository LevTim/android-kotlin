package com.example.pw3.pz9

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Інтерфейс REST API для роботи з сесіями зарядних станцій через Retrofit.
 */
interface ChargingApiService {

    @GET("pz9/sessions")
    suspend fun getSessions(): List<ChargingSession>

    @POST("pz9/sessions")
    suspend fun addSession(@Body session: ChargingSession): Response<ChargingSession>

    @PUT("pz9/sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Int,
        @Body session: ChargingSession
    ): Response<ChargingSession>

    @DELETE("pz9/sessions/{id}")
    suspend fun deleteSession(@Path("id") id: Int): Response<Unit>
}