package com.example.pw3.pz11

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Інтерфейс API автентифікації та захищених ресурсів мережі ЕЗС.
 */
interface AuthApiService {

    @POST("pz11/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Захищений ресурс, доступний для будь-якої авторизованої ролі (USER або ADMIN)
    @GET("pz11/protected")
    suspend fun getProtectedDriverProfile(): ProtectedDriverProfile

    // Адміністративний ресурс, доступний виключно для ролі ADMIN
    @GET("pz11/admin-only")
    suspend fun getAdminStationsControl(): List<AdminStationControlDto>
}