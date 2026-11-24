package com.example.pw3.pz11

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Архітектурне рішення (OkHttp Interceptor):
 * Перехоплює кожен вихідний HTTP-запит і автоматично додає заголовок
 * "Authorization: Bearer <token>", якщо користувач автентифікований.
 * Це звільняє методи API-інтерфейсу від ручної передачі токена в параметрах.
 */
class AuthInterceptor(private val authDataStore: AuthDataStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { authDataStore.tokenFlow.first() }

        val modifiedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(modifiedRequest)
    }
}