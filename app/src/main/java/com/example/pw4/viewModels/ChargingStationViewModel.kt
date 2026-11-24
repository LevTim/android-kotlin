package com.example.pw4.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.pw4.data.ChargingStation
import com.example.pw4.data.ChargingStationsRepository

/**
 * ViewModel для реактивного управління списком зарядних станцій (ПЗ №15, просунутий рівень).
 * Забезпечує пошук, багаторівневу фільтрацію (конектор + статус), сортування та вибір станції.
 */
class ChargingStationViewModel : ViewModel() {

    // Початковий реактивний список усіх зарядних станцій
    private val _stations = mutableStateListOf<ChargingStation>().apply {
        addAll(ChargingStationsRepository.getSampleStations())
    }

    // Пошуковий запит (пошук за назвою або адресою станції)
    var searchQuery by mutableStateOf("")
        private set

    // Фільтр 1-го рівня: тип роз'єму ("Всі", "CCS2", "Type 2", "CHAdeMO")
    var selectedConnector by mutableStateOf("Всі")
        private set

    // Фільтр 2-го рівня: статус роботи ("Всі", "Вільна", "Заряджає", "На ремонті")
    var selectedStatus by mutableStateOf("Всі")
        private set

    // Критерій сортування: "Назва", "Тип", "Потужність", "Дата", "Тариф"
    var sortBy by mutableStateOf("Потужність")
        private set

    // Обрана користувачем станція для перегляду розширених параметрів
    var selectedStation by mutableStateOf<ChargingStation?>(null)
        private set

    /**
     * Обчислювана властивість, яка повертає відфільтрований і відсортований список.
     * Автоматично оновлює UI при зміні будь-якого параметра фільтрації або пошуку.
     */
    val filteredStations: List<ChargingStation>
        get() {
            var result = _stations.toList()

            // 1. Фільтрація за типом конектора
            if (selectedConnector != "Всі") {
                result = result.filter {
                    it.connectorType.equals(selectedConnector, ignoreCase = true)
                }
            }

            // 2. Фільтрація за статусом роботи станції
            if (selectedStatus != "Всі") {
                result = result.filter {
                    it.status.equals(selectedStatus, ignoreCase = true)
                }
            }

            // 3. Пошук за назвою або адресою станції без урахування регістру
            if (searchQuery.isNotBlank()) {
                result = result.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.address.contains(searchQuery, ignoreCase = true)
                }
            }

            // 4. Сортування за обраним критерієм
            result = when (sortBy) {
                "Назва" -> result.sortedBy { it.name }
                "Тип" -> result.sortedBy { it.connectorType }
                "Потужність" -> result.sortedByDescending { it.powerKw }
                "Дата" -> result.sortedByDescending { it.lastUpdate }
                "Тариф" -> result.sortedBy { it.pricePerKwh }
                else -> result
            }

            return result
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateConnectorFilter(connector: String) {
        selectedConnector = connector
    }

    fun updateStatusFilter(status: String) {
        selectedStatus = status
    }

    fun updateSortBy(criteria: String) {
        sortBy = criteria
    }

    fun selectStation(station: ChargingStation) {
        selectedStation = if (selectedStation?.id == station.id) null else station
    }

    fun resetFilters() {
        searchQuery = ""
        selectedConnector = "Всі"
        selectedStatus = "Всі"
        sortBy = "Потужність"
    }
}