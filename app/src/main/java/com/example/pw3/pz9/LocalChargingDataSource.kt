package com.example.pw3.pz9

/**
 * Локальне джерело даних (імітація Room Database / локального кешу в пам'яті).
 * Інкапсулює CRUD-операції, фільтрацію та сортування без звернення до мережі.
 */
class LocalChargingDataSource {

    // Внутрішнє сховище сесій заряджання
    private val sessionsCache = mutableListOf<ChargingSession>()

    @Synchronized
    fun getAll(): List<ChargingSession> = sessionsCache.toList()

    @Synchronized
    fun insert(session: ChargingSession) {
        sessionsCache.removeIf { it.id == session.id }
        sessionsCache.add(session)
    }

    @Synchronized
    fun update(session: ChargingSession) {
        sessionsCache.replaceAll { if (it.id == session.id) session else it }
    }

    @Synchronized
    fun delete(id: Int) {
        sessionsCache.removeIf { it.id == id }
    }

    @Synchronized
    fun clear() {
        sessionsCache.clear()
    }

    /**
     * Фільтрація сесій за типом конектора або назвою зарядної станції (без урахування регістру).
     */
    @Synchronized
    fun filter(query: String, connectorFilter: String): List<ChargingSession> {
        return sessionsCache.filter { session ->
            val matchesQuery = query.isBlank() ||
                    session.stationName.contains(query, ignoreCase = true) ||
                    session.status.contains(query, ignoreCase = true)
            val matchesConnector = connectorFilter == "Усі" ||
                    session.connectorType.equals(connectorFilter, ignoreCase = true)
            matchesQuery && matchesConnector
        }
    }

    /**
     * Сортування сесій за потужністю заряджання (кВт).
     */
    fun sortByPower(list: List<ChargingSession>, descending: Boolean): List<ChargingSession> {
        return if (descending) list.sortedByDescending { it.powerKw }
        else list.sortedBy { it.powerKw }
    }
}