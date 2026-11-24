package com.example.pw3.pz9

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel для ПЗ №9.
 * Керує реактивним станом UI через StateFlow та делегує всю роботу з даними у Repository.
 */
class ChargingViewModel : ViewModel() {

    private val repository = ChargingRepository(
        local = LocalChargingDataSource(),
        remote = RemoteChargingDataSource()
    )

    private val _sessions = MutableStateFlow<List<ChargingSession>>(emptyList())
    val sessions: StateFlow<List<ChargingSession>> = _sessions.asStateFlow()

    private val _summary = MutableStateFlow(ChargingStatsSummary())
    val summary: StateFlow<ChargingStatsSummary> = _summary.asStateFlow()

    private val _sourceStatus = MutableStateFlow("Ініціалізація...")
    val sourceStatus: StateFlow<String> = _sourceStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Поточні параметри фільтрації та сортування
    private var currentQuery = ""
    private var currentConnector = "Усі"
    private var sortDescending = true

    init {
        loadSessions(forceRefresh = false)
    }

    fun loadSessions(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getSessions(forceRefresh)
                .onSuccess { (_, statusText) ->
                    _sourceStatus.value = statusText
                    applyFiltersAndMetrics()
                }
                .onFailure { e ->
                    _errorMessage.value = "Помилка завантаження: ${e.localizedMessage}"
                }
            _isLoading.value = false
        }
    }

    fun addSession(stationName: String, connector: String, powerKw: Double, energyKwh: Double) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newSession = ChargingSession(
            id = 0,
            stationName = stationName.trim(),
            connectorType = connector,
            powerKw = powerKw,
            energyDeliveredKwh = energyKwh,
            status = "Активна",
            timestamp = currentTime
        )
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            repository.addSession(newSession)
                .onSuccess {
                    _sourceStatus.value = "Додано нову сесію та синхронізовано кеш"
                    applyFiltersAndMetrics()
                }
                .onFailure { _errorMessage.value = it.localizedMessage }
            _isLoading.value = false
        }
    }

    fun toggleStatus(session: ChargingSession) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSessionStatus(session).onSuccess {
                applyFiltersAndMetrics()
            }
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(id).onSuccess {
                applyFiltersAndMetrics()
            }
        }
    }

    fun updateFilter(query: String, connector: String) {
        currentQuery = query
        currentConnector = connector
        applyFiltersAndMetrics()
    }

    fun toggleSortOrder(): Boolean {
        sortDescending = !sortDescending
        applyFiltersAndMetrics()
        return sortDescending
    }

    private fun applyFiltersAndMetrics() {
        val processed = repository.getFilteredAndSorted(currentQuery, currentConnector, sortDescending)
        _sessions.value = processed
        _summary.value = repository.calculateBusinessMetrics(processed)
    }
}