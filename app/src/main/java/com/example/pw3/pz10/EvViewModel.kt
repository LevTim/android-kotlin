package com.example.pw3.pz10

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

typealias StationSessionsMap = Map<Int, List<ChargingRecordDto>>
typealias StationStatsMap = Map<Int, StationTelemetryStatsDto>

/**
 * Архітектурне рішення (ПЗ №10 - Просунутий рівень):
 * ViewModel поєднує дані з кількох REST-ендпоінтів у реактивні StateFlow,
 * використовує кеш у пам'яті (_stationsCache, _sessionsCache, _statsCache) для офлайн-доступу
 * та застосовує функцію retryRequest для автоматичного повтору невдалих мережевих запитів.
 */
class EvViewModel : ViewModel() {

    private val api = EvRetrofitClient.api

    // Локальні кеші в пам'яті для швидкого доступу та стійкості до збоїв мережі
    private val _stationsCache = mutableMapOf<Int, ChargingStationDto>()
    private val _sessionsCache = mutableMapOf<Int, List<ChargingRecordDto>>()
    private val _statsCache = mutableMapOf<Int, StationTelemetryStatsDto>()

    private val _stations = MutableStateFlow<List<ChargingStationDto>>(emptyList())
    val stations: StateFlow<List<ChargingStationDto>> = _stations.asStateFlow()

    private val _sessions = MutableStateFlow<StationSessionsMap>(emptyMap())
    val sessions: StateFlow<StationSessionsMap> = _sessions.asStateFlow()

    private val _stats = MutableStateFlow<StationStatsMap>(emptyMap())
    val stats: StateFlow<StationStatsMap> = _stats.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Універсальна функція повторного виконання асинхронного запиту (до 3 спроб із затримкою).
     */
    private suspend fun <T> retryRequest(
        times: Int = 3,
        delayMillis: Long = 800L,
        block: suspend () -> T
    ): T {
        var attempt = 0
        var lastException: Exception? = null
        while (attempt < times) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                attempt++
                if (attempt < times) delay(delayMillis)
            }
        }
        throw lastException ?: Exception("Невідома помилка мережі")
    }

    fun loadStations() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = retryRequest { api.getStations() }
                val sorted = data.sortedBy { it.name }
                _stations.value = sorted
                sorted.forEach { _stationsCache[it.id] = it }
            } catch (e: Exception) {
                if (_stationsCache.isNotEmpty()) {
                    _stations.value = _stationsCache.values.toList()
                    _error.value = "Мережа недоступна. Показано кешовані станції."
                } else {
                    _error.value = "Помилка завантаження станцій: ${e.localizedMessage}"
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSessionsAndStats(stationId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val sessionList = retryRequest { api.getStationSessions(stationId) }
                val sortedSessions = sessionList.sortedByDescending { it.timestamp }
                _sessions.value = _sessions.value + (stationId to sortedSessions)
                _sessionsCache[stationId] = sortedSessions

                val stationStats = retryRequest { api.getStationStats(stationId) }
                _stats.value = _stats.value + (stationId to stationStats)
                _statsCache[stationId] = stationStats
            } catch (e: Exception) {
                _sessionsCache[stationId]?.let { cached ->
                    _sessions.value = _sessions.value + (stationId to cached)
                }
                _statsCache[stationId]?.let { cachedStats ->
                    _stats.value = _stats.value + (stationId to cachedStats)
                }
                _error.value = "Помилка оновлення телеметрії: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun generateSessions(stationId: Int, count: Int = 3) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val generated = retryRequest { api.simulateSessions(stationId, count) }
                val currentList = _sessions.value[stationId].orEmpty()
                val updated = (currentList + generated).sortedByDescending { it.timestamp }
                _sessions.value = _sessions.value + (stationId to updated)
                _sessionsCache[stationId] = updated

                val updatedStats = retryRequest { api.getStationStats(stationId) }
                _stats.value = _stats.value + (stationId to updatedStats)
                _statsCache[stationId] = updatedStats
            } catch (e: Exception) {
                _error.value = "Помилка генерації сесій: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }
}