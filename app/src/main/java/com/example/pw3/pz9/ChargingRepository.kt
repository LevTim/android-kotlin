package com.example.pw3.pz9

/**
 * Архітектурне рішення (Repository Pattern):
 * ChargingRepository виступає єдиною точкою входу для ViewModel.
 * Він агрегує локальний кеш та віддалений API, керує політикою TTL (5 хвилин),
 * забезпечує offline-fallback у разі збою мережі та виконує бізнес-розрахунки
 * (загальна віддана енергія, середня потужність заряджання, фінансовий дохід ЕЗС).
 */
class ChargingRepository(
    private val local: LocalChargingDataSource,
    private val remote: RemoteChargingDataSource
) {
    // Час життя кешу (TTL) — 5 хвилин
    private val cacheTtlMillis = 5 * 60 * 1000L
    private var lastFetchTimestamp = 0L

    // Тариф на швидку зарядку електромобіля (грн за 1 кВт·год)
    private val tariffUahPerKwh = 16.50

    /**
     * Отримує сесії заряджання з урахуванням кешу та прапорця forceRefresh.
     * Повертає пару: (список сесій, текстове повідомлення про джерело даних).
     */
    suspend fun getSessions(forceRefresh: Boolean = false): Result<Pair<List<ChargingSession>, String>> {
        val now = System.currentTimeMillis()
        val cached = local.getAll()

        // Якщо кеш свіжий і не вимагається примусове оновлення — повертаємо локальні дані
        if (!forceRefresh && cached.isNotEmpty() && (now - lastFetchTimestamp) < cacheTtlMillis) {
            return Result.success(cached to "Джерело: Локальний кеш (TTL актуальний)")
        }

        val remoteResult = remote.fetchAll()
        return remoteResult.fold(
            onSuccess = { remoteSessions ->
                local.clear()
                remoteSessions.forEach { local.insert(it) }
                lastFetchTimestamp = now
                Result.success(remoteSessions to "Джерело: REST API (кеш оновлено)")
            },
            onFailure = { error ->
                // Offline-fallback: якщо сервер недоступний, віддаємо збережений кеш
                if (cached.isNotEmpty()) {
                    Result.success(cached to "Джерело: Offline-кеш (помилка мережі: ${error.localizedMessage})")
                } else {
                    Result.failure(error)
                }
            }
        )
    }

    suspend fun addSession(session: ChargingSession): Result<ChargingSession> {
        return remote.create(session).onSuccess { created ->
            local.insert(created)
        }
    }

    suspend fun toggleSessionStatus(session: ChargingSession): Result<ChargingSession> {
        val newStatus = if (session.status == "Активна") "Завершено" else "Активна"
        val updatedSession = session.copy(status = newStatus)
        return remote.update(updatedSession).onSuccess {
            local.update(it)
        }
    }

    suspend fun deleteSession(id: Int): Result<Boolean> {
        return remote.delete(id).onSuccess {
            local.delete(id)
        }
    }

    /**
     * Комбінована фільтрація та сортування даних із локального джерела.
     */
    fun getFilteredAndSorted(
        query: String,
        connectorFilter: String,
        sortDescending: Boolean
    ): List<ChargingSession> {
        val filtered = local.filter(query, connectorFilter)
        return local.sortByPower(filtered, sortDescending)
    }

    /**
     * Бізнес-логіка: розрахунок агрегованих показників роботи мережі зарядних станцій.
     */
    fun calculateBusinessMetrics(sessions: List<ChargingSession>): ChargingStatsSummary {
        if (sessions.isEmpty()) return ChargingStatsSummary()
        val totalEnergy = sessions.sumOf { it.energyDeliveredKwh }
        val avgPower = sessions.map { it.powerKw }.average()
        val activeCount = sessions.count { it.status == "Активна" }
        val revenue = totalEnergy * tariffUahPerKwh

        return ChargingStatsSummary(
            totalSessions = sessions.size,
            activeSessionsCount = activeCount,
            totalEnergyKwh = totalEnergy,
            averagePowerKw = avgPower,
            estimatedRevenueUah = revenue
        )
    }
}