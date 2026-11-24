package com.example.pw4.data

import java.time.LocalDate

/**
 * Модель даних зарядної станції для електромобілів (Варіант 9).
 * Використовується у практичних заняттях №14, №15 та №16.
 */
data class ChargingStation(
    val id: Int,                     // Унікальний ідентифікатор станції
    val name: String,                // Назва зарядної станції
    val connectorType: String,       // Тип конектора: "CCS2", "Type 2", "CHAdeMO"
    val currentType: String,         // Рід струму: "DC (Швидка)" або "AC (Звичайна)"
    val powerKw: Double,             // Максимальна потужність у кВт
    val pricePerKwh: Double,         // Тариф на заряджання (грн/кВт·год)
    val freePorts: Int,              // Кількість вільних портів
    val totalPorts: Int,             // Загальна кількість портів
    val status: String,              // Статус: "Вільна", "Заряджає", "На ремонті"
    val address: String,             // Адреса розташування
    val latitude: Double,            // Географічна широта
    val longitude: Double,           // Географічна довгота
    val lastUpdate: LocalDate        // Дата останнього оновлення даних телеметрії
)

/**
 * Модель об'єднаної телеметрії зарядної станції для ПЗ №13
 * (результат комбінування потоку потужності порту та потоку резерву мережі).
 */
data class ChargingTelemetry(
    val activePortPowerKw: Float,    // Потужність активного зарядного порту (кВт)
    val gridReserveKw: Float,        // Доступний резерв потужності електромережі (кВт)
    val totalLoadKw: Float,          // Сумарне навантаження вузла (кВт)
    val loadPercent: Int,            // Відсоток завантаження трансформаторної підстанції
    val isOverloadRisk: Boolean      // Попередження про наближення до ліміту 250 кВт
)

/**
 * Репозиторій тестових даних зарядних станцій для електромобілів (м. Київ).
 */
object ChargingStationsRepository {
    fun getSampleStations(): List<ChargingStation> = listOf(
        ChargingStation(
            id = 1,
            name = "ЕЗС «Хрещатик Fast»",
            connectorType = "CCS2",
            currentType = "DC (Швидка)",
            powerKw = 150.0,
            pricePerKwh = 15.99,
            freePorts = 2,
            totalPorts = 3,
            status = "Вільна",
            address = "вул. Хрещатик, 22",
            latitude = 50.4501,
            longitude = 30.5234,
            lastUpdate = LocalDate.of(2026, 11, 20)
        ),
        ChargingStation(
            id = 2,
            name = "ЕЗС «Поділ Hub»",
            connectorType = "Type 2",
            currentType = "AC (Звичайна)",
            powerKw = 22.0,
            pricePerKwh = 11.50,
            freePorts = 0,
            totalPorts = 4,
            status = "Заряджає",
            address = "Контрактова площа, 4",
            latitude = 50.4645,
            longitude = 30.5167,
            lastUpdate = LocalDate.of(2026, 11, 22)
        ),
        ChargingStation(
            id = 3,
            name = "ЕЗС «Оболонь SuperCharge»",
            connectorType = "CCS2",
            currentType = "DC (Швидка)",
            powerKw = 250.0,
            pricePerKwh = 17.50,
            freePorts = 3,
            totalPorts = 4,
            status = "Вільна",
            address = "просп. Володимира Івасюка, 14",
            latitude = 50.5012,
            longitude = 30.5115,
            lastUpdate = LocalDate.of(2026, 11, 24)
        ),
        ChargingStation(
            id = 4,
            name = "ЕЗС «Печерськ Eco»",
            connectorType = "CHAdeMO",
            currentType = "DC (Швидка)",
            powerKw = 50.0,
            pricePerKwh = 14.00,
            freePorts = 0,
            totalPorts = 2,
            status = "На ремонті",
            address = "бул. Лесі Українки, 26",
            latitude = 50.4285,
            longitude = 30.5382,
            lastUpdate = LocalDate.of(2026, 11, 18)
        ),
        ChargingStation(
            id = 5,
            name = "ЕЗС «КПІ Energy Point»",
            connectorType = "Type 2",
            currentType = "AC (Звичайна)",
            powerKw = 44.0,
            pricePerKwh = 12.00,
            freePorts = 1,
            totalPorts = 2,
            status = "Заряджає",
            address = "просп. Берестейський, 37",
            latitude = 50.4492,
            longitude = 30.4568,
            lastUpdate = LocalDate.of(2026, 11, 23)
        ),
        ChargingStation(
            id = 6,
            name = "ЕЗС «Лівобережна Charge»",
            connectorType = "CCS2",
            currentType = "DC (Швидка)",
            powerKw = 120.0,
            pricePerKwh = 15.20,
            freePorts = 2,
            totalPorts = 2,
            status = "Вільна",
            address = "вул. Євгена Сверстюка, 11",
            latitude = 50.4518,
            longitude = 30.5984,
            lastUpdate = LocalDate.of(2026, 11, 21)
        )
    )
}