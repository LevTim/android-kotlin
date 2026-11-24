package com.example.pw3.pz9

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.pw3.mock.MockEvBackendInterceptor

/**
 * Віддалене джерело даних для взаємодії з сервером зарядних станцій через Retrofit + Gson.
 * Ізолює мережеві виклики та повертає безпечний об'єкт Kotlin Result<T>.
 */
class RemoteChargingDataSource {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(MockEvBackendInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5156/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ChargingApiService::class.java)

    suspend fun fetchAll(): Result<List<ChargingSession>> = try {
        Result.success(api.getSessions())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun create(session: ChargingSession): Result<ChargingSession> = try {
        val response = api.addSession(session)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Помилка створення сесії: HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun update(session: ChargingSession): Result<ChargingSession> = try {
        val response = api.updateSession(session.id, session)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Помилка оновлення: HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun delete(id: Int): Result<Boolean> = try {
        val response = api.deleteSession(id)
        if (response.isSuccessful) Result.success(true)
        else Result.failure(Exception("Помилка видалення: HTTP ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}