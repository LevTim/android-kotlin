package com.example.pw3.pz11

import android.util.Base64
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.nio.charset.StandardCharsets

data class LoginRequest(
    @SerializedName("Username") val username: String,
    @SerializedName("Password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("role") val role: String,
    @SerializedName("username") val username: String
)

data class ProtectedDriverProfile(
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String,
    @SerializedName("rfidCardNumber") val rfidCardNumber: String,
    @SerializedName("balanceUah") val balanceUah: Double,
    @SerializedName("activeTariff") val activeTariff: String
)

data class AdminStationControlDto(
    @SerializedName("id") val id: Int,
    @SerializedName("stationCode") val stationCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalCapacityKw") val totalCapacityKw: Double,
    @SerializedName("uptimePercent") val uptimePercent: Double,
    @SerializedName("activeVehicles") val activeVehicles: Int
)

/**
 * Утиліта для декодування JWT-токена на стороні клієнта:
 * витягує роль користувача з Payload та перевіряє термін дії токена (claim "exp").
 */
object JwtDecoder {
    data class JwtClaims(
        val username: String,
        val role: String,
        val isExpired: Boolean
    )

    fun parseToken(token: String?): JwtClaims? {
        if (token.isNullOrBlank()) return null
        val parts = token.split(".")
        if (parts.size != 3) return null
        return try {
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val json = JSONObject(payloadJson)
            val sub = json.optString("sub", "unknown")
            val role = json.optString("role", "USER")
            val exp = json.optLong("exp", 0L)
            val nowSec = System.currentTimeMillis() / 1000L
            JwtClaims(username = sub, role = role, isExpired = exp <= nowSec)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Генерує завідомо прострочений токен для тестування сценарію 401 Unauthorized на захисті.
     */
    fun createExpiredMockToken(username: String, role: String): String {
        val header = Base64.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        val expiredSec = (System.currentTimeMillis() / 1000L) - 3600
        val payload = Base64.encodeToString("""{"sub":"$username","role":"$role","exp":$expiredSec}""".toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        return "$header.$payload.expired_signature"
    }
}