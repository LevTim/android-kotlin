package com.example.pw3.mock

import android.util.Base64
import com.example.pw3.pz9.ChargingSession
import com.example.pw3.pz10.ChargingRecordDto
import com.example.pw3.pz10.ChargingStationDto
import com.example.pw3.pz10.ConnectorPortDto
import com.example.pw3.pz10.LocationInfo
import com.example.pw3.pz10.StationTelemetryStatsDto
import com.example.pw3.pz11.AdminStationControlDto
import com.example.pw3.pz11.LoginRequest
import com.example.pw3.pz11.LoginResponse
import com.example.pw3.pz11.ProtectedDriverProfile
import com.example.pw3.pz12.ChargerTelemetryDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

class MockEvBackendInterceptor : Interceptor {

    companion object {
        const val USE_MOCK_BACKEND = true

        private val gson = Gson()
        private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        private val pz9Sessions = mutableListOf(
            ChargingSession(1, "EcoFactor Hub #1 (Київ)", "CCS2", 150.0, 42.5, "Завершено", "2026-11-24 09:15"),
            ChargingSession(2, "TOKA Fast #14 (Львів)", "Type 2", 22.0, 18.0, "Активна", "2026-11-24 11:30"),
            ChargingSession(3, "IONITY Highway #3 (Житомир)", "CCS2", 350.0, 68.4, "Завершено", "2026-11-24 14:10"),
            ChargingSession(4, "Yasno E-Mobility #7 (Одеса)", "CHAdeMO", 50.0, 27.2, "Активна", "2026-11-24 16:45")
        )

        private val pz10Stations = listOf(
            ChargingStationDto(
                id = 101,
                name = "SuperCharge Kyiv Center",
                operator = "EcoFactor Network",
                maxPowerKw = 350.0,
                location = LocationInfo("Київ, вул. Хрещатик, 22", 50.4501, 30.5234),
                ports = listOf(
                    ConnectorPortDto("P1", "CCS2", 350, "Вільний"),
                    ConnectorPortDto("P2", "Type 2", 22, "Зайнятий")
                )
            ),
            ChargingStationDto(
                id = 102,
                name = "Lviv Green Hub",
                operator = "TOKA Energy",
                maxPowerKw = 160.0,
                location = LocationInfo("Львів, пр. Свободи, 15", 49.8419, 24.0315),
                ports = listOf(
                    ConnectorPortDto("P1", "CCS2", 160, "Зайнятий"),
                    ConnectorPortDto("P2", "CHAdeMO", 50, "Вільний")
                )
            )
        )

        private val pz10SessionsMap = mutableMapOf(
            101 to mutableListOf(
                ChargingRecordDto(1, 101, "2026-11-24T10:00:00", "Tesla Model 3", 54.2, 145.0, 34.5),
                ChargingRecordDto(2, 101, "2026-11-24T13:20:00", "Hyundai Ioniq 5", 68.0, 210.0, 38.0)
            ),
            102 to mutableListOf(
                ChargingRecordDto(3, 102, "2026-11-24T12:05:00", "Volkswagen ID.4", 41.5, 110.0, 31.2)
            )
        )

        private val flakyAttemptCounter = AtomicInteger(0)

        fun resetFlakyCounter() {
            flakyAttemptCounter.set(0)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!USE_MOCK_BACKEND) {
            return chain.proceed(request)
        }

        Thread.sleep(250)

        val path = request.url.encodedPath
        val method = request.method

        return when {
            // ================= ПЗ №9 =================
            path == "/pz9/sessions" && method == "GET" -> {
                jsonResponse(request, 200, gson.toJson(pz9Sessions))
            }
            path == "/pz9/sessions" && method == "POST" -> {
                val bodyStr = readRequestBody(request)
                val incoming = gson.fromJson(bodyStr, ChargingSession::class.java)
                val nextId = (pz9Sessions.maxOfOrNull { it.id } ?: 0) + 1
                val created = incoming.copy(id = nextId)
                pz9Sessions.add(created)
                jsonResponse(request, 201, gson.toJson(created))
            }
            path.startsWith("/pz9/sessions/") && method == "PUT" -> {
                val id = path.substringAfterLast("/").toIntOrNull() ?: -1
                val incoming = gson.fromJson(readRequestBody(request), ChargingSession::class.java)
                val index = pz9Sessions.indexOfFirst { it.id == id }
                if (index != -1) {
                    pz9Sessions[index] = incoming
                    jsonResponse(request, 200, gson.toJson(incoming))
                } else {
                    jsonResponse(request, 404, """{"msg":"Session not found"}""")
                }
            }
            path.startsWith("/pz9/sessions/") && method == "DELETE" -> {
                val id = path.substringAfterLast("/").toIntOrNull() ?: -1
                pz9Sessions.removeIf { it.id == id }
                jsonResponse(request, 200, """{"deleted":true}""")
            }

            // ================= ПЗ №10 =================
            path == "/pz10/stations" && method == "GET" -> {
                jsonResponse(request, 200, gson.toJson(pz10Stations))
            }
            path.matches(Regex("/pz10/stations/\\d+/sessions")) && method == "GET" -> {
                val id = path.split("/")[3].toInt()
                val list = pz10SessionsMap[id] ?: emptyList()
                jsonResponse(request, 200, gson.toJson(list))
            }
            path.matches(Regex("/pz10/stations/\\d+/stats")) && method == "GET" -> {
                val id = path.split("/")[3].toInt()
                val list = pz10SessionsMap[id] ?: emptyList()
                val avgPower = if (list.isEmpty()) 0.0 else list.map { it.avgPowerKw }.average()
                val totalEnergy = list.sumOf { it.energyKwh }
                val avgTemp = if (list.isEmpty()) 25.0 else list.map { it.connectorTempC }.average()
                val stationMax = pz10Stations.find { it.id == id }?.maxPowerKw ?: 150.0
                val utilization = ((avgPower / stationMax) * 100.0).coerceIn(0.0, 100.0)
                val stats = StationTelemetryStatsDto(
                    stationId = id,
                    averagePowerKw = avgPower,
                    totalEnergyDeliveredKwh = totalEnergy,
                    averageConnectorTempC = avgTemp,
                    utilizationPercent = utilization
                )
                jsonResponse(request, 200, gson.toJson(stats))
            }
            path.matches(Regex("/pz10/stations/\\d+/simulate/\\d+")) && method == "POST" -> {
                val parts = path.split("/")
                val id = parts[3].toInt()
                val count = parts[5].toInt()
                val cars = listOf("Audi e-tron GT", "Porsche Taycan", "BMW iX", "Nissan Leaf", "Kia EV6")
                val generated = (1..count).map { idx ->
                    ChargingRecordDto(
                        id = (100..9999).random(),
                        stationId = id,
                        timestamp = "2026-11-24T18:${10 + idx}:00",
                        vehicleModel = cars.random(),
                        energyKwh = (25..75).random().toDouble(),
                        avgPowerKw = (45..220).random().toDouble(),
                        connectorTempC = (28..42).random().toDouble()
                    )
                }
                pz10SessionsMap.getOrPut(id) { mutableListOf() }.addAll(generated)
                jsonResponse(request, 200, gson.toJson(generated))
            }

            // ================= ПЗ №11 =================
            path == "/pz11/login" && method == "POST" -> {
                val req = gson.fromJson(readRequestBody(request), LoginRequest::class.java)
                val role = when {
                    req.username == "admin" && req.password == "admin123" -> "ADMIN"
                    req.username == "driver" && req.password == "driver123" -> "USER"
                    else -> null
                }
                if (role == null) {
                    jsonResponse(request, 401, """{"msg":"Невірний логін або пароль"}""")
                } else {
                    val token = createMockJwt(req.username, role, validForSeconds = 3600)
                    jsonResponse(request, 200, gson.toJson(LoginResponse(token, role, req.username)))
                }
            }
            path == "/pz11/protected" && method == "GET" -> {
                val claims = validateAuthHeader(request.header("Authorization"))
                    ?: return jsonResponse(request, 401, """{"msg":"Токен відсутній, невалідний або прострочений (401)"}""")

                val profile = ProtectedDriverProfile(
                    username = claims.optString("sub"),
                    role = claims.optString("role"),
                    rfidCardNumber = "EV-UA-9941-2026",
                    balanceUah = if (claims.optString("role") == "ADMIN") 99999.0 else 1450.50,
                    activeTariff = "Fast DC 16.50 грн/кВт·год"
                )
                jsonResponse(request, 200, gson.toJson(profile))
            }
            path == "/pz11/admin-only" && method == "GET" -> {
                val claims = validateAuthHeader(request.header("Authorization"))
                    ?: return jsonResponse(request, 401, """{"msg":"Необхідна авторизація (401 Unauthorized)"}""")

                if (claims.optString("role") != "ADMIN") {
                    return jsonResponse(request, 403, """{"msg":"Доступ заборонено: потрібні права ADMIN (403 Forbidden)"}""")
                }

                val adminStations = listOf(
                    AdminStationControlDto(1, "EV-Hub Kyiv #01", "ONLINE", 400.0, 99.8, 4),
                    AdminStationControlDto(2, "EV-Hub Lviv #04", "ONLINE", 240.0, 98.5, 2),
                    AdminStationControlDto(3, "EV-Hub Odesa #02", "MAINTENANCE", 150.0, 87.0, 0)
                )
                jsonResponse(request, 200, gson.toJson(adminStations))
            }

            // ================= ПЗ №12 =================
            path == "/pz12/telemetry/today" && method == "GET" -> {
                val telemetry = ChargerTelemetryDto(
                    stationCode = "EV-KPI-009",
                    timestamp = "2026-11-24 20:15:42",
                    voltageV = 398.4,
                    currentA = 215.0,
                    powerKw = 85.6,
                    connectorTempC = 34.2,
                    status = "CHARGING_OPTIMAL"
                )
                jsonResponse(request, 200, gson.toJson(telemetry))
            }
            path == "/pz12/telemetry/flaky" && method == "GET" -> {
                val attempt = flakyAttemptCounter.incrementAndGet()
                if (attempt % 3 != 0) {
                    jsonResponse(request, 503, """{"msg":"Тимчасове перевантаження контролера ЕЗС (спроба $attempt)"}""")
                } else {
                    val telemetry = ChargerTelemetryDto(
                        stationCode = "EV-KPI-009 (Відновлено після retry)",
                        timestamp = "2026-11-24 20:16:05",
                        voltageV = 401.2,
                        currentA = 250.0,
                        powerKw = 100.3,
                        connectorTempC = 36.1,
                        status = "RECOVERED_ONLINE"
                    )
                    jsonResponse(request, 200, gson.toJson(telemetry))
                }
            }
            path.startsWith("/pz12/telemetry/error/") && method == "GET" -> {
                val code = path.substringAfterLast("/").toIntOrNull() ?: 500
                jsonResponse(request, code, """{"msg":"Симульована HTTP помилка $code"}""")
            }

            else -> jsonResponse(request, 404, """{"msg":"Endpoint not found: $path"}""")
        }
    }

    private fun createMockJwt(username: String, role: String, validForSeconds: Long): String {
        val header = Base64.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        val expEpochSec = (System.currentTimeMillis() / 1000L) + validForSeconds
        val payloadJson = """{"sub":"$username","role":"$role","exp":$expEpochSec}"""
        val payload = Base64.encodeToString(payloadJson.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        return "$header.$payload.mock_signature_kpi_pw3"
    }

    private fun validateAuthHeader(authHeader: String?): JSONObject? {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) return null
        val token = authHeader.removePrefix("Bearer ").trim()
        val parts = token.split(".")
        if (parts.size != 3) return null
        return try {
            val decoded = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val json = JSONObject(decoded)
            val exp = json.optLong("exp", 0L)
            val nowSec = System.currentTimeMillis() / 1000L
            if (exp <= nowSec) null else json
        } catch (e: Exception) {
            null
        }
    }

    private fun readRequestBody(request: okhttp3.Request): String {
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun jsonResponse(request: okhttp3.Request, code: Int, body: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "HTTP $code")
            .body(body.toResponseBody(jsonMediaType))
            .build()
    }
}